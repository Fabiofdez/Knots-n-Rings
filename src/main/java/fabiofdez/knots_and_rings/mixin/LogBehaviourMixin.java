package fabiofdez.knots_and_rings.mixin;

import fabiofdez.knots_and_rings.util.LivingWoodBlock;
import fabiofdez.knots_and_rings.util.LivingWoodCluster;
import fabiofdez.knots_and_rings.util.LogConnectivityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class LogBehaviourMixin {
  @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
  private void LivingWood$isRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> cir) {
    if (!state.hasProperty(LivingWoodBlock.Properties.NONLIVING)) return;
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    cir.setReturnValue(true);
  }

  @Inject(method = "randomTick", at = @At("HEAD"))
  private void LivingWood$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource tickSrc, CallbackInfo ci) {
    if (!state.hasProperty(LivingWoodBlock.Properties.NONLIVING)) return;
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    if (LogConnectivityCache.exploring(pos)) return;
    Boolean cachedAlive = LogConnectivityCache.checkCached(pos);
    if (cachedAlive == null) {
      LivingWoodCluster.revivePathOrDecay(level, pos);
    } else {
      LivingWoodBlock.updateState(level, pos, cachedAlive);
    }

    if (LivingWoodBlock.isTrunkNearby(state, level, pos)) {
      LivingWoodBlock.updateIsTrunk(level, pos, true);
    }
  }

  @Inject(method = "useItemOn", at = @At("TAIL"), cancellable = true)
  protected void LivingWood$useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
    if (!state.hasProperty(LivingWoodBlock.Properties.NONLIVING)) return;
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    if (stack.is(ItemTags.PICKAXES) && LivingWoodBlock.isTrunk(state)) {
      if (level.isClientSide) {
        ParticleUtils.spawnParticlesOnBlockFace(
            level,
            pos,
            new BlockParticleOption(ParticleTypes.BLOCK, state),
            UniformInt.of(10, 15),
            hitResult.getDirection(),
            () -> Vec3.ZERO,
            0.5
        );
      } else {
        LogConnectivityCache.invalidateAttachedTo(level.getChunkAt(pos), pos);
        LivingWoodCluster.revivePathOrDecay((ServerLevel) level, pos, true);
        LivingWoodBlock.updateIsTrunk((ServerLevel) level, pos, false);

        float pitch = 0.8F + level.random.nextFloat() * 0.2F;
        level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.8F, pitch);
        level.playSound(null, pos, SoundEvents.BAMBOO_HIT, SoundSource.BLOCKS, 0.4F, pitch);
        if (!player.isCreative() && stack.isDamageableItem()) stack.setDamageValue(stack.getDamageValue() - 1);
      }
      cir.setReturnValue(InteractionResult.SUCCESS);

    } else if (stack.is(Items.BONE_MEAL) && !LivingWoodBlock.isTrunk(state)) {
      if (level.isClientSide) {
        ParticleUtils.spawnParticlesOnBlockFace(
            level,
            pos,
            ParticleTypes.HAPPY_VILLAGER,
            UniformInt.of(10, 15),
            hitResult.getDirection(),
            () -> Vec3.ZERO,
            0.5
        );
      } else {
        LogConnectivityCache.invalidateAttachedTo(level.getChunkAt(pos), pos);
        LivingWoodBlock.updateIsTrunk((ServerLevel) level, pos, true);

        float pitch = 0.8F + level.random.nextFloat() * 0.2F;
        level.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.BLOCKS, 0.8F, pitch);
        level.playSound(null, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 0.4F, 1.2F);
        stack.consume(1, player);
      }
      cir.setReturnValue(InteractionResult.SUCCESS);
    }
  }
}
