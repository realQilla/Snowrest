package net.qilla.snowrest;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.qilla.snowrest.data.DataPersistent;
import net.qilla.snowrest.painter.HeightmapEditor;
import net.qilla.snowrest.painter.Paintries;
import net.qilla.snowrest.painter.PalettePainter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class Snowrest extends JavaPlugin {
    private Logger logger;
    private ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        this.logger = getSLF4JLogger();

        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onEnable() {
        this.protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ITEM_ON) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                World world = player.getWorld();
                Object packetObject = event.getPacket().getHandle();

                if(!(packetObject instanceof ServerboundUseItemOnPacket packet)) {
                    logger.warn("Failed to read use item action from packet");
                    return;
                }

                BlockPos blockPos = packet.getHitResult().getBlockPos();
                BlockState blockState = ((CraftWorld) world).getHandle().getBlockState(blockPos);

                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + blockState.getBlock().getName() + " ID: " + Block.getId(blockState) +"</yellow>"));
            }
        });

        this.protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                World world = event.getPlayer().getWorld();
                //new PacketDebug(logger);

                StructureModifier<WrappedLevelChunkData.ChunkData> wrapperChunkData = packet.getLevelChunkData();
                WrappedLevelChunkData.ChunkData chunkData = wrapperChunkData.readSafely(0);
                int chunkX = packet.getIntegers().read(0);
                int chunkZ = packet.getIntegers().read(1);
                long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);

                if(chunkData == null) {
                    logger.warn("Failed to read chunk data from packet.");
                    return;
                }

                int[][] iceHeightmap = HeightmapEditor.of(chunkData.getHeightmaps().get(EnumWrappers.HeightmapType.MOTION_BLOCKING))
                        .offsetY(-1)
                        .build();
                int[][] snowHeightmap = HeightmapEditor.of(chunkData.getHeightmaps().get(EnumWrappers.HeightmapType.MOTION_BLOCKING))
                        .build();
                int[][] otherHeightmap = HeightmapEditor.of(chunkData.getHeightmaps().get(EnumWrappers.HeightmapType.MOTION_BLOCKING))
                        .offsetY(1)
                        .stack(2)
                        .build();

                DissectChunkData dissect = new DissectChunkData(Unpooled.wrappedBuffer(chunkData.getBuffer()));
                ChunkSection[] chunk = dissect.sections();

                final int[] snowLayers = {6718, 6719, 6720};
                final int waterBlock = Block.getId(Blocks.WATER.defaultBlockState());
                final int iceBlock = Block.getId(Blocks.ICE.defaultBlockState());

                for(int sectionIndex = 0; sectionIndex < DataPersistent.SECTIONS_PER_CHUNK; sectionIndex++) {
                    int[] sectionIceHeightmap = iceHeightmap[sectionIndex];
                    int[] sectionSnowHeightmap = snowHeightmap[sectionIndex];
                    int[] sectionOtherHeightmap = otherHeightmap[sectionIndex];
                    ChunkSection section = chunk[sectionIndex];
                    PalettedContainer container = section.blockStates();

                    if(container.isEmpty()) continue;

                    PalettePainter painter = container.painter(chunkKey);
                    //painter.replace(waterBlock, iceBlock)
                    int[] finalizedIceHeightmap = painter.matchConditions(sectionIceHeightmap, Paintries.single(waterBlock));
                    int[] finalizedSnowHeightmap = painter.notMatchConditions(sectionSnowHeightmap, Paintries.single(waterBlock));

                    painter.setPositions(finalizedIceHeightmap, Paintries.multi(iceBlock));
                    painter.setPositions(finalizedSnowHeightmap, Paintries.multi(snowLayers));
                    painter.replaceNonAir(sectionOtherHeightmap, Paintries.single(0));

                    section.setBlockStates(painter.build());
//
//                    int[] paintTopSection = paintTop[sectionIndex];
//
//                    PalettedContainer blockStates = section.blockStates();
//                    BaseBitStorage originalData = blockStates.data();
//                    int originalBpe = originalData.bpe();
//                    XORShift random = new XORShift((int) System.currentTimeMillis());
//
//                    if(originalBpe == 0 || originalBpe > 8) continue;
//
//                    int[] originalPalette = blockStates.palette();
//                    int[] modifiedPalette = Arrays.copyOf(originalPalette, originalPalette.length + 4);
//
//                    int modifiedBpe = BitStorage.calculateBpeForBlockStates(modifiedPalette.length);
//                    int snowLayersPaletteIndex = modifiedPalette.length - 4;
//                    int iceBlockPaletteIndex = modifiedPalette.length - 1;
//
//                    for(int i = 0; i < snowLayers.length; i++) {
//                        modifiedPalette[snowLayersPaletteIndex + i] = snowLayers[i];
//                    }
//                    modifiedPalette[iceBlockPaletteIndex] = iceBlock;
//
//                    int airIndex = -1;
//                    int waterIndex = -1;
//
//                    for (int paletteIndex = 0; paletteIndex < modifiedPalette.length; paletteIndex++) {
//                        if (modifiedPalette[paletteIndex] == airBlock) {
//                            airIndex = paletteIndex;
//                            continue;
//                        }
//                        if(modifiedPalette[paletteIndex] == waterBlock) {
//                            waterIndex = paletteIndex;
//                            continue;
//                        }
//                    }
//
//                    int[] unpackedData = originalData.unpack(DataPersistent.BLOCKS_PER_SECTION);
//
//                    for(int sectionPos = 0; sectionPos < unpackedData.length; sectionPos ++) {
//                        int aboveIndex = sectionPos + DataPersistent.BLOCKS_PER_SECTION_LAYER;
//
//                        if(unpackedData[aboveIndex] != airIndex) continue;
//
//                        for(int heightEntry : paintTopSection) {
//                            if(sectionPos == heightEntry) {
//                                if(unpackedData[sectionPos] == waterIndex) {
//                                    unpackedData[sectionPos] = iceBlockPaletteIndex;
//                                    break;
//                                }
//
//                                unpackedData[aboveIndex] = snowLayersPaletteIndex + random.nextInt(0, snowLayers.length);
//                                break;
//                            }
//                        }
//                    }
//
//                    BitStorage modifiedData = BitStorage.ofUnpacked(modifiedBpe, unpackedData.length, unpackedData);
//
//                    section.blockStates().setPalette(modifiedPalette);
//                    section.blockStates().setData(modifiedData);
                }

                chunkData.setBuffer(dissect.buildBuffer());
                wrapperChunkData.writeSafely(0, chunkData);
            }
        });
    }
}
