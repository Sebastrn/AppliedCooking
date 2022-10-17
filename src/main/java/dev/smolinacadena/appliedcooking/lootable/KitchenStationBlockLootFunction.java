package dev.smolinacadena.appliedcooking.lootable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import dev.smolinacadena.appliedcooking.AppliedCookingLootFunctions;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class KitchenStationBlockLootFunction extends LootItemConditionalFunction {
    public KitchenStationBlockLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof KitchenStationBlockEntity){
            ((KitchenStationBlockEntity)blockEntity).applyDataFromBlockEntityToItem(stack);
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return AppliedCookingLootFunctions.KITCHEN_STATION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<KitchenStationBlockLootFunction> {

        @Override
        public KitchenStationBlockLootFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditions) {
            return new KitchenStationBlockLootFunction(conditions);
        }
    }
}
