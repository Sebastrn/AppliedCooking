package dev.smolinacadena.appliedcooking.compat.jade;

import dev.smolinacadena.appliedcooking.AppliedCooking;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import mcp.mobius.waila.api.*;

@WailaPlugin(AppliedCooking.ID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new KitchenStationComponentProvider(), KitchenStationBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(new KitchenStationComponentProvider(), TooltipPosition.BODY, KitchenStationBlock.class);
    }
}
