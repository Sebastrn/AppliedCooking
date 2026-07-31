package sebastrn.appliedcooking;

import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
// The One Probe integration is disabled for 26.1.2 (no 26.1 TOP build). Imports kept, commented, for easy re-enable.
// import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
// import sebastrn.appliedcooking.compat.Compat;
// import sebastrn.appliedcooking.compat.theoneprobe.TheOneProbeAddon;
import sebastrn.appliedcooking.config.ServerConfig;
import sebastrn.appliedcooking.setup.CommonSetup;

@Mod(AppliedCooking.ID)
public final class AppliedCooking {
    public static final String ID = "appliedcooking";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public AppliedCooking(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        AppliedCookingBlocks.register(modEventBus);
        AppliedCookingItems.register(modEventBus);
        AppliedCookingBlockEntities.register(modEventBus);
        AppliedCookingLootFunctions.register(modEventBus);
        AppliedCookingCreativeTab.register(modEventBus);

        modEventBus.addListener(CommonSetup::onCommonSetup);
        modEventBus.addListener(this::registerCapabilities);
        // modEventBus.addListener(this::enqueueIMC);  // TOP integration disabled for 26.1.2
    }

    /**
     * Exposes the Kitchen Station's {@link KitchenItemProvider} to Cooking for Blockheads. Balm 26.1 dropped the
     * old {@code BalmBlockEntity#getProviders()} route; CFB now registers its {@code kitchen_item_provider}
     * capability through Balm's capability system, backed by a plain NeoForge {@link BlockCapability}. Those are
     * singletons keyed by name+type+context, so re-creating CFB's exact capability here returns the very object
     * CFB's multiblock scanner looks up, and does so independently of CFB's own static-init ordering.
     */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockCapability<KitchenItemProvider, Void> kitchenItemProvider = BlockCapability.create(
                Identifier.fromNamespaceAndPath("cookingforblockheads", "kitchen_item_provider"),
                KitchenItemProvider.class, Void.class);

        event.registerBlockEntity(kitchenItemProvider, AppliedCookingBlockEntities.KITCHEN_STATION.get(),
                (blockEntity, context) -> blockEntity.getKitchenItemProvider());
    }

    // The One Probe integration, disabled for 26.1.2 (no 26.1 TOP build). Re-enable together with the imports,
    // the addListener call above, and TheOneProbeAddon when McJty ships a 26.1 TOP.
    // private void enqueueIMC(InterModEnqueueEvent event) {
    //     if (Balm.isModLoaded(Compat.THEONEPROBE)) {
    //         TheOneProbeAddon.register();
    //     }
    // }
}
