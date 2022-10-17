package dev.smolinacadena.appliedcooking.setup;

import dev.smolinacadena.appliedcooking.AppliedCooking;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e) {
        ResourceLocation kitchenStation = new ResourceLocation(AppliedCooking.ID, "block/kitchen_station");
        ResourceLocation kitchenStationConnected = new ResourceLocation(AppliedCooking.ID, "block/kitchen_station_connected");

        ResourceLocation kitchenAccessPoint = new ResourceLocation(AppliedCooking.ID, "block/kitchen_access_point");
        ResourceLocation kitchenAccessPointConnected = new ResourceLocation(AppliedCooking.ID, "block/kitchen_access_point_connected");
    }
}
