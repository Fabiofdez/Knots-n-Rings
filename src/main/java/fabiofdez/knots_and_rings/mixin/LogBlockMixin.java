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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RotatedPillarBlock.class)
public class LogBlockMixin extends Block implements BonemealableBlock {
  @Unique
  private static final BooleanProperty ALIVE;
  @Unique
  private static final BooleanProperty IS_TRUNK;
  @Unique
  private static final BooleanProperty SINGLETON;

  public LogBlockMixin(Properties properties) {
    super(properties);
  }

  @Inject(method = "<init>", at = @At("TAIL"))
  protected void LivingWood$init(Properties properties, CallbackInfo ci) {
    if (!this
        .defaultBlockState()
        .hasProperty(ALIVE)) return;

    this.registerDefaultState(this
        .defaultBlockState()
        .setValue(ALIVE, false)
        .setValue(IS_TRUNK, false)
        .setValue(SINGLETON, true));
  }

  @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
  protected void LivingWood$defineBlockState(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(ALIVE, SINGLETON, IS_TRUNK);
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
        .setValue(ALIVE, alive)
        .setValue(IS_TRUNK, isTrunk)
        .setValue(SINGLETON, false));
  }

  @Override
  protected boolean isRandomlyTicking(BlockState state) {
    boolean defaultTicking = super.isRandomlyTicking(state);
    if (!LivingWoodBlock.isNaturalWood(state)) return defaultTicking;

    return true;
  }

  @Override
  protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource tickSrc) {
    super.randomTick(state, level, pos, tickSrc);
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    if (LogConnectivityCache.exploring(pos)) return;
    Boolean cachedAlive = LogConnectivityCache.checkCached(pos);
    if (cachedAlive == null) {
      LivingWoodCluster.attemptRevivePath(level, pos);
    } else {
      LivingWoodBlock.updateState(level, pos, cachedAlive);
    }

    if (LivingWoodBlock.isTrunkNearby(state, level, pos)) {
      LivingWoodBlock.updateIsTrunk(level, pos, true);
    }
  }

  @Override
  protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    if (!LivingWoodBlock.isNaturalWood(state)) return InteractionResult.PASS;

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
      return InteractionResult.SUCCESS;

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
      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }

  @Override
  public void destroy(LevelAccessor levelAccessor, BlockPos pos, BlockState state) {
    super.destroy(levelAccessor, pos, state);
    if (!LivingWoodBlock.isNaturalWood(state)) return;

    if (LogConnectivityCache.checkCached(pos) != null) {
      LogConnectivityCache.invalidateAttachedTo(levelAccessor.getChunk(pos), pos);
    }
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

  static {
    ALIVE = LivingWoodBlock.Properties.ALIVE;
    IS_TRUNK = LivingWoodBlock.Properties.IS_TRUNK;
    SINGLETON = LivingWoodBlock.Properties.SINGLETON;
  }
}
