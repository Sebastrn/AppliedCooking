package sebastrn.appliedcooking.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import sebastrn.appliedcooking.AppliedCooking;
import sebastrn.appliedcooking.AppliedCookingItems;

/**
 * JEI integration: attaches an "info" page to the ME Kitchen Station item explaining how to link and use it, so the
 * how-to is discoverable right where players look things up. Loaded only when JEI is present (JEI's {@code @JeiPlugin}
 * scan never touches this class otherwise), so JEI stays an optional dependency.
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(AppliedCooking.ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                new ItemStack(AppliedCookingItems.KITCHEN_STATION.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.appliedcooking.kitchen_station.1"),
                Component.translatable("jei.appliedcooking.kitchen_station.2"),
                Component.translatable("jei.appliedcooking.kitchen_station.3"));
    }
}
