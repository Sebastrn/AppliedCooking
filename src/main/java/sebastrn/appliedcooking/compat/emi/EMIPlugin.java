package sebastrn.appliedcooking.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import sebastrn.appliedcooking.AppliedCooking;
import sebastrn.appliedcooking.AppliedCookingItems;

import java.util.ArrayList;
import java.util.List;

/**
 * EMI integration: the same "info" page the JEI plugin attaches to the ME Kitchen Station. Reuses the existing
 * {@code jei.appliedcooking.*} text (the keys are viewer-agnostic). Unlike REI, EMI does not read JEI plugins, so this
 * class is the only way EMI users see it. Discovered by EMI's {@code @EmiEntrypoint} scan, so EMI stays optional.
 */
@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        info(registry, AppliedCookingItems.KITCHEN_STATION.get(), "kitchen_station",
                "jei.appliedcooking.kitchen_station.1",
                "jei.appliedcooking.kitchen_station.2",
                "jei.appliedcooking.kitchen_station.3");
    }

    private static void info(EmiRegistry registry, ItemLike item, String id, String... lineKeys) {
        List<Component> text = new ArrayList<>();
        for (String key : lineKeys) {
            text.add(Component.translatable(key));
        }
        // Synthetic recipe id: these info pages have no data-driven JSON recipe, so EMI wants the path prefixed with
        // '/' (otherwise its dev mode warns that the id isn't in the recipe manager and isn't marked synthetic).
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(item)),
                text,
                ResourceLocation.fromNamespaceAndPath(AppliedCooking.ID, "/info/" + id)));
    }
}
