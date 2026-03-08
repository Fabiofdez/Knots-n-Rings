package fabiofdez.livingwood;

import fabiofdez.livingwood.util.LogConnectivityCache;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivingWood implements ModInitializer {
  public static final String MOD_ID = "livingwood";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Knots & Rings");

    ServerChunkEvents.CHUNK_UNLOAD.register(LogConnectivityCache::invalidateInChunk);
  }

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  }
}