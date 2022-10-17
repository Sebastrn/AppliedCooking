package dev.smolinacadena.appliedcooking.item;

import appeng.api.features.IGridLinkableHandler;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class KitchenStationBlockItem extends BlockItem {

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public static final String TAG_GRID_KEY = "gridKey";

    public KitchenStationBlockItem(KitchenStationBlock block, Properties builder) {
        super(block, builder);
    }

    private static class LinkableHandler implements IGridLinkableHandler {
        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof KitchenStationBlockItem;
        }

        @Override
        public void link(ItemStack itemStack, long securityKey) {
            itemStack.getOrCreateTag().putLong(TAG_GRID_KEY, securityKey);
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.removeTagKey(TAG_GRID_KEY);
        }
    }
}
