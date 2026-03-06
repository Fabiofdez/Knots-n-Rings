package fabiofdez.livingwood.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class LogConnectivityCache {
  private static int clusterCounter = 1;

  private static final Set<BlockPos> currentlyExploring = new HashSet<>();
  private static final Map<LevelChunk, Set<Integer>> clusterByChunk = new HashMap<>();
  private static final Map<BlockPos, Integer> clusterByPos = new HashMap<>();
  private static final Map<Integer, Boolean> clusterAlive = new HashMap<>();
  private static final Map<Integer, Set<BlockPos>> clusterMembers = new HashMap<>();

  public static void addToExploring(BlockPos pos) {
    currentlyExploring.add(pos);
  }

  public static void forgetExplored(BlockPos pos) {
    currentlyExploring.remove(pos);
  }

  public static void forgetPathExplored(List<BlockPos> path) {
    if (path == null) return;

    for (BlockPos pos : path) {
      forgetExplored(pos);
    }
  }

  public static boolean exploring(BlockPos pos) {
    return currentlyExploring.contains(pos);
  }

  public static Boolean checkCached(BlockPos pos) {
    Integer clusterId = clusterByPos.get(pos);
    if (clusterId != null) {
      return clusterAlive.get(clusterId);
    }
    return null;
  }

  public static void cacheCluster(LevelChunk chunk, Set<BlockPos> cluster, boolean alive) {
    int clusterId = clusterCounter++;
    clusterAlive.put(clusterId, alive);

    Set<Integer> clustersAtChunk = clusterByChunk.get(chunk);
    if (clustersAtChunk == null) {
      clusterByChunk.put(chunk, new HashSet<>(List.of(clusterId)));
    } else {
      clustersAtChunk.add(clusterId);
    }

    for (BlockPos pos : cluster) {
      clusterByPos.put(pos.immutable(), clusterId);
    }
    clusterMembers.put(clusterId, new HashSet<>(cluster));
  }

  public static void invalidateAttachedTo(LevelChunk chunk, BlockPos origin) {
    Integer clusterId = clusterByPos.remove(origin);
    if (clusterId == null) return;

    Set<Integer> clustersAtChunk = clusterByChunk.get(chunk);
    if (clustersAtChunk != null) {
      clustersAtChunk.remove(clusterId);
    }

    invalidateClusterById(clusterId);
  }

  public static void invalidateInChunk(ServerLevel ignored, LevelChunk chunk) {
    Set<Integer> clustersAtChunk = clusterByChunk.remove(chunk);
    if (clustersAtChunk == null) return;

    for (Integer clusterId : clustersAtChunk) {
      invalidateClusterById(clusterId);
    }
  }

  private static void invalidateClusterById(Integer clusterId) {
    if (clusterId == null) return;

    clusterAlive.remove(clusterId);
    Set<BlockPos> attachedBlocks = clusterMembers.remove(clusterId);
    if (attachedBlocks == null) return;

    for (BlockPos pos : attachedBlocks) {
      clusterByPos.remove(pos);
    }
  }
}
