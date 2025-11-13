package net.qilla.snowrest.outdated;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public final class PacketDebug {
    private final int SECTIONS_PER_CHUNK = 24;
    private final int BLOCKS_PER_SECTION = 4096;
    private final int BIOMES_PER_SECTION = 64;

    private final Logger logger;

    public PacketDebug(Logger logger) {
        this.logger = logger;
    }

    public void init(PacketEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        PacketContainer packet = event.getPacket();

        int chunkX = packet.getIntegers().read(0);
        int chunkZ = packet.getIntegers().read(1);

        logger.info("Loading chunk @ {} {}", chunkX, chunkZ);

        WrappedLevelChunkData.ChunkData chunkData = packet.getLevelChunkData().read(0);
        RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(
                Unpooled.wrappedBuffer(chunkData.getBuffer()),
                ((CraftWorld) world).getHandle().registryAccess()
        );

        try {
            for (int i = 0; i < SECTIONS_PER_CHUNK; i++) {
                int sectionY = -4 + i;
                logger.info("# Chunk Section {}", sectionY);

                chunkSection(registryBuf);
            }

        } finally {
            registryBuf.release();
        }
    }

    private void chunkSection(RegistryFriendlyByteBuf registryBuf) {
        //The first two bytes that included; specifies the total number of blocks that exist in this chunk section.
        short blockCount = registryBuf.readShort();
        logger.info("   - {} total blocks", blockCount);

        sectionBlockStates(registryBuf);
        sectionBiomes(registryBuf);
    }

    private void sectionBlockStates(RegistryFriendlyByteBuf registryBuf) {
        logger.info("   + Block States");

        int bpe = registryBuf.readUnsignedByte();

        if(bpe == 0) {
            int stateID = registryBuf.readVarInt();
            Block block = Block.stateById(stateID).getBlock();

            logger.info("      - Single palette of {}", block.getName());
        } else {
            int paletteLength = 0;
            boolean isDirect = false;

            if(bpe <= 8) {
                //Indirect palette
                bpe = Math.max(bpe, 4);

                paletteLength = registryBuf.readVarInt();

                logger.info("      + {} Palette of", paletteLength);
                for (int pallete = 0; pallete < paletteLength; pallete++) {
                    int stateID = registryBuf.readVarInt();
                    Block block = Block.stateById(stateID).getBlock();

                    logger.info("          > [{}]: Block ID {} : {}", pallete, stateID, block.getName());
                }
            } else {
                //Direct palette
                logger.info("      - Large palette skipped");
                isDirect = true;
            }

            int entriesPerLong = 64 / bpe;
            int dataLengthBits = BLOCKS_PER_SECTION * bpe;
            int dataArrayLength = (BLOCKS_PER_SECTION + entriesPerLong - 1) / entriesPerLong;

            logger.info("      - {} bits per entry", bpe);
            logger.info("      - total data size of {}KB", dataLengthBits / 1000.0f);
            logger.info("      - {} entries per long", entriesPerLong);
            logger.info("      - {} longs", dataArrayLength);

            int blockIndex = 0;
            int invalidBlocks = 0;
            int validBlocks = 0;

            for(int i = 0; i < dataArrayLength; i ++) {
                long packedData = registryBuf.readLong();

                for(int j = 0; j < entriesPerLong && blockIndex < 4096; j++) {
                    int shift = j * bpe;
                    long mask = (1L << bpe) - 1;
                    int paletteIndex = (int) ((packedData >> shift) & mask);

                    if(isDirect) {
                        validBlocks++;
                    } else {
                        if(paletteIndex >= paletteLength) {
                            logger.warn("      - Block {} has invalid palette index: {} (max: {})", blockIndex, paletteIndex, paletteLength - 1);
                            invalidBlocks++;
                        } else {
                            validBlocks++;
                        }
                    }
                    blockIndex++;
                }
            }

            logger.info("   - Verification complete: {} valid blocks, {} invalid blocks", validBlocks, invalidBlocks);
        }
    }

    private void sectionBiomes(RegistryFriendlyByteBuf registryBuf) {
        logger.info("   + Biomes");

        short bpe = registryBuf.readUnsignedByte();

        logger.info("      - {} bits per entry", bpe);

        if(bpe == 0) {
            //Single value palette
            int biomeID = registryBuf.readVarInt();

            logger.info("      - Single Palette of {}", biomeID);
        } else {
            int paletteLength = 0;
            boolean isDirect = false;

            if(bpe <= 3) {
                //Indirect palette
                paletteLength = registryBuf.readVarInt();

                logger.info("      + {} Palette of", paletteLength);
                for (int pallete = 0; pallete < paletteLength; pallete++) {
                    int biomeID = registryBuf.readVarInt();

                    logger.info("          > [{}]: Biome ID {}", pallete, biomeID);
                }
            } else {
                //Direct palette
                logger.info("      - Large palette skipped");
                isDirect = true;
            }

            int entriesPerLong = (int) Math.ceil(64.0 / bpe);
            int dataLengthBits = BIOMES_PER_SECTION * bpe;
            int dataArrayLength = (BIOMES_PER_SECTION + entriesPerLong - 1) / entriesPerLong;

            logger.info("      - total data size of {}KB", dataLengthBits / 1024.0f);
            logger.info("      - {} entries per long", entriesPerLong);
            logger.info("      - {} longs", dataArrayLength);

            int biomeIndex = 0;
            int invalidBiomes = 0;
            int validBiomes = 0;

            for(int i = 0; i < dataArrayLength; i ++) {
                long packedData = registryBuf.readLong();

                for(int j = 0; j < entriesPerLong && biomeIndex < BIOMES_PER_SECTION; j++) {
                    int shift = j * bpe;
                    long mask = (1L << bpe) - 1;
                    int paletteIndex = (int) ((packedData >> shift) & mask);

                    if(isDirect) {
                        validBiomes++;
                    } else {
                        if(paletteIndex >= paletteLength) {
                            logger.warn("      - Biome {} has invalid palette index: {} (max: {})", biomeIndex, paletteIndex, paletteLength - 1);
                            invalidBiomes++;
                        } else {
                            validBiomes++;
                        }
                    }
                    biomeIndex++;

                }
            }

            logger.info("   - Verification complete: {} valid blocks, {} invalid blocks", validBiomes, invalidBiomes);
        }
    }
}
