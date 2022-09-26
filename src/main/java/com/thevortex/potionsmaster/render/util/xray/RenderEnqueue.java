package com.thevortex.potionsmaster.render.util.xray;


import com.thevortex.potionsmaster.PotionsMaster;
import com.thevortex.potionsmaster.reference.Ores;
import com.thevortex.potionsmaster.render.util.BlockInfo;
import com.thevortex.potionsmaster.render.util.BlockStore;
import com.thevortex.potionsmaster.render.util.WorldRegion;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
				double dist = Math.sqrt(pos.distSqr(PotionsMaster.proxy.getClientPlayer().position(), true));
				double alpha = Math.max(0.0, 1.0 - dist / Controller.getRadius());

				// the block was added to the world, let's add it to the drawing buffer
				Render.addOre(pos, dataWithUUID.getBlockData().getColor(), alpha);
			} else {
				Render.removeOre(pos);
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
			Render.clearOres();
		}

		final World world = PotionsMaster.proxy.getClientPlayer().level;

		final PlayerEntity player = PotionsMaster.proxy.getClientPlayer();

		final List<BlockInfo> renderQueue = new ArrayList<>();

		// Loop on chunks (x, z)
		for (int chunkX = box.minChunkX; chunkX <= box.maxChunkX; chunkX++) {
			// Pre-compute the extend bounds on X
			int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
			// lower bound for x within the extend
			int lowBoundX = Math.max(0, box.minX - x);
			// and higher bound. Basically, we clamp it to fit the radius.
			int highBoundX = Math.min(15, box.maxX - x);

			for (int chunkZ = box.minChunkZ; chunkZ <= box.maxChunkZ; chunkZ++) {
				// Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
				if (!world.hasChunk(chunkX, chunkZ)) {
					continue; // We won't find anything interesting in unloaded chunks
				}

				Chunk chunk = world.getChunk(chunkX, chunkZ);
				ChunkSection[] extendsList = chunk.getSections();

				// Pre-compute the extend bounds on Z
				int z = chunkZ << 4;
				int lowBoundZ = Math.max(0, box.minZ - z);
				int highBoundZ = Math.min(15, box.maxZ - z);

				// Loop on the extends around the player's layer (6 down, 2 up)
				for (int curExtend = box.minChunkY; curExtend <= box.maxChunkY; curExtend++) {
					ChunkSection ebs = extendsList[curExtend];
					if (ebs == null) // happens quite often!
						continue;

					// Pre-compute the extend bounds on Y
					int y = curExtend << 4;
					int lowBoundY = Math.max(0, box.minY - y);
					int highBoundY = Math.min(15, box.maxY - y);

					// Now that we have an extend, let's check all its blocks
					for (int i = lowBoundX; i <= highBoundX; i++) {
						for (int j = lowBoundY; j <= highBoundY; j++) {
							for (int k = lowBoundZ; k <= highBoundZ; k++) {
								BlockState currentState = ebs.getBlockState(i, j, k);

								// Reject blacklisted blocks
								//if( Controller.blackList.contains(currentState.getBlock()) )
								 //	continue;

								ResourceLocation registryName = currentState.getBlock().getRegistryName();
								if (registryName == null)
									continue;

								Set<ResourceLocation> tags = currentState.getBlock().getTags();
								String registryPath = registryName.getPath();

								final ResourceLocation block;
								if (tags.contains(Ores.UNOBTAINIUM)) {
									block = Ores.UNOBTAINIUM;
								} else if (tags.contains(Ores.VIBRANIUM)) {
									block = Ores.VIBRANIUM;
								} else if (tags.contains(Ores.ALLTHEMODIUM)) {
									block = Ores.ALLTHEMODIUM;
								} else if (tags.contains(Ores.NETHERITE)) {
									block = Ores.NETHERITE;
								} else if (tags.contains(Ores.PLATINUM) || registryPath.contains("ore_other_platinum")) {
									block = Ores.PLATINUM;
								} else if (tags.contains(Ores.CRIMSONIRON)) {
									block = Ores.CRIMSONIRON;
								} else if (tags.contains(Ores.BISMUTH)) {
									block = Ores.BISMUTH;
								} else if (tags.contains(Ores.QUARTZ) || registryPath.contains("quartz")) {
									block = Ores.QUARTZ;
								} else if (tags.contains(Ores.REDSTONE) || registryPath.contains("ore_other_redstone")) {
									block = Ores.REDSTONE;
								} else if (tags.contains(Ores.COAL) || registryPath.contains("ore_other_coal")) {
									block = Ores.COAL;
								} else if (tags.contains(Ores.EMERALD)) {
									block = Ores.EMERALD;
								} else if (tags.contains(Ores.ZINC) || registryPath.contains("ore_other_zinc")) {
									block = Ores.ZINC;
								} else if (tags.contains(Ores.OSMIUM) || registryPath.contains("ore_other_osmium")) {
									block = Ores.OSMIUM;
								} else if (tags.contains(Ores.IRON) || registryPath.contains("ore_other_iron")) {
									block = Ores.IRON;
								} else if (tags.contains(Ores.NICKEL) || registryPath.contains("ore_other_nickel")) {
									block = Ores.NICKEL;
								} else if (tags.contains(Ores.URANIUM) || registryPath.contains("ore_other_uranium")) {
									block = Ores.URANIUM;
								} else if (tags.contains(Ores.GOLD) || registryPath.contains("nether_gold_ore")) {
									block = Ores.GOLD;
								} else if (tags.contains(Ores.SILVER) || registryPath.contains("ore_other_silver")) {
									block = Ores.SILVER;
								} else if (tags.contains(Ores.LEAD) || registryPath.contains("ore_other_lead")) {
									block = Ores.LEAD;
								} else if (tags.contains(Ores.TIN) || registryPath.contains("ore_other_tin")) {
									block = Ores.TIN;
								} else if (tags.contains(Ores.COPPER) || registryPath.contains("ore_other_copper")) {
									block = Ores.COPPER;
								} else if (tags.contains(Ores.ALUMINIUM) || registryPath.contains("ore_other_aluminum")) {
									block = Ores.ALUMINIUM;
								} else if (tags.contains(Ores.LAPIS) || registryPath.contains("ore_other_lapis")) {
									block = Ores.LAPIS;
								} else if (tags.contains(Ores.DIAMOND) || registryPath.contains("ore_other_diamond")) {
									block = Ores.DIAMOND;
								} else {
									block = registryName;
								}

								// Used for cleaning up the searching process
								BlockStore.BlockDataWithUUID dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
								if (dataWithUUID == null)
									continue;

								if (dataWithUUID.getBlockData() == null || !dataWithUUID.getBlockData().isDrawing()) // fail safe
									continue;

								// Calculate distance from player to block. Fade out further away blocks
								Vector3i blockPos = new Vector3i(x + i, y + j, z + k);

								double dist = Math.sqrt(PotionsMaster.proxy.getClientPlayer().distanceToSqr(
										blockPos.getX() + 0.5,
										blockPos.getY() + 0.5,
										blockPos.getZ() + 0.5));
								double alpha = Math.max(0.0, 1.0 - dist / Controller.getRadius());

								// Push the block to the render queue
								renderQueue.add(new BlockInfo(blockPos, dataWithUUID.getBlockData().getColor(), alpha));
							}
						}
					}
				}
			}
		}

		Vector3d playerPosition = player.position();
		renderQueue.sort((t, t1) -> Double.compare(t1.distSqr(playerPosition, true), t.distSqr(playerPosition, true)));

		// Add all our found blocks to the Render.ores list. To be use by Render when drawing.
		Render.replaceOres(renderQueue);
	}
}
