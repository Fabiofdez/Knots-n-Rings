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

    boolean isWood = blockIdPath.endsWith("_log") || blockIdPath.endsWith("_wood");
    boolean isStripped = blockIdPath.startsWith("stripped_");
    return isWood && !isStripped;
  }

  public static boolean isNaturalLeaves(BlockState state) {
    if (!state.hasProperty(LeavesBlock.PERSISTENT)) return false;
    return state.is(BlockTags.LEAVES) && !state.getValue(LeavesBlock.PERSISTENT);
  }

  public static boolean isAliveNearby(BlockState state, ServerLevel level, BlockPos pos) {
    if (!LivingWoodBlock.isNaturalWood(state)) return false;

    return neighborsOf(level, pos).any((neighbor, neighborPos) -> {
      if (LivingWoodBlock.isNaturalLeaves(neighbor)) {
        return true;
      }

      if (LivingWoodBlock.isNaturalWood(neighbor)) {
        return !isNonliving(neighbor);
      }

      return false;
    });
  }

  public static boolean isTrunkNearby(BlockState state, ServerLevel level, BlockPos pos) {
    if (!LivingWoodBlock.isNaturalWood(state)) return false;

    return neighborsOf(level, pos).any((neighbor, neighborPos) -> {
      if (LivingWoodBlock.isNaturalLeaves(neighbor)) {
        return true;
      }

      if (LivingWoodBlock.isNaturalWood(neighbor)) {
        return isTrunk(neighbor);
      }

      return false;
    });
  }

  public static boolean isNonliving(BlockState state) {
    return state.getValue(Properties.NONLIVING);
  }

  public static boolean isSingleton(BlockState state) {
    return state.getValue(Properties.SINGLETON);
  }

  public static boolean isTrunk(BlockState state) {
    return state.getValue(Properties.IS_TRUNK);
  }

  public static NeighborIterable neighborsOf(ServerLevel level, BlockPos pos) {
    return new NeighborIterable(level, pos);
  }

  public static void updateState(ServerLevel level, BlockPos pos, boolean nowAlive) {
    BlockState state = level.getBlockState(pos);
    if (!state.hasProperty(LivingWoodBlock.Properties.NONLIVING)) return;

    boolean stateChanged = false;
    if (LivingWoodBlock.isSingleton(state)) {
      state = state
          .setValue(LivingWoodBlock.Properties.SINGLETON, false)
          .setValue(LivingWoodBlock.Properties.IS_TRUNK, nowAlive);
      stateChanged = true;
    }
    if (LivingWoodBlock.isNonliving(state) == nowAlive) {
      state = state.setValue(LivingWoodBlock.Properties.NONLIVING, !nowAlive);
      stateChanged = true;
    }

    if (stateChanged) level.setBlockAndUpdate(pos, state);
  }

  public static void updateIsTrunk(ServerLevel level, BlockPos pos, boolean isTrunk) {
    BlockState state = level.getBlockState(pos);
    if (isTrunk(state) == isTrunk) return;

    level.setBlockAndUpdate(pos, state.setValue(Properties.IS_TRUNK, isTrunk));
  }

  public static void resetSingleton(ServerLevel level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (isSingleton(state)) return;

    level.setBlockAndUpdate(
        pos,
        state
            .setValue(Properties.SINGLETON, true)
            .setValue(Properties.IS_TRUNK, false)
    );
  }

  public static class Properties {
    public static final BooleanProperty NONLIVING = BooleanProperty.create("nonliving");
    public static final BooleanProperty SINGLETON = BooleanProperty.create("singleton");
    public static final BooleanProperty IS_TRUNK = BooleanProperty.create("is_trunk");
  }

}
