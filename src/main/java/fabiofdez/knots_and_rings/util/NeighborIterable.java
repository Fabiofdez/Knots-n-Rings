package fabiofdez.knots_and_rings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class NeighborIterable {
  private final ServerLevel level;
  private final BlockPos pos;

  public NeighborIterable(ServerLevel level, BlockPos pos) {
    this.level = level;
    this.pos = pos;
  }

  public boolean any(NeighborPredicate predicate) {
    // iterate in layers vertically, starting from top
    for (int dy = 1; dy >= -1; dy--) {
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx == 0 && dy == 0 && dz == 0) continue;

          BlockPos neighborPos = this.pos.offset(dx, dy, dz);
          BlockState neighbor = this.level.getBlockState(neighborPos);

          if (predicate.eval(neighbor, neighborPos)) return true;
        }
      }
    }

    return false;
  }

  @FunctionalInterface
  public interface NeighborPredicate {
    boolean eval(BlockState state, BlockPos pos);
  }
}
