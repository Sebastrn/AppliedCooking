package dev.smolinacadena.appliedcooking.item.group;

import dev.smolinacadena.appliedcooking.AppliedCookingItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MainCreativeModeTab extends CreativeModeTab {
    public MainCreativeModeTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(AppliedCookingItems.KITCHEN_STATION.get());
    }
}
