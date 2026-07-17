package sebastrn.appliedcooking.item;

import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import sebastrn.appliedcooking.block.KitchenStationBlock;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class KitchenStationBlockItem extends BlockItem {

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public KitchenStationBlockItem(KitchenStationBlock block, Properties builder) {
        super(block, builder);
    }

    private static class LinkableHandler implements IGridLinkableHandler {
        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof KitchenStationBlockItem;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos) {
            itemStack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.remove(AEComponents.WIRELESS_LINK_TARGET);
        }
    }
}
