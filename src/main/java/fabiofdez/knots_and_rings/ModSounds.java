package fabiofdez.knots_and_rings;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
  public static SoundEvent SPLIT_WOOD = registerSound("split_wood");
  public static SoundEvent CRACK_WOOD = registerSound("crack_wood");
  public static SoundEvent HEAL_WOOD = registerSound("heal_wood");
  public static SoundEvent HEAL_WOOD_ALT = registerSound("heal_wood_alt");

  private static SoundEvent registerSound(String id) {
    ResourceLocation soundId = KnotsAndRings.id(id);
    return Registry.register(BuiltInRegistries.SOUND_EVENT, soundId, SoundEvent.createVariableRangeEvent(soundId));
  }

  public static void initialize() {
  }
}
