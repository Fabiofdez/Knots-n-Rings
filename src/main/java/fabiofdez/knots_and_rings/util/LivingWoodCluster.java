package fabiofdez.knots_and_rings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LivingWoodCluster {
  public static void attemptRevivePath(ServerLevel level, BlockPos pos) {
    revivePathOrDecay(level, pos, false);
  }

  public static void revivePathOrDecay(ServerLevel level, BlockPos pos, boolean forceDecay) {
    Set<BlockPos> attachedLogs = new HashSet<>();
    List<BlockPos> foundPath = LivingWoodCluster.findPathToLeaves(level, pos, attachedLogs);

    if (foundPath != null) {
      LivingWoodCluster.revivePath(level, foundPath);
    } else {
      LivingWoodCluster.decay(level, attachedLogs);
      if (forceDecay) {
        attachedLogs.forEach((attached) -> LivingWoodBlock.resetSingleton(level, attached));
      }
    }
  }

  public static List<BlockPos> findPathToLeaves(LevelReader level, BlockPos start, Set<BlockPos> cluster) {
    Queue<BlockPos> queue = new ArrayDeque<>();
    Map<BlockPos, BlockPos> pathTrace = new HashMap<>();
    AtomicReference<List<BlockPos>> foundPath = new AtomicReference<>(null);
    AtomicReference<BlockPos> existingAttachment = new AtomicReference<>(null);

    queue.add(start);
    pathTrace.put(start, null);
    LogConnectivityCache.addToExploring(start);

    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      if (!cluster.add(current)) continue; // already visited

      boolean found = LivingWoodBlock
          .neighborsOf(level, current)
          .any((neighbor, neighborPos) -> {
            if (LivingWoodBlock.isNaturalLeaves(neighbor)) {
              foundPath.set(buildTracedPath(pathTrace, current));
              return true;
            }

            Boolean cachedNeighborAlive = LogConnectivityCache.checkCached(neighborPos);
            if (cachedNeighborAlive != null) {
              if (cachedNeighborAlive) {
                foundPath.set(buildTracedPath(pathTrace, current));
              }
              existingAttachment.set(neighborPos.immutable());
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
    if (existingAttachment.get() != null) {
      if (resolvedPath != null) {
        LogConnectivityCache.attachToCluster(existingAttachment.get(), resolvedPath);
      } else {
        LogConnectivityCache.attachToCluster(existingAttachment.get(), cluster);
      }
    } else {
      LogConnectivityCache.cacheCluster(level.getChunk(start), cluster, resolvedPath != null);
    }

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
      LivingWoodBlock.updateState(level, pos, true);
    }
  }

  public static void decay(ServerLevel level, Set<BlockPos> cluster) {
    for (BlockPos pos : cluster) {
      LivingWoodBlock.updateState(level, pos, false);
    }
  }
}
