package qa.luffy.pseudo.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import qa.luffy.pseudo.Pseudo;
import qa.luffy.pseudo.event.ScytheHarvestCropEvent;
import qa.luffy.pseudo.helper.BlockHelper;
import qa.luffy.pseudo.init.PseudoTags;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ScytheItem extends SwordItem {
    private static final Method GET_SEED;

    private static float attackDamage;
    private static float attackSpeed;
    private static int range;

    static {
        GET_SEED = ObfuscationReflectionHelper.findMethod(CropBlock.class, "m_6404_");
    }

    public ScytheItem(int attackDamage, float attackSpeed,  int range, Tier tier, Function<Properties, Properties> properties) {
        super(tier, attackDamage, attackSpeed, properties.apply(new Properties()));
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.range = range;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
            super.fillItemCategory(group, items);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        var pos = context.getClickedPos();
        var hand = context.getHand();
        var face = context.getClickedFace();
        var stack = player.getItemInHand(hand);

        if (!player.mayUseItemAt(pos.relative(face), face, stack))
            return InteractionResult.FAIL;

        var level = context.getLevel();

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        BlockPos.betweenClosed(pos.offset(-this.range, 0, -this.range), pos.offset(this.range, 0, this.range)).forEach(aoePos -> {
            if (stack.isEmpty())
                return;

            var state = level.getBlockState(aoePos);
            var event = new ScytheHarvestCropEvent(level, aoePos, state, stack, player);

            if (MinecraftForge.EVENT_BUS.post(event))
                return;

            var block = state.getBlock();

            if (block instanceof CropBlock crop) {
                Item seed = getSeed(crop);

                if (crop.isMaxAge(state) && seed != null) {
                    var drops = Block.getDrops(state, (ServerLevel) level, aoePos, level.getBlockEntity(aoePos));

                    for (var drop : drops) {
                        var item = drop.getItem();

                        if (!drop.isEmpty() && item == seed) {
                            drop.shrink(1);
                            break;
                        }
                    }

                    for (var drop : drops) {
                        if (!drop.isEmpty()) {
                            Block.popResource(level, aoePos, drop);
                        }
                    }

                    stack.hurtAndBreak(1, player, entity -> {
                        entity.broadcastBreakEvent(player.getUsedItemHand());
                    });

                    level.setBlockAndUpdate(aoePos, crop.getStateForAge(0));
                }
            }
        });

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.getAttackStrengthScale(0.5F) >= 0.95F) {
            var level = player.level;
            var range = (this.range >= 2 ? 1.0D + (this.range - 1) * 0.25D : 1.0D);
            var entities = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(range, 0.25D, range));

            for (var aoeEntity : entities) {
                if (aoeEntity != player && aoeEntity != entity && !player.isAlliedTo(entity)) {
                    var source = DamageSource.playerAttack(player);
                    var attackDamage = this.getAttackDamage() * 0.67F;
                    var success = ForgeHooks.onLivingAttack(aoeEntity, source, attackDamage);

                    if (success) {
                        aoeEntity.knockback(0.4F, Mth.sin(player.getYRot() * 0.017453292F), -Mth.cos(player.getYRot() * 0.017453292F));
                        aoeEntity.hurt(source, attackDamage);
                    }
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);

            player.sweepAttack();
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    public float getAttackDamage() {
        return this.attackDamage + this.getTier().getAttackDamageBonus();
    }

    public float getAttackSpeed() {
        return this.attackSpeed;
    }

    private static Item getSeed(Block block) {
        try {
            return (Item) GET_SEED.invoke(block);
        } catch (Exception e) {
            Pseudo.LOGGER.error("Unable to get seed from crop {}", e.getLocalizedMessage());
        }

        return null;
    }


    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isValidMaterial(state) ? this.getTier().getSpeed() / 2 : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isValidMaterial(state);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return !this.harvest(stack, player.level, pos, player);
    }

    private boolean harvest(ItemStack stack, Level level, BlockPos pos, Player player) {
        var state = level.getBlockState(pos);
        var hardness = state.getDestroySpeed(level, pos);

        if (!this.tryHarvest(level, pos, false, stack, player) || !isValidMaterial(state))
            return false;

        if (this.range > 0) {
            BlockPos.betweenClosed(pos.offset(-this.range, -this.range, -this.range), pos.offset(this.range, this.range, this.range)).forEach(aoePos -> {
                if (aoePos != pos) {
                    var aoeState = level.getBlockState(aoePos);
                    var aoeHardness = aoeState.getDestroySpeed(level, aoePos);

                    if (aoeHardness <= hardness + 5.0F && isValidMaterial(aoeState)) {
                        if (this.tryHarvest(level, aoePos, true, stack, player)) {
                            if (aoeHardness <= 0.0F && Math.random() < 0.33) {
                                if (!player.getAbilities().instabuild) {
                                    stack.hurtAndBreak(1, player, entity -> {
                                        entity.broadcastBreakEvent(player.getUsedItemHand());
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }

        return true;
    }

    private boolean tryHarvest(Level level, BlockPos pos, boolean extra, ItemStack stack, Player player) {
        var state = level.getBlockState(pos);
        var hardness = state.getDestroySpeed(level, pos);
        var harvest = !extra || (ForgeHooks.isCorrectToolForDrops(state, player) || this.isCorrectToolForDrops(stack, state));

        if (hardness >= 0.0F && harvest)
            return BlockHelper.breakBlocksAOE(stack, level, player, pos, !extra);

        return false;
    }

    private static boolean isValidMaterial(BlockState state) {
        var material = state.getMaterial();
        return state.is(PseudoTags.Blocks.SCYTHE_MINEABLE) || material == Material.LEAVES || material == Material.PLANT || material == Material.REPLACEABLE_PLANT;
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState p_150859_) {
        return (p_238241_) -> {
            p_238241_.getLevel().setBlock(p_238241_.getClickedPos(), p_150859_, 11);
            p_238241_.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, p_238241_.getClickedPos(), GameEvent.Context.of(p_238241_.getPlayer(), p_150859_));
        };
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction);
    }

}
