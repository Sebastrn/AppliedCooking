package sebastrn.appliedcooking.lootable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import sebastrn.appliedcooking.AppliedCookingLootFunctions;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;

import java.util.List;

public class KitchenStationBlockLootFunction extends LootItemConditionalFunction {

    public static final Codec<KitchenStationBlockLootFunction> CODEC = RecordCodecBuilder.create(
            instance -> commonFields(instance).apply(instance, KitchenStationBlockLootFunction::new));

    protected KitchenStationBlockLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof KitchenStationBlockEntity kitchenStation) {
            kitchenStation.applyDataFromBlockEntityToItem(stack);
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return AppliedCookingLootFunctions.KITCHEN_STATION.get();
    }
}
