package dev.smolinacadena.appliedcooking.blockentity;

import appeng.api.features.Locatables;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.me.InWorldGridNode;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.mojang.datafixers.util.Pair;
import dev.smolinacadena.appliedcooking.AppliedCookingBlockEntities;
import dev.smolinacadena.appliedcooking.AppliedCookingBlocks;
import dev.smolinacadena.appliedcooking.api.cookingforblockheads.capability.KitchenItemProvider;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.block.BalmBlockEntityContract;
import net.blay09.mods.balm.api.energy.EnergyStorage;
import net.blay09.mods.balm.api.fluid.FluidTank;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.api.provider.BalmProviderHolder;
import net.blay09.mods.balm.forge.energy.ForgeEnergyStorage;
import net.blay09.mods.balm.forge.fluid.ForgeFluidTank;
import net.blay09.mods.balm.forge.provider.ForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitchenStationBlockEntity extends BlockEntity implements BalmBlockEntityContract {
    private final Map<Capability<?>, LazyOptional<?>> capabilities = new HashMap<>();
    private final Table<Capability<?>, Direction, LazyOptional<?>> sidedCapabilities = HashBasedTable.create();
    private boolean capabilitiesInitialized;
    private final KitchenItemProvider itemProvider = new KitchenItemProvider(this);
    private Long gridKey;

    public KitchenStationBlockEntity(BlockPos pos, BlockState state) {
        super(AppliedCookingBlockEntities.KITCHEN_STATION.get(), pos, state);
    }

    private void addCapabilities(BalmProvider<?> provider, Map<Capability<?>, LazyOptional<?>> capabilities) {
        ForgeBalmProviders forgeProviders = (ForgeBalmProviders) Balm.getProviders();
        Capability<?> capability = forgeProviders.getCapability(provider.getProviderClass());
        capabilities.put(capability, LazyOptional.of(provider::getInstance));

        if (provider.getProviderClass() == Container.class) {
            capabilities.put(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, LazyOptional.of(() -> new InvWrapper((Container) provider.getInstance())));
        } else if (provider.getProviderClass() == FluidTank.class) {
            capabilities.put(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(() -> new ForgeFluidTank((FluidTank) provider.getInstance())));
        } else if (provider.getProviderClass() == EnergyStorage.class) {
            capabilities.put(CapabilityEnergy.ENERGY, LazyOptional.of(() -> new ForgeEnergyStorage((EnergyStorage) provider.getInstance())));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProvider(Class<T> clazz) {
        ForgeBalmProviders forgeProviders = (ForgeBalmProviders) Balm.getProviders();
        Capability<?> capability = forgeProviders.getCapability(clazz);
        return (T) getCapability(capability).resolve().orElse(null);
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider<>(IKitchenItemProvider.class, itemProvider));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if (!capabilitiesInitialized) {
            List<BalmProviderHolder> providers = new ArrayList<>();
            buildProviders(providers);

            for (BalmProviderHolder providerHolder : providers) {
                for (BalmProvider<?> provider : providerHolder.getProviders()) {
                    addCapabilities(provider, capabilities);
                }

                for (Pair<Direction, BalmProvider<?>> pair : providerHolder.getSidedProviders()) {
                    Direction direction = pair.getFirst();
                    BalmProvider<?> provider = pair.getSecond();
                    Map<Capability<?>, LazyOptional<?>> sidedCapabilities = this.sidedCapabilities.column(direction);
                    addCapabilities(provider, sidedCapabilities);
                }
            }
            capabilitiesInitialized = true;
        }

        LazyOptional<?> result = null;
        if (side != null) {
            result = sidedCapabilities.get(cap, side);
        }
        if (result == null) {
            result = capabilities.get(cap);
        }
        return result != null ? result.cast() : super.getCapability(cap, side);
    }

    public void setConnected(boolean connected) {
        level.blockEvent(worldPosition, AppliedCookingBlocks.KITCHEN_STATION.get(), 0, 0);

        BlockState state = level.getBlockState(worldPosition);
        level.setBlockAndUpdate(worldPosition, state.setValue(KitchenStationBlock.CONNECTED, connected));
        setChanged();
    }

    public void applyDataFromItemToBlockEntity(ItemStack stack) {
        if (stack.hasTag()) {
            if (stack.getTag().contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
                gridKey = stack.getTag().getLong(KitchenStationBlockItem.TAG_GRID_KEY);

                if (getActionHost() != null) {
                    setConnected(true);
                }
            }
        }

        setChanged();
    }

    public void applyDataFromBlockEntityToItem(ItemStack stack) {
        stack.setTag(new CompoundTag());

        if (stack.getTag().contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
            stack.getTag().putLong(KitchenStationBlockItem.TAG_GRID_KEY, gridKey);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (gridKey != null) {
            tag.putLong(KitchenStationBlockItem.TAG_GRID_KEY, gridKey.longValue());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
            gridKey = tag.getLong(KitchenStationBlockItem.TAG_GRID_KEY);
        }
    }

    public MEStorage getNetworkStorage() {
        if (getActionHost() != null) {
            var n = getActionHost().getActionableNode();
            if (n != null) {
                return n.getGrid().getStorageService().getInventory();
            }
        }
        return null;
    }

    public String getSecurityStationPos() {
        if (getActionHost() != null) {
            InWorldGridNode securityStation = (InWorldGridNode) getActionHost().getActionableNode();
            return securityStation.getLocation().getX() + ", " + securityStation.getLocation().getY() + ", " + securityStation.getLocation().getZ();
        }
        return "";
    }

    public IActionHost getActionHost() {
        if (gridKey != null) {
            return Locatables.securityStations().get(this.level, gridKey.longValue());
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KitchenStationBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        if (getActionHost() != null) {
            setConnected(true);
        } else {
            setConnected(false);
        }
    }
}
