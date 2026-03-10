package fabiofdez.knots_and_rings.mixin;

import fabiofdez.knots_and_rings.util.LivingWoodBlock;
import fabiofdez.knots_and_rings.util.LogConnectivityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class DestroyBlockMixin {
  @Inject(method = "destroy", at = @At("TAIL"))
  protected void LivingWood$blockDestroy(LevelAccessor levelAccessor, BlockPos pos, BlockState state, CallbackInfo ci) {
    if (!state.hasProperty(LivingWoodBlock.Properties.NONLIVING)) return;
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    if (LogConnectivityCache.checkCached(pos) != null) {
      LogConnectivityCache.invalidateAttachedTo(levelAccessor.getChunk(pos), pos);
    }
  }
}
