package com.thevortex.potionsmaster.render.util.xray;


import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.thevortex.potionsmaster.reference.Ores;
import com.thevortex.potionsmaster.PotionsMaster;
import com.thevortex.potionsmaster.render.util.BlockInfo;
import com.thevortex.potionsmaster.render.util.BlockStore;
import com.thevortex.potionsmaster.render.util.WorldRegion;

public class RenderEnqueue implements Runnable {
	private final WorldRegion box;

	public RenderEnqueue(WorldRegion region) {
		box = region;
	}

	/**
	 * Single-block version of blockFinder. Can safely be called directly
	 * for quick block check.
	 *
	 * @param pos   the BlockPos to check
	 * @param state the current state of the block
	 * @param add   true if the block was added to world, false if it was removed
	 */
	public static void checkBlock(BlockPos pos, BlockState state, boolean add) {
		if (!Controller.drawOres())
			return; // just pass

		BlockStore blockStore = Controller.getBlockStore();

		if (blockStore.isStoreEmpty())
			return; // just pass

		String defaultState = state.getBlock().defaultBlockState().toString();

		// Let's see if the block to check is an ore we monitor
		BlockStore.BlockDataWithUUID dataWithUUID = blockStore.getStoreByReference(defaultState);

		if (dataWithUUID != null) { // it's a block we are monitoring

			if (add) {
				double alpha = Math.max(0, (Controller.getRadius() - PotionsMaster.proxy.getClientPlayer().distanceToSqr(pos.getX(), pos.getY(), pos.getZ())) / Controller.getRadius() * 255);

				// the block was added to the world, let's add it to the drawing buffer
				Render.ores.add(new BlockInfo(pos, dataWithUUID.getBlockData().getColor().getColor(), alpha));

			} else {
				Render.ores.remove(new BlockInfo(pos, null, 0.0));
			}
		}
	}

	/**
	 * Our thread code for finding ores near the player.
	 */
	@Override
	public void run() {
		blockFinder();
	}

