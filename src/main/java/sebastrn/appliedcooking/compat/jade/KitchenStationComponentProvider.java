package sebastrn.appliedcooking.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import sebastrn.appliedcooking.AppliedCooking;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * The client-side tooltip half. Jade 1.21.6+ forbids one class implementing both {@link IBlockComponentProvider} and
 * {@code IServerDataProvider} ("Data providers cannot implement IComponentProvider … Use a separate client provider"),
 * so the server half lives in {@link KitchenStationServerDataProvider}. Both share the same UID.
 */
public class KitchenStationComponentProvider implements IBlockComponentProvider {

    public static final Identifier KITCHEN_STATION_UID = Identifier.fromNamespaceAndPath(AppliedCooking.ID, "kitchen_station");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // 26.1's CompoundTag getters return Optional; use the *Or accessors so we still get a plain value (the old
        // getString/getDouble would compile but stringify an Optional into the tooltip).
        CompoundTag data = accessor.getServerData();
        String linkState = data.getStringOr("linkState", "unlinked");
        String accessPointPos = data.getStringOr("accessPointPos", "");
        switch (linkState) {
            case "online" -> {
                tooltip.add(Component.translatable("jade.appliedcooking:online"));
                tooltip.add(Component.translatable("jade.appliedcooking:kitchen_station", Component.translatable("block.ae2.wireless_access_point"), accessPointPos));
                tooltip.add(Component.translatable("jade.appliedcooking:power_drain", String.valueOf(data.getDoubleOr("powerDrain", 0))));
            }
            case "linked_offline" -> {
                tooltip.add(Component.translatable("jade.appliedcooking:linked_offline"));
                if (!accessPointPos.isEmpty()) {
                    tooltip.add(Component.translatable("jade.appliedcooking:kitchen_station", Component.translatable("block.ae2.wireless_access_point"), accessPointPos));
                }
            }
            default -> tooltip.add(Component.translatable("jade.appliedcooking:offline"));
        }
    }

    @Override
    public Identifier getUid() {
        return KITCHEN_STATION_UID;
    }
}
