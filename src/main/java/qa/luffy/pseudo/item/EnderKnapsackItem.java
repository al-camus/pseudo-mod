package qa.luffy.pseudo.item;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import qa.luffy.init.PseudoTags;

public class EnderKnapsackItem extends Item {

    public EnderKnapsackItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        openEnderChest(player);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void openEnderChest(Player player) {
        PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
        player.openMenu(new SimpleMenuProvider((syncId, inventory, playerx) -> {
            return ChestMenu.threeRows(syncId, inventory, enderChestInventory);
        }, MutableComponent.create(new TranslatableContents("container.enderchest"))));
    }

}