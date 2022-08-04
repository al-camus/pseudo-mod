package qa.luffy.pseudo.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import qa.luffy.pseudo.Pseudo;
import qa.luffy.pseudo.PseudoCreativeTab;
import qa.luffy.pseudo.item.PseudoItems;

import java.util.function.Supplier;

public class PseudoBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Pseudo.MODID);

    public static final RegistryObject<Block> GRAPHITE_BLOCK = registerBlock("graphite_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(4f).requiresCorrectToolForDrops()), PseudoCreativeTab.PSEUDO_TAB);

    public static final RegistryObject<Block> MESH_BLOCK = registerBlock("mesh_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(2f, 100000f)), PseudoCreativeTab.PSEUDO_TAB);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab){
        return PseudoItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
