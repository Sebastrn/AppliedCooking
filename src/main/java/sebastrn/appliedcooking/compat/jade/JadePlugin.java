package sebastrn.appliedcooking.compat.jade;

import sebastrn.appliedcooking.AppliedCooking;
import sebastrn.appliedcooking.block.KitchenStationBlock;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(AppliedCooking.ID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new KitchenStationServerDataProvider(), KitchenStationBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new KitchenStationComponentProvider(), KitchenStationBlock.class);
    }
}
