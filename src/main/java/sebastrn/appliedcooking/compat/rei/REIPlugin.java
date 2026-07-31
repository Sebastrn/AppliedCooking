package sebastrn.appliedcooking.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import sebastrn.appliedcooking.AppliedCookingItems;

/**
 * REI integration: the same "info" page the JEI plugin attaches to the ME Kitchen Station, so REI users get the how-to
 * where they look things up. Reuses the existing {@code jei.appliedcooking.*} text (the keys are viewer-agnostic).
 * Discovered only when REI is present (its {@code @REIPluginClient} scan never touches this class otherwise), so REI
 * stays optional. (EMI has no 26.1 build, so there is no EMI plugin on this branch, see the build script.)
 */
@REIPluginClient
public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        info(AppliedCookingItems.KITCHEN_STATION.get(),
                "jei.appliedcooking.kitchen_station.1",
                "jei.appliedcooking.kitchen_station.2",
                "jei.appliedcooking.kitchen_station.3");
    }

    private static void info(ItemLike item, String... lineKeys) {
        BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.of(item),
                new ItemStack(item).getHoverName(),
                lines -> {
                    for (String key : lineKeys) {
                        lines.add(Component.translatable(key));
                    }
                    return lines;
                });
    }
}
