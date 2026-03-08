package fabiofdez.livingwood;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;

public class LivingWoodClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModContainer container = FabricLoader
        .getInstance()
        .getModContainer(LivingWood.MOD_ID)
        .orElseThrow();

    ResourceManagerHelper.registerBuiltinResourcePack(
        LivingWood.id("knots-and-rings"),
        container,
        Component.literal("Wood Connected Texture Features"),
        ResourcePackActivationType.DEFAULT_ENABLED
    );
  }
}