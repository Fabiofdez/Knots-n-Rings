package fabiofdez.knots_and_rings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;

public class KnotsAndRingsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModContainer container = FabricLoader
        .getInstance()
        .getModContainer(KnotsAndRings.MOD_ID)
        .orElseThrow();

    ResourceManagerHelper.registerBuiltinResourcePack(
        KnotsAndRings.id("knots_and_rings_resources"),
        container,
        Component.literal("Wood Connected Textures"),
        ResourcePackActivationType.DEFAULT_ENABLED
    );
  }
}