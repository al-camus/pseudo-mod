package qa.luffy.pseudo.item;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Vanishable;
import qa.luffy.pseudo.init.PseudoTags;

public class DiggingMittsItem extends DiggerItem implements Vanishable {
    public DiggingMittsItem(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pAttackDamageModifier, pAttackSpeedModifier, pTier, PseudoTags.Blocks.MITTS_MINEABLE, pProperties);
    }
}