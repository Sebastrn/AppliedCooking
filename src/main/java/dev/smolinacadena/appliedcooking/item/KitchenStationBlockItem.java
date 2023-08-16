package dev.smolinacadena.appliedcooking.item;

import appeng.api.features.IGridLinkableHandler;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class KitchenStationBlockItem extends BlockItem {

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public static final String TAG_ACCESS_POINT_POS = "accessPointPos";

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
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
                    .result()
                    .ifPresent(tagValue -> itemStack.getOrCreateTag().put(TAG_ACCESS_POINT_POS, tagValue));
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.removeTagKey(TAG_ACCESS_POINT_POS);
        }
    }
}
