package fabiofdez.knots_and_rings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LivingWoodBlock {
  public static boolean isNaturalWood(BlockState state) {
    if (!state.hasProperty(Properties.ALIVE)) return false;

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
    if (!isNaturalWood(state)) return false;

    return neighborsOf(level, pos).any((neighbor, neighborPos) -> {
      if (isNaturalLeaves(neighbor)) {
        return true;
      }

      if (isNaturalWood(neighbor)) {
        return isAlive(neighbor);
      }

      return false;
    });
  }

  public static boolean isTrunkNearby(BlockState state, ServerLevel level, BlockPos pos) {
    if (!isNaturalWood(state)) return false;

    return neighborsOf(level, pos).any((neighbor, neighborPos) -> {
      if (isNaturalLeaves(neighbor)) {
        return true;
      }

      if (isNaturalWood(neighbor)) {
        return isTrunk(neighbor);
      }

      return false;
    });
  }

  public static boolean isAlive(BlockState state) {
    return state.getValue(Properties.ALIVE);
  }

  public static boolean isSingleton(BlockState state) {
    return state.getValue(Properties.SINGLETON);
  }

  public static boolean isTrunk(BlockState state) {
    return state.getValue(Properties.IS_TRUNK);
  }

  public static NeighborIterable neighborsOf(LevelReader level, BlockPos pos) {
    return new NeighborIterable(level, pos);
  }

  public static void updateState(ServerLevel level, BlockPos pos, boolean nowAlive) {
    BlockState state = level.getBlockState(pos);
    if (!state.hasProperty(Properties.ALIVE)) return;

    boolean stateChanged = false;
    if (isSingleton(state)) {
      state = state
          .setValue(Properties.SINGLETON, false)
          .setValue(Properties.IS_TRUNK, nowAlive);
      stateChanged = true;
    }
    if (isAlive(state) != nowAlive) {
      state = state.setValue(Properties.ALIVE, nowAlive);
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
    public static final BooleanProperty ALIVE = BooleanProperty.create("alive");
    public static final BooleanProperty SINGLETON = BooleanProperty.create("singleton");
    public static final BooleanProperty IS_TRUNK = BooleanProperty.create("is_trunk");
  }

}
