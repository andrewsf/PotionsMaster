package com.thevortex.potionsmaster.render.util.xray;


import com.thevortex.potionsmaster.PotionsMaster;
import com.thevortex.potionsmaster.render.util.BlockStore;
import com.thevortex.potionsmaster.render.util.WorldRegion;
import net.minecraft.util.math.vector.Vector3i;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller {
	// Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
	private static final int[] distanceList = new int[]{8, 16, 32, 48, 64, 80, 128, 256};

//	public static List<Block> blackList = new ArrayList<>(Arrays.asList(
//			Blocks.AIR,
//			Blocks.BEDROCK,
//			Blocks.STONE,
//			Blocks.GRASS,
//			Blocks.DIRT
//	));

	private static Vector3i lastPlayerPos = null;


	// Thread management
	private static Future<?> task;
	private static ExecutorService executor;
	// Draw states
	private static boolean drawOres = false; // Off by default

	public static BlockStore getBlockStore() {
		return PotionsMaster.blockStore;
	}


	public static boolean drawOres() {
		return drawOres && (PotionsMaster.proxy.getClientWorld() != null) && (PotionsMaster.proxy.getClientPlayer() != null);
	}

	public static void toggleDrawOres() {
		if (!drawOres) {
			Render.clearOres();
			executor = Executors.newSingleThreadExecutor();
			drawOres = true;
			requestBlockFinder(true);

		} else {
			shutdownExecutor();
		}
	}

	public static int getRadius() {
		return distanceList[2];
	}

	public static void incrementCurrentDist() {

	}

	public static void decrementCurrentDist() {

	}


	private static boolean playerHasMoved() {
		if ((PotionsMaster.proxy.getClientWorld() == null) && (Controller.drawOres)) {
			toggleDrawOres();
			return false;
		}

		return lastPlayerPos == null
				|| lastPlayerPos.getX() != PotionsMaster.proxy.getClientPlayer().getX()
				|| lastPlayerPos.getZ() != PotionsMaster.proxy.getClientPlayer().getZ();
	}

	private static void updatePlayerPosition() {

		lastPlayerPos = new Vector3i(PotionsMaster.proxy.getClientPlayer().position().x,PotionsMaster.proxy.getClientPlayer().position().y,PotionsMaster.proxy.getClientPlayer().position().z);
	}

	public static synchronized void requestBlockFinder(boolean force) {
		if (drawOres() && (task == null || task.isDone()) && (force || playerHasMoved())) // world/player check done by drawOres()
		{
			updatePlayerPosition(); // since we're about to run, update the last known position

			WorldRegion region = new WorldRegion(lastPlayerPos, getRadius()); // the region to scan for ores

			task = executor.submit(new RenderEnqueue(region));

		}
	}

	public static void shutdownExecutor() {
		// Important. If drawOres is true when a player logs out then logs back in, the next requestBlockFinder will crash
		drawOres = false;
		try {
			executor.shutdownNow();
		} catch (Throwable ignore) {
		}
	}


}


