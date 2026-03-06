package fabiofdez.livingwood.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LivingWoodCluster {
  public static void revivePathOrDecay(ServerLevel level, BlockPos pos) {
    Set<BlockPos> attachedLogs = new HashSet<>();
    List<BlockPos> foundPath = LivingWoodCluster.findPathToLeaves(level, pos, attachedLogs);

    if (foundPath != null) {
      LivingWoodCluster.revivePath(level, foundPath);
    } else {
      LivingWoodCluster.decay(level, attachedLogs);
    }
  }

  public static List<BlockPos> findPathToLeaves(ServerLevel level, BlockPos start, Set<BlockPos> cluster) {
    Queue<BlockPos> queue = new ArrayDeque<>();
    Map<BlockPos, BlockPos> pathTrace = new HashMap<>();
    AtomicReference<List<BlockPos>> foundPath = new AtomicReference<>(null);

    queue.add(start);
    pathTrace.put(start, null);
    LogConnectivityCache.addToExploring(start);

    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      if (!cluster.add(current)) continue; // already visited

      boolean found = LivingWoodBlock
          .neighborsOf(level, current)
          .sustainLife((neighbor, neighborPos) -> {
            if (LivingWoodBlock.isNaturalLeaves(neighbor)) {
              foundPath.set(buildTracedPath(pathTrace, current));
              return true;
            }

            if (LivingWoodBlock.isNaturalWood(neighbor) && !pathTrace.containsKey(neighborPos)) {
              queue.add(neighborPos);
              pathTrace.put(neighborPos, current);
              LogConnectivityCache.addToExploring(start);
            }

            return false;
          });

      if (found) break;
    }
    List<BlockPos> resolvedPath = foundPath.get();

    List<BlockPos> pathToForget = Objects.requireNonNullElseGet(resolvedPath, () -> List.copyOf(cluster));
    LogConnectivityCache.forgetPathExplored(pathToForget);
    LogConnectivityCache.cacheCluster(level.getChunkAt(start), cluster, resolvedPath != null);

    return resolvedPath;
  }

  private static List<BlockPos> buildTracedPath(Map<BlockPos, BlockPos> pathTrace, BlockPos current) {
    List<BlockPos> path = new ArrayList<>();
    BlockPos backtrack = current;

    while (backtrack != null) {
      path.add(backtrack);
      backtrack = pathTrace.get(backtrack);
    }

    return path;
  }

  public static void revivePath(ServerLevel level, List<BlockPos> path) {
    for (BlockPos pos : path) {
      BlockState state = level.getBlockState(pos);
      if (!state.hasProperty(LivingWoodBlock.Properties.ALIVE)) continue;
      if (state.getValue(LivingWoodBlock.Properties.ALIVE)) continue;
      level.setBlockAndUpdate(pos, state.setValue(LivingWoodBlock.Properties.ALIVE, true));
    }
  }

  public static void decay(ServerLevel level, Set<BlockPos> cluster) {
    for (BlockPos pos : cluster) {
      BlockState state = level.getBlockState(pos);
      if (!state.hasProperty(LivingWoodBlock.Properties.ALIVE)) continue;
      if (!state.getValue(LivingWoodBlock.Properties.ALIVE)) continue;
      level.setBlockAndUpdate(pos, state.setValue(LivingWoodBlock.Properties.ALIVE, false));
    }
  }
}
