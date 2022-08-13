package qa.luffy.pseudo.item;

import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import qa.luffy.pseudo.PseudoTooltips;

import java.util.List;

public class WindKnotsItem extends Item {

    public WindKnotsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var range = 5;
        var entities = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range, 0.5, range));
        var items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range, 0.5, range));
        var projectiles = level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(range, 0.5, range));
        var throwable = level.getEntitiesOfClass(ThrowableProjectile.class, player.getBoundingBox().inflate(range, range, range));
        for (var livingEntity : entities) {
            if (livingEntity == player){
                if (player.isShiftKeyDown() & !player.isOnGround()){
                    player.push(0, 5, 0);
                }
            }
            if (livingEntity != player && !player.isAlliedTo(livingEntity)) {
                this.gust(4f, player, livingEntity);
            }
        }
        for (var itemEntity : items) {
            this.gust(4f, player, itemEntity);
        }
        for (var projectileEntity : projectiles) {
            this.gust(4f, player, projectileEntity);
        }
        for (var throwableEntity : throwable) {
            throwableEntity.push(0, 3, 0);
            this.gust(4f, player, throwableEntity);
        }
        player.getItemInHand(hand).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
        level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.HORSE_BREATHE, SoundSource.WEATHER, 2F, 0.4F);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        var copy = stack.copy();

        copy.setCount(1);

        copy.setDamageValue(stack.getDamageValue() + 1);

        if (copy.getDamageValue() > stack.getMaxDamage())
            return ItemStack.EMPTY;

        return copy;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag advanced) {
        var damage = stack.getMaxDamage() - stack.getDamageValue();

        if (damage == 1) {
            tooltip.add(PseudoTooltips.ONE_USE_LEFT.build());
        } else {
            tooltip.add(PseudoTooltips.USES_LEFT.args(damage).build());
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void gust(Float strength, Player player, Entity entity){
        var ratioX = Mth.sin(player.getYRot() * 0.017453292F);
        var ratioZ = -Mth.cos(player.getYRot() * 0.017453292F);
        Vec3 vec3 = entity.getDeltaMovement();
        Vec3 vec31 = (new Vec3(ratioX, 0.4D, ratioZ)).normalize().scale(strength);
        entity.setDeltaMovement(vec3.x / 2.0D - vec31.x, entity.isOnGround() ? Math.min(2D, vec3.y / 2.0D + strength) : vec3.y, vec3.z / 2.0D - vec31.z);
    }
}
