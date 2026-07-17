package sebastrn.appliedcooking.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * The server-side data half, split from {@link KitchenStationComponentProvider} because Jade 1.21.6+ forbids one class
 * being both a data provider and a component provider. Shares the component provider's UID so the two stay paired.
 */
public class KitchenStationServerDataProvider implements IServerDataProvider<BlockAccessor> {

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        KitchenStationBlockEntity kitchenStation = (KitchenStationBlockEntity) accessor.getBlockEntity();
        data.putString("accessPointPos", kitchenStation.getAccessPointPos());
        // Send the drain rather than reading the config client-side: it's a server config, so the client's copy is
        // only correct once synced, and on a server the authoritative value is the one we're actually charging.
        data.putDouble("powerDrain", KitchenStationBlockEntity.idlePowerDrain());
    }

    @Override
    public Identifier getUid() {
        return KitchenStationComponentProvider.KITCHEN_STATION_UID;
    }
}
