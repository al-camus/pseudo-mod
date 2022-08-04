package qa.luffy.pseudo;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import qa.luffy.pseudo.item.PseudoItems;

public class PseudoCreativeTab {
    public static final CreativeModeTab PSEUDO_TAB = new CreativeModeTab("pseudotab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(PseudoItems.GRAPHITE.get());
        }
    };
}
