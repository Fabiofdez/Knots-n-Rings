package fabiofdez.knots_and_rings.datagen;

import fabiofdez.knots_and_rings.ModSounds;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SoundEventProvider extends FabricSoundsProvider {
  public SoundEventProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  protected void configure(HolderLookup.Provider provider, SoundExporter soundExporter) {
    soundExporter.add(
        ModSounds.SPLIT_WOOD,
        blockSound(ModSounds.SPLIT_WOOD).sound(ofEvent(SoundEvents.AXE_STRIP).volume(0.8F))
    );
    soundExporter.add(
        ModSounds.CRACK_WOOD,
        blockSound(ModSounds.CRACK_WOOD).sound(ofEvent(SoundEvents.BAMBOO_HIT).volume(0.4F))
    );

    soundExporter.add(
        ModSounds.HEAL_WOOD,
        blockSound(ModSounds.HEAL_WOOD).sound(ofEvent(SoundEvents.MOSS_PLACE).volume(0.8F))
    );
    soundExporter.add(
        ModSounds.HEAL_WOOD_ALT,
        blockSound(ModSounds.HEAL_WOOD_ALT).sound(ofEvent(SoundEvents.COMPOSTER_FILL_SUCCESS).volume(0.4F))
    );
  }

  private static SoundTypeBuilder blockSound(SoundEvent sound) {
    return SoundTypeBuilder.of().category(SoundSource.BLOCKS).subtitle(sound.location().toLanguageKey("subtitles"));
  }

  private static SoundTypeBuilder.EntryBuilder ofEvent(SoundEvent sound) {
    return SoundTypeBuilder.EntryBuilder.ofEvent(sound);
  }

  @Override
  public @NotNull String getName() {
    return "SoundEventProvider";
  }
}
