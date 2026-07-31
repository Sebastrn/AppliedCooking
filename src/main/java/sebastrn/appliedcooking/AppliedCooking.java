package sebastrn.appliedcooking;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.block.entity.ModBlockEntities;
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
     * Balm {@code getProviders()} declaration alone is not enough, the block entity must be registered
     * against CFB's block capability here. The capability is obtained from Balm, where CFB registered it.
     */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        @SuppressWarnings("unchecked")
        BlockCapability<KitchenItemProvider, Void> kitchenItemProvider =
                (BlockCapability<KitchenItemProvider, Void>) ((NeoForgeBalmProviders) Balm.getProviders())
                        .getBlockCapability(KitchenItemProvider.class);

        event.registerBlockEntity(kitchenItemProvider, AppliedCookingBlockEntities.KITCHEN_STATION.get(),
                (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));

        registerOvenItemProvider(event, kitchenItemProvider);
    }

    /**
     * Registers Cooking for Blockheads' <em>own</em> Oven against its <em>own</em> item provider capability, which
     * CFB forgot to do here, every other complex block entity of theirs (sink, milk jar, fridge, cutting board) is
     * registered, and the Oven is registered for its energy capability two lines away, but never for this one. The
     * result is that the Oven's tool and output slots are invisible to the Cooking Table: a pot sitting in the Oven
     * does not count towards a recipe, though the same pot in an ME network does.
     * <p>
     * This is not our bug and not our block, so it is worth being explicit about why we fix it anyway: Cooking for
     * Blockheads 18.0.9 is the <em>final</em> 1.20.4 build, so upstream will never correct it. CFB fixed it on their
     * 1.21.1 line (21.1.24 registers the Oven), which is why the 1.21.1 branch of this mod carries no equivalent.
     * <p>
     * We deliberately hand back CFB's own provider via {@code getProvider(...)} rather than wrapping the Oven's
     * container ourselves. Their provider is scoped to the tools and output slots only; the Oven's full container is
     * 20 slots including the inputs, so exposing that would let the Cooking Table eat raw food queued for cooking.
     * Tagging the Oven into {@code kitchen_item_providers} does exactly that, and would additionally break when
     * {@code disallowOvenAutomation} is enabled, since that makes {@code getContainer()} return null.
     */
    private void registerOvenItemProvider(RegisterCapabilitiesEvent event,
                                          BlockCapability<KitchenItemProvider, Void> kitchenItemProvider) {
        event.registerBlockEntity(kitchenItemProvider, ModBlockEntities.oven.get(),
                (blockEntity, context) -> blockEntity.getProvider(KitchenItemProvider.class));
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }
}