	/**
	 * Use Controller.requestBlockFinder() to trigger a scan.
	 */
	private void blockFinder() {
		if (Controller.getBlockStore().isStoreEmpty()) {
			Render.ores.clear();
		}

		final World world = PotionsMaster.proxy.getClientPlayer().level;

		final PlayerEntity player = PotionsMaster.proxy.getClientPlayer();

		final List<BlockInfo> renderQueue = new ArrayList<>();

		int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;

		// Used for cleaning up the searching process
		BlockState currentState;
		ResourceLocation block;
		BlockStore.BlockDataWithUUID dataWithUUID;
		// Loop on chunks (x, z)
		for (int chunkX = box.minChunkX; chunkX <= box.maxChunkX; chunkX++) {
			// Pre-compute the extend bounds on X
			int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
			lowBoundX = (x < box.minX) ? box.minX - x : 0; // lower bound for x within the extend
			highBoundX = (x + 15 > box.maxX) ? box.maxX - x : 15;// and higher bound. Basically, we clamp it to fit the radius.

			for (int chunkZ = box.minChunkZ; chunkZ <= box.maxChunkZ; chunkZ++) {
				// Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
				if (!world.hasChunk(chunkX, chunkZ)) {
					continue; // We won't find anything interesting in unloaded chunks
				}

				Chunk chunk = world.getChunk(chunkX, chunkZ);
				ChunkSection[] extendsList = chunk.getSections();

				// Pre-compute the extend bounds on Z
				int z = chunkZ << 4;
				lowBoundZ = (z < box.minZ) ? box.minZ - z : 0;
				highBoundZ = (z + 15 > box.maxZ) ? box.maxZ - z : 15;

				// Loop on the extends around the player's layer (6 down, 2 up)
				for (int curExtend = box.minChunkY; curExtend <= box.maxChunkY; curExtend++) {
					ChunkSection ebs = extendsList[curExtend];
					if (ebs == null) // happens quite often!
						continue;

					// Pre-compute the extend bounds on Y
					int y = curExtend << 4;
					lowBoundY = (y < box.minY) ? box.minY - y : 0;
					highBoundY = (y + 15 > box.maxY) ? box.maxY - y : 15;

					// Now that we have an extend, let's check all its blocks
					for (int i = lowBoundX; i <= highBoundX; i++) {
						for (int j = lowBoundY; j <= highBoundY; j++) {
							for (int k = lowBoundZ; k <= highBoundZ; k++) {
                                currentState = ebs.getBlockState(i, j, k);

                                // Reject blacklisted blocks
                                //if( Controller.blackList.contains(currentState.getBlock()) )
                                //	continue;
                                block = currentState.getBlock().getRegistryName();
                                if (block == null)
                                    continue;

                                if ((currentState.getBlock().getTags().contains(Ores.DIAMOND)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_diamond"))) {
                                    block = Ores.DIAMOND;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.LAPIS)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_lapis"))) {
                                    block = Ores.LAPIS;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.ALUMINIUM)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_aluminum"))) {
                                    block = Ores.ALUMINIUM;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.COPPER)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_copper"))) {
                                    block = Ores.COPPER;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.TIN)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_tin"))) {
                                    block = Ores.TIN;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.LEAD)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_lead"))) {
                                    block = Ores.LEAD;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.SILVER)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_silver"))) {
                                    block = Ores.SILVER;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.GOLD)) || (currentState.getBlock().getRegistryName().getPath().contains("nether_gold_ore"))) {
                                    block = Ores.GOLD;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.URANIUM)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_uranium"))) {
                                    block = Ores.URANIUM;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.NICKEL)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_nickel"))) {
                                    block = Ores.NICKEL;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.IRON)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_iron"))) {
                                    block = Ores.IRON;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.OSMIUM)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_osmium"))) {
                                    block = Ores.OSMIUM;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.ZINC)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_zinc"))) {
                                    block = Ores.ZINC;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.EMERALD)) {
                                    block = Ores.EMERALD;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.COAL)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_coal"))) {
                                    block = Ores.COAL;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.REDSTONE)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_redstone"))) {
                                    block = Ores.REDSTONE;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.QUARTZ)) || (currentState.getBlock().getRegistryName().getPath().contains("quartz"))) {
                                    block = Ores.QUARTZ;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.BISMUTH)) {
                                    block = Ores.BISMUTH;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.CRIMSONIRON)) {
                                    block = Ores.CRIMSONIRON;
                                }
                                if ((currentState.getBlock().getTags().contains(Ores.PLATINUM)) || (currentState.getBlock().getRegistryName().getPath().contains("ore_other_platinum"))) {
                                    block = Ores.PLATINUM;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.NETHERITE)) {
                                    block = Ores.NETHERITE;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.ALLTHEMODIUM)) {
                                    block = Ores.ALLTHEMODIUM;
                                }
                                if (currentState.getBlock().getTags().contains(Ores.VIBRANIUM)) {
                                    block = Ores.VIBRANIUM;
								}
								if (currentState.getBlock().getTags().contains(Ores.UNOBTAINIUM)) {
									block = Ores.UNOBTAINIUM;
								}

								dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
								if (dataWithUUID == null)
									continue;

								if (dataWithUUID.getBlockData() == null || !dataWithUUID.getBlockData().isDrawing()) // fail safe
									continue;

								// Calculate distance from player to block. Fade out further away blocks
								//double alpha = Math.max(0, ((Controller.getRadius() - PotionsMaster.proxy.getClientPlayer().getDistanceSq(x + i, y + j, z + k)) / Controller.getRadius() ) * 255);
								double alpha = Math.max(0, Controller.getRadius() - PotionsMaster.proxy.getClientPlayer().distanceToSqr(x + i, y + j, z + k) / (Controller.getRadius() / 4));
								// Push the block to the render queue
								renderQueue.add(new BlockInfo(x + i, y + j, z + k, dataWithUUID.getBlockData().getColor().getColor(), alpha));
							}
						}
					}
				}
			}
		}
		renderQueue.sort((t, t1) -> Double.compare(t1.distSqr(new Vector3i(player.getX(),player.getY(),player.getZ())), t.distSqr(new Vector3i(player.getX(),player.getY(),player.getZ()))));

		Render.ores.clear();
		Render.ores.addAll(renderQueue); // Add all our found blocks to the Render.ores list. To be use by Render when drawing.
	}
}
