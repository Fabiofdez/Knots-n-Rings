package fabiofdez.livingwood.mixin;

import fabiofdez.livingwood.util.LivingWoodBlock;
import fabiofdez.livingwood.util.LivingWoodCluster;
import fabiofdez.livingwood.util.LogConnectivityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class LogBehaviourMixin {
  @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
  private void LivingWood$isRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> cir) {
    if (!state.hasProperty(LivingWoodBlock.Properties.ALIVE)) return;

    if (LivingWoodBlock.isStripped(state) && !state.getValue(LivingWoodBlock.Properties.ALIVE)) {
      cir.setReturnValue(false);
    } else if (LivingWoodBlock.isWood(state)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "randomTick", at = @At("HEAD"))
  private void LivingWood$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource tickSrc, CallbackInfo ci) {
    if (!state.hasProperty(LivingWoodBlock.Properties.ALIVE)) return;
    if (!LivingWoodBlock.isWood(state)) return;

    if (LivingWoodBlock.isStripped(state) && state.getValue(LivingWoodBlock.Properties.ALIVE)) {
      level.setBlockAndUpdate(pos, state.setValue(LivingWoodBlock.Properties.ALIVE, false));
      return;
    }

    if (LogConnectivityCache.exploring(pos)) return;
    Boolean cachedAlive = LogConnectivityCache.checkCached(pos);
    if (cachedAlive != null) {
      level.setBlockAndUpdate(pos, state.setValue(LivingWoodBlock.Properties.ALIVE, cachedAlive));
      return;
    }

    LivingWoodCluster.revivePathOrDecay(level, pos);
  }

  @Inject(method = "neighborChanged", at = @At("TAIL"))
  protected void LivingWood$neighborChanged(BlockState state, Level level, BlockPos pos, Block _b, Orientation _o, boolean _bl, CallbackInfo ci) {
    if (level.isClientSide) return;

    if (!state.hasProperty(LivingWoodBlock.Properties.ALIVE)) return;
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    LogConnectivityCache.invalidateAttachedTo(level.getChunkAt(pos), pos);
    if (LogConnectivityCache.exploring(pos)) return;

    LivingWoodCluster.revivePathOrDecay((ServerLevel) level, pos);
  }
}
