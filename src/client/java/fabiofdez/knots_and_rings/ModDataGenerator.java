package fabiofdez.knots_and_rings;

import fabiofdez.knots_and_rings.datagen.ModLangProvider;
import fabiofdez.knots_and_rings.datagen.SoundEventProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ModDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
    FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

    pack.addProvider(SoundEventProvider::new);
    pack.addProvider(ModLangProvider::new);
  }
}
