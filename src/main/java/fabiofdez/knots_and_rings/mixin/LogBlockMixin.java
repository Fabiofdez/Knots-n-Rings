package fabiofdez.knots_and_rings.mixin;

import fabiofdez.knots_and_rings.util.LivingWoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RotatedPillarBlock.class)
public class LogBlockMixin implements BonemealableBlock {
  @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
  private void LivingWood$defineBlockState(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(
        LivingWoodBlock.Properties.NONLIVING,
        LivingWoodBlock.Properties.SINGLETON,
        LivingWoodBlock.Properties.IS_TRUNK
    );
  }

  @Inject(method = "getStateForPlacement", at = @At("TAIL"), cancellable = true)
  protected void LivingWood$getStateForPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
    Level level = ctx.getLevel();
    if (level.isClientSide) return;

    BlockState state = cir.getReturnValue();
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    Block placedBlock = state.getBlock();
    ItemStack itemInHand = ctx.getItemInHand();
    if (!itemInHand.is(placedBlock.asItem())) return;

    BlockPos pos = ctx.getClickedPos();
    boolean alive = LivingWoodBlock.isAliveNearby(state, (ServerLevel) level, pos);
    boolean isTrunk = LivingWoodBlock.isTrunkNearby(state, (ServerLevel) level, pos);

    cir.setReturnValue(state
        .setValue(LivingWoodBlock.Properties.NONLIVING, !alive)
        .setValue(LivingWoodBlock.Properties.SINGLETON, false)
        .setValue(LivingWoodBlock.Properties.IS_TRUNK, isTrunk));
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos pos, BlockState state) {
    return LivingWoodBlock.isNaturalWood(state) && !LivingWoodBlock.isTrunk(state);
  }

  @Override
  public boolean isBonemealSuccess(Level level, RandomSource src, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel level, RandomSource src, BlockPos pos, BlockState state) {
  }

  @Override
  public @NotNull BlockPos getParticlePos(BlockPos pos) {
    return pos.above();
  }
}
