package qa.luffy.init;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import qa.luffy.pseudo.item.PseudoItems;

public class PseudoTiers {
    public static final ForgeTier MOLE = new ForgeTier(2, 400, 6, 3, 10,
            BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.FLINT));

    public static final ForgeTier MESH = new ForgeTier(3, 1000, 9, 4, 12,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(PseudoItems.MESH_HARDENING.get()));
}
