package fabiofdez.knots_and_rings.datagen;

import fabiofdez.knots_and_rings.ModSounds;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModLangProvider extends FabricLanguageProvider {
  public ModLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
    super(dataOutput, registryLookup);
  }

  @Override
  public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
    translationBuilder.add(ModSounds.SPLIT_WOOD, "Wood splitting");
    translationBuilder.add(ModSounds.CRACK_WOOD, "Wood cracking");
    translationBuilder.add(ModSounds.HEAL_WOOD, "Wood healing");
    translationBuilder.add(ModSounds.HEAL_WOOD_ALT, "Wood healing");
  }
}
