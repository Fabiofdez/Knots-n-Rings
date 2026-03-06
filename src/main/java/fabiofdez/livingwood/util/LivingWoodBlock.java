package fabiofdez.livingwood.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LivingWoodBlock {
  public static boolean isNaturalWood(BlockState state) {
    String blockIdPath = BuiltInRegistries.BLOCK
        .getKey(state.getBlock())
        .getPath();

    return isWood(blockIdPath) && !isStripped(blockIdPath);
  }

  public static boolean isWood(BlockState state) {
    String blockIdPath = BuiltInRegistries.BLOCK
        .getKey(state.getBlock())
        .getPath();

    return isWood(blockIdPath);
  }

  public static boolean isStripped(BlockState state) {
    String blockIdPath = BuiltInRegistries.BLOCK
        .getKey(state.getBlock())
        .getPath();

    return isStripped(blockIdPath);
  }

  private static boolean isWood(String path) {
    return (path.endsWith("_log") || path.endsWith("_wood"));
  }

  private static boolean isStripped(String path) {
    return path.startsWith("stripped_");
  }

  public static boolean isNaturalLeaves(BlockState state) {
    if (!state.hasProperty(LeavesBlock.PERSISTENT)) return false;
    return state.is(BlockTags.LEAVES) && !state.getValue(LeavesBlock.PERSISTENT);
  }

  public static boolean isAlive(ServerLevel level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (LivingWoodBlock.isStripped(state)) return false;

    return neighborsOf(level, pos).sustainLife((neighbor, neighborPos) -> {
      if (LivingWoodBlock.isNaturalLeaves(neighbor)) {
        return true;
      }

      if (LivingWoodBlock.isNaturalWood(neighbor)) {
        return neighbor.getValue(Properties.ALIVE);
      }

      return false;
    });
  }

  public static NeighborIterable neighborsOf(ServerLevel level, BlockPos pos) {
    return new NeighborIterable(level, pos);
  }

  public static class Properties {
    public static final BooleanProperty ALIVE = BooleanProperty.create("alive");
  }

}
