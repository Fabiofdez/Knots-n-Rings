package fabiofdez.livingwood.mixin;

import fabiofdez.livingwood.util.LivingWoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RotatedPillarBlock.class)
public abstract class LogBlockMixin extends Block {
  public LogBlockMixin(Properties properties) {
    super(properties);
  }

  @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
  private void LivingWood$defineBlockState(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(LivingWoodBlock.Properties.ALIVE);
  }

  @Inject(method = "getStateForPlacement", at = @At("TAIL"), cancellable = true)
  protected void LivingWood$getStateForPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
    Level level = ctx.getLevel();
    if (level.isClientSide) return;

    BlockState state = cir.getReturnValue();
    if (!LivingWoodBlock.isWood(state)) return;
    if (LivingWoodBlock.isStripped(state)) {
      cir.setReturnValue(state.setValue(LivingWoodBlock.Properties.ALIVE, false));
      return;
    }

    BlockPos pos = ctx.getClickedPos();
    boolean alive = LivingWoodBlock.isAlive((ServerLevel) level, pos);
    cir.setReturnValue(state.setValue(LivingWoodBlock.Properties.ALIVE, alive));
  }
}
