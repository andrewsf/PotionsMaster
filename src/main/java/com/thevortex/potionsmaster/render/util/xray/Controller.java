package com.thevortex.potionsmaster.render.util.xray;


import com.mojang.math.Vector3d;
import com.thevortex.potionsmaster.PotionsMaster;
import com.thevortex.potionsmaster.render.util.BlockStore;
import com.thevortex.potionsmaster.render.util.WorldRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;


import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller {
	// Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
	private static final int[] distanceList = new int[]{8, 16, 32, 48, 64, 80, 128, 256};

	public static ArrayList blackList = new ArrayList<Block>() {{
		add(Blocks.AIR);
		add(Blocks.BEDROCK);
		add(Blocks.STONE);
		add(Blocks.GRASS);
		add(Blocks.DIRT);
	}};

	private static Vector3d lastPlayerPos = null;


	// Thread management
	private static Future task;
	private static ExecutorService executor;
	// Draw states
	private static boolean drawOres = false; // Off by default

	public static BlockStore getBlockStore() {
		return PotionsMaster.blockStore;
	}


	public static boolean drawOres() {
		return drawOres && (Minecraft.getInstance().level != null) && (Minecraft.getInstance().player != null);
	}

	public static void toggleDrawOres() {
		if (!drawOres) {
			Render.ores.clear();
			executor = Executors.newSingleThreadExecutor();
			drawOres = true;
			requestBlockFinder(true);

		} else {
			shutdownExecutor();
		}
	}

	public static int getRadius() {
		return distanceList[1];
	}

	public static void incrementCurrentDist() {

	}

	public static void decrementCurrentDist() {

	}


	private static boolean playerHasMoved() {
		if ((Minecraft.getInstance().player == null) && (Controller.drawOres)) {
			toggleDrawOres();
			return false;
		}

		return lastPlayerPos == null
				|| lastPlayerPos.x != Minecraft.getInstance().player.getX()
				|| lastPlayerPos.y != Minecraft.getInstance().player.getY()
				|| lastPlayerPos.z != Minecraft.getInstance().player.getZ();
	}

	private static void updatePlayerPosition() {

		lastPlayerPos = new Vector3d(Minecraft.getInstance().player.position().x,Minecraft.getInstance().player.position().y,Minecraft.getInstance().player.position().z);
	}

	public static synchronized void requestBlockFinder(boolean force) {
		if (drawOres() && (task == null || task.isDone()) && (force || playerHasMoved())) // world/player check done by drawOres()
		{
			updatePlayerPosition(); // since we're about to run, update the last known position

			WorldRegion region = new WorldRegion(lastPlayerPos, getRadius(), Minecraft.getInstance().level.getMinBuildHeight(),Minecraft.getInstance().level.getMaxBuildHeight()); // the region to scan for ores
			//PotionsMaster.LOGGER.info("min " + PotionsMaster.proxy.getClientWorld().getMinBuildHeight() + " >> 4 = "+ (PotionsMaster.proxy.getClientWorld().getMinBuildHeight() >> 4) + "max " + PotionsMaster.proxy.getClientWorld().getMaxBuildHeight() + " >> 4 = "+ (PotionsMaster.proxy.getClientWorld().getMaxBuildHeight() >> 4)  );
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


