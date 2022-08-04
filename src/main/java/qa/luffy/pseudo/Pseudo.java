package qa.luffy.pseudo;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import qa.luffy.pseudo.block.PseudoBlocks;
import qa.luffy.pseudo.item.PseudoItems;

@Mod(Pseudo.MODID)
public class Pseudo {
    public static final String MODID = "pseudo";

    public Pseudo()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        PseudoItems.register(modEventBus);

        PseudoBlocks.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }


    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){

        }
    }
}
