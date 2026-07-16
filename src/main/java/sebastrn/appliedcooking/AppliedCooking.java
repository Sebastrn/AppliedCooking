package sebastrn.appliedcooking;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import sebastrn.appliedcooking.compat.Compat;
import sebastrn.appliedcooking.compat.theoneprobe.TheOneProbeAddon;
import sebastrn.appliedcooking.config.ServerConfig;
import sebastrn.appliedcooking.setup.CommonSetup;

@Mod(AppliedCooking.ID)
public final class AppliedCooking {
    public static final String ID = "appliedcooking";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public AppliedCooking(IEventBus modEventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        AppliedCookingBlocks.register(modEventBus);
        AppliedCookingItems.register(modEventBus);
        AppliedCookingBlockEntities.register(modEventBus);
        AppliedCookingLootFunctions.register(modEventBus);
        AppliedCookingCreativeTab.register(modEventBus);

        modEventBus.addListener(CommonSetup::onCommonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::enqueueIMC);
    }

    /**
     * Exposes the Kitchen Station's {@link KitchenItemProvider} to Cooking for Blockheads. On NeoForge the
     * Balm {@code getProviders()} declaration alone is not enough — the block entity must be registered
     * against CFB's block capability here. The capability is obtained from Balm, where CFB registered it.
     */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        @SuppressWarnings("unchecked")
        BlockCapability<KitchenItemProvider, Void> kitchenItemProvider =
                (BlockCapability<KitchenItemProvider, Void>) ((NeoForgeBalmProviders) Balm.getProviders())
                        .getBlockCapability(KitchenItemProvider.class);

        event.registerBlockEntity(kitchenItemProvider, AppliedCookingBlockEntities.KITCHEN_STATION.get(),
                (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }
}
