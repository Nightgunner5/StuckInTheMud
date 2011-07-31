package me.chrizc.sitm;

import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Wool;

/**
 * Usage instructions:
 * * Get an instance of Arena using {@link Arena#build(org.bukkit.Location, int, int, int, java.util.Random)}.
 * * Keep a reference to the instance and call {@link Arena#tearDown()} when you are done with it. This will return all changed blocks to their original state.
 * * If {@link Arena#tearDown()} is not called before the returned Arena is garbage collected, the arena will be torn down via a finalizer.
 *
 * @author Nightgunner5
 */
public abstract class Arena {
	public static Arena build(Location center, int xSize, int zSize, int height, Random random) {
		return new Internal(center, xSize, zSize, height, random);
	}

	public static boolean isWithin(int[] min, int[] max, Block block) {
		return isWithin(min, max, new int[] {block.getX(), block.getY(), block.getZ()});
	}

	public static boolean isWithin(int[] min, int[] max, int[] point) {
		for (int i = 0; i < 3; i++) {
			if (min[i] > point[i]) {
				return false;
			}
			if (max[i] < point[i]) {
				return false;
			}
		}
		return true;
	}

	public static int[][] parseMinMax(String[] point1, String[] point2) throws NumberFormatException {
		int[][] minmax = new int[2][3];
		for (int i = 0; i < 3; i++) {
			int coord1 = Integer.parseInt(point1[i]);
			int coord2 = Integer.parseInt(point2[i]);
			minmax[0][i] = Math.min(coord1, coord2);
			minmax[1][i] = Math.max(coord1, coord2);
		}
		return minmax;
	}

	public abstract boolean isWithin(Block block);

	public abstract boolean isWithin(int[] point);

	public abstract String[] getBounds();

	private static class Internal extends Arena {
		private final BlockState[] blocks;
		private boolean tornDown;
		private final int[] min;
		private final int[] max;
		private final World world;

		Internal(Location center, int xSize, int zSize, int height, Random random) {
			blocks = new BlockState[(xSize + 1) * (zSize + 1) * 4 * (height + 1)];
			world = center.getWorld();
			min = new int[] {
				center.getBlockX() - xSize,
				center.getBlockY(),
				center.getBlockZ() - zSize
			};
			max = new int[] {
				center.getBlockX() + xSize,
				center.getBlockY() + height,
				center.getBlockZ() + zSize
			};
			generate(world, random, min[0], max[0], min[1], max[1], min[2], max[2]);
		}

		@Override
		protected void finalize() throws Throwable {
			tearDown();
		}

		public void tearDown() {
			if (tornDown) {
				return;
			}
			tornDown = true;
			for (BlockState block : blocks) {
				if (block != null) {
					block.update(true);
				}
			}
		}

		private void generate(World world, Random random, int startX, int endX, int startY, int endY, int startZ, int endZ) {
			int zs = endY - startY + 1;
			int xs = (endZ - startZ + 1) * zs;
			// Backup
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					for (int z = startZ; z <= endZ; z++) {
						blocks[(x - startX) * xs + (z - startZ) * zs + (y - startY)] = world.getBlockAt(x, y, z).getState();
					}
				}
			}
			// Clear
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					for (int z = startZ; z <= endZ; z++) {
						world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
			// Walls
			for (int y = startY; y <= endY; y++) {
				for (int x = startX; x <= endX; x++) {
					world.getBlockAt(x, y, startZ).setType(Material.WOOL);
					BlockState state = world.getBlockAt(x, y, startZ).getState();
					if (state.getData() instanceof Wool) {
						((Wool) state.getData()).setColor(DyeColor.LIME);
						state.update();
					}
					world.getBlockAt(x, y, endZ).setType(Material.WOOL);
					state = world.getBlockAt(x, y, endZ).getState();
					if (state.getData() instanceof Wool) {
						((Wool) state.getData()).setColor(DyeColor.LIME);
						state.update();
					}
				}
				for (int z = startZ; z <= endZ; z++) {
					world.getBlockAt(startX, y, z).setType(Material.WOOL);
					BlockState state = world.getBlockAt(startX, y, z).getState();
					if (state.getData() instanceof Wool) {
						((Wool) state.getData()).setColor(DyeColor.LIME);
						state.update();
					}
					world.getBlockAt(endX, y, z).setType(Material.WOOL);
					state = world.getBlockAt(endX, y, z).getState();
					if (state.getData() instanceof Wool) {
						((Wool) state.getData()).setColor(DyeColor.LIME);
						state.update();
					}
				}
			}
			// Roof
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					world.getBlockAt(x, endY, z).setType(Material.GLASS);
				}
			}
			// Floor
			for (int x = startX; x <= endX; x++) {
				for (int z = startZ; z <= endZ; z++) {
					world.getBlockAt(x, startY, z).setType(Material.WOOL);
					BlockState state = world.getBlockAt(x, startY, z).getState();
					if (state.getData() instanceof Wool) {
						((Wool) state.getData()).setColor(DyeColor.PINK);
						state.update();
					}
				}
			}
			buildObstacles(world, random, startX + 1, endX - 1, startY + 1, endY - 1, startZ + 1, endZ - 1);
		}

		private void buildObstacles(World world, Random random, int startX, int endX, int startY, int endY, int startZ, int endZ) {
			int area = (endX - startX) * (endZ - startZ);
			int volume = area * (endY - startY);

			// Low walls
			for (int i = 0; i < area / 30; i++) {
				int x1 = random.nextInt(endX - startX + 1) + startX;
				int x2 = x1;
				int y1 = startY;
				int y2 = random.nextInt(4) + startY;
				int z1 = random.nextInt(endZ - startZ + 1) + startZ;
				int z2 = z1;

				if (random.nextBoolean()) {
					x2 += random.nextInt(5) - random.nextInt(5);
					if (x2 < startX) {
						x2 = startX;
					}
					if (x2 > endX) {
						x2 = endX;
					}
				} else {
					z2 += random.nextInt(5) - random.nextInt(5);
					if (z2 < startZ) {
						z2 = startZ;
					}
					if (z2 > endZ) {
						z2 = endZ;
					}
				}

				for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
					for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
						for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
							if (y == startY + 1 && random.nextInt(8) == 0) {
								continue;
							}
							Block b = world.getBlockAt(x, y, z);
							b.setType(Material.WOOL);
							BlockState state = b.getState();
							if (state.getData() instanceof Wool) {
								((Wool) state.getData()).setColor(DyeColor.YELLOW);
								state.update();
							}
						}
					}
				}
			}
		}

		@Override
		public boolean isWithin(Block block) {
			return block.getWorld() == world && isWithin(min, max, block);
		}

		@Override
		public boolean isWithin(int[] point) {
			return isWithin(min, max, point);
		}

		@Override
		public String[] getBounds() {
			return new String[] {
						new StringBuilder().append(min[0]).append(',').append(min[1]).append(',').append(min[2]).toString(),
						new StringBuilder().append(max[0]).append(',').append(max[1]).append(',').append(max[2]).toString()
					};
		}
	}

	public abstract void tearDown();
}