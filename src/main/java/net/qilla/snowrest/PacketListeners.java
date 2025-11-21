package net.qilla.snowrest;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import io.netty.buffer.Unpooled;
import net.qilla.snowrest.config.SnowrestConfig;
import net.qilla.snowrest.data.DataPersistent;
import net.qilla.snowrest.data.RegistryIDs;
import net.qilla.snowrest.editor.ContainerEditor;
import net.qilla.snowrest.editor.HeightmapEditor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class PacketListeners {
    private static final Logger LOGGER = Snowrest.logger();
    private static PacketListeners INSTANCE;

    private final Snowrest plugin;
    private final ProtocolManager protocol;
    private PacketListener surfaceDecorator;
    private PacketListener weatherListener;
    private PacketListener hideWeather;

    int[] waterReplacements;
    int[] surfaceReplacements;

    private PacketListeners(@NotNull final Snowrest plugin) {
        this.plugin = plugin;
        this.protocol = plugin.protocolManager();

        this.populateUsingConfig(plugin.config());
        plugin.config().subscribe(this::populateUsingConfig);
    }

    public static @NotNull PacketListeners init(@NotNull final Snowrest plugin) {
        if(INSTANCE == null) {
            INSTANCE = new PacketListeners(plugin);
        }

        return INSTANCE;
    }

    private void populateUsingConfig(@NotNull final SnowrestConfig config) {
        if(!config.isEnabled()) {
            setSurfaceDecorator(false);
            setWeather(false);
            return;
        }

        if(config.isWaterFrozen()) {
            waterReplacements = config.waterReplacement().stream().mapToInt(Integer::intValue).toArray();
        } else {
            waterReplacements = new int[0];        }

        if(config.isSurfaceCovered()) {
            surfaceReplacements = config.surfaceReplacement().stream().mapToInt(Integer::intValue).toArray();
        } else {
            surfaceReplacements = new int[0];
        }

        setSurfaceDecorator(config.isSurfaceCovered() || config.isWaterFrozen());
        setWeather(config.isSnowing());
    }

    public void setSurfaceDecorator(boolean enabled) {
        if(surfaceDecorator == null && enabled) {
            surfaceDecorator = buildDecoratorPacket();
            protocol.addPacketListener(surfaceDecorator);
        } else if(surfaceDecorator != null && !enabled) {
            protocol.removePacketListener(surfaceDecorator);
            surfaceDecorator = null;
        }
    }

    public void setWeather(boolean enabled) {
        if(weatherListener == null && enabled) {
            weatherListener = buildForceWeather();
            hideWeather = buildHideWeather();

            protocol.addPacketListener(weatherListener);
            protocol.addPacketListener(hideWeather);
        } else if(weatherListener != null && !enabled) {
            protocol.removePacketListener(weatherListener);
            protocol.removePacketListener(hideWeather);

            weatherListener = null;
            hideWeather = null;
        }
    }

    private @NotNull PacketListener buildHideWeather() {
        return new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.GAME_STATE_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                int eventType = packet.getGameStateIDs().readSafely(0);

                switch(eventType) {
                    case 1, 7, 8 -> event.setCancelled(true);
                }
            }
        };
    }

    private @NotNull PacketListener buildForceWeather() {
        return new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.LOGIN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();

                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
                packetContainer.getGameStateIDs().write(0, 2);
                packetContainer.getFloat().write(0, 0.0f);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    protocol.sendServerPacket(player, packetContainer);
                });
            }
        };
    }

    private @NotNull PacketListener buildDecoratorPacket() {
        return new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                StructureModifier<WrappedLevelChunkData.ChunkData> wrapperChunkData = packet.getLevelChunkData();
                WrappedLevelChunkData.ChunkData chunkData = wrapperChunkData.readSafely(0);
                int chunkX = packet.getIntegers().read(0);
                int chunkZ = packet.getIntegers().read(1);
                long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);

                if(chunkData == null) {
                    LOGGER.warn("Failed to read chunk data from packet.");
                    return;
                }

                ProcessedChunk dissect = ProcessedChunk.of(Unpooled.wrappedBuffer(chunkData.getBuffer()));
                ChunkSection[] sections = dissect.sections();

                HeightmapEditor iceSnowEditor = HeightmapEditor.of(chunkData.getHeightmaps().get(EnumWrappers.HeightmapType.MOTION_BLOCKING));
                HeightmapEditor remainingEditor = iceSnowEditor.copy();

                iceSnowEditor.offsetY(-1);

                HeightmapEditor[] splitEditor = iceSnowEditor.separate(sections, EntryHolder.of(RegistryIDs.WATER_BLOCKS));
                HeightmapEditor iceHMEditor = splitEditor[0];
                HeightmapEditor snowHMEditor = splitEditor[1];

                snowHMEditor.offsetY(1);
                remainingEditor.offsetY(1);
                remainingEditor.stack(2);

                int[][] iceHeightmap = iceHMEditor.build();
                int[][] snowHeightmap = snowHMEditor.build();
                int[][] otherHeightmap = remainingEditor.build();

                for(int sectionIndex = 0; sectionIndex < DataPersistent.SECTIONS_PER_CHUNK; sectionIndex++) {
                    ChunkSection section = sections[sectionIndex];

                    int[] iceHeightmapSec = iceHeightmap[sectionIndex];
                    int[] snowHeightmapSec = snowHeightmap[sectionIndex];
                    int[] otherHeightmapSec = otherHeightmap[sectionIndex];

                    ContainerEditor blockStateEditor = ContainerEditor.ofBlockState(section.blockStates(), chunkKey)
                            .set(iceHeightmapSec, EntryHolder.of(waterReplacements))
                            .set(snowHeightmapSec, EntryHolder.of(surfaceReplacements))
                            .replaceExcept(otherHeightmapSec, RegistryIDs.AIR_STATE_IDS, EntryHolder.of(0));

                    section.setBlockStates(blockStateEditor.buildBlockStates());



                    ContainerEditor biomeEditor = ContainerEditor.ofBiome(section.biomes(), chunkKey)
                            .set(EntryHolder.of(RegistryIDs.SNOW_BIOME));

                    section.setBiomes(biomeEditor.buildBiomes());
                }

                chunkData.setBuffer(dissect.buildBuffer());
                wrapperChunkData.writeSafely(0, chunkData);
            }
        };
    }
}