package qa.luffy.pseudo.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.block.Block;
import qa.luffy.init.PseudoTags;

public class DiggingMittsItem extends DiggerItem implements Vanishable {
    public DiggingMittsItem(Tier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pAttackDamageModifier, pAttackSpeedModifier, pTier, PseudoTags.Blocks.MITTS_MINEABLE, pProperties);
    }
}