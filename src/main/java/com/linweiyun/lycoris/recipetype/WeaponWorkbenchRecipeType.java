package com.linweiyun.lycoris.recipetype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.linweiyun.lycoris.LycPerseusMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

// recipe类仅描述了配方数据和执行逻辑，
// 通过container子类提供数据
// 任何输入的Container都应该是不可变的，任何的操作都应该通过copy输入副本。

public class WeaponWorkbenchRecipeType implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> material;
    public WeaponWorkbenchRecipeType(ResourceLocation id, ItemStack output,
                                     NonNullList<Ingredient> catalyst){
        this.id = id;
        this.output = output;
        this.material = catalyst;
    }
    // 为了能够通过管理器获得配方，match必须返回true
    // 此方法用于管理容器是否输入有效。
    // 通过代用test检测
    // 检查容器内的物品和配方是否匹配。
@Override
public boolean matches(SimpleContainer pContainer, Level pLevel) {
    // 如果当前环境为客户端，则返回false，不在客户端进行合成匹配验证
    if (pLevel.isClientSide()) {
        return false;
    }

    // 创建一个StackedContents对象，用于追踪容器内物品堆叠情况
    StackedContents stackedContents = new StackedContents();

    // 初始化计数器，用于统计非空物品槽位的数量

    // 直接判断计数器i所代表的非空物品槽位数量是否等于本配方所需物品数量
    // 并使用StackedContents确认容器内的物品组合是否满足本配方的合成条件
    // 同时检查容器中首个槽位的物品是否满足本配方定义的催化剂条件
    return output.getItem() == pContainer.getItem(0).getItem();
}

    // 获得合成表所需要的item stacks
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return material;
    }

    // 构建配方
    // 返回了合成表的结果output
    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess registryAccess) {
        return output;
    }
    // 这个方法用于判断合成表是否可以在指定的dimensions合成。
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
    // 获得合成表物品的copy()
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    //
    @Override
    public ResourceLocation getId() {
        return id;
    }
    // 返回Serializer 必须返回
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    // 返回type
    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    // 注册新的合成的type
    public static class Type implements RecipeType<WeaponWorkbenchRecipeType>{
        private Type(){}
        public static final Type INSTANCE = new Type();
        // 标识了合成的类型，和json文件中的type一致
        public static final String ID = "weapon_workbench";
    }

    // 负责解码JSON并通过网络通信
    // 需要注册
    public static class Serializer implements RecipeSerializer<WeaponWorkbenchRecipeType> {
        public static final Serializer INSTANCE = new Serializer();
        public static final  ResourceLocation ID =
                new ResourceLocation(LycPerseusMod.MOD_ID,"weapon_workbench");
        // 将JSON解码为recipe子类型
        @Override
        public WeaponWorkbenchRecipeType fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe,"output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe,"ingredients");

            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(),Ingredient.EMPTY);

            for(int i =0;i<inputs.size();i++){
                JsonElement ingredientElement = ingredients.get(i);
                Ingredient ingredient = parseIngredientWithCount(ingredientElement);
                inputs.set(i,ingredient);
            }
            return new WeaponWorkbenchRecipeType(pRecipeId,output,inputs);
        }
        // 从服务器中发送的数据中解码recipe，配方标识符不需要解码。
        @Override
        public @Nullable WeaponWorkbenchRecipeType fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(),Ingredient.EMPTY);
            for (int i=0;i < inputs.size();i++){
                inputs.set(i,Ingredient.fromNetwork(pBuffer));
            }
            ItemStack output = pBuffer.readItem();
            return new WeaponWorkbenchRecipeType(pRecipeId,output,inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, WeaponWorkbenchRecipeType pRecipe) {
            pBuffer.writeInt(pRecipe.getIngredients().size());
            for (Ingredient ing : pRecipe.getIngredients()){
                ing.toNetwork(pBuffer);
            }
            pBuffer.writeItemStack(pRecipe.getResultItem(null),false);
        }
    }

    private static Ingredient parseIngredientWithCount(JsonElement ingredientElement) {
        if (!ingredientElement.isJsonObject()) {
            throw new IllegalArgumentException("Ingredient must be a JSON object");
        }

        JsonObject ingredientObject = ingredientElement.getAsJsonObject();

        String itemName = GsonHelper.getAsString(ingredientObject, "item");
        String nbtId1 = GsonHelper.getAsString(ingredientObject, "nbtId1", "");
        String nbtValue1 = GsonHelper.getAsString(ingredientObject, "nbtValue1", "");
        String nbtId2 = GsonHelper.getAsString(ingredientObject, "nbtId2", "");
        String nbtValue2 = GsonHelper.getAsString(ingredientObject, "nbtValue2", "");
        String nbtId3 = GsonHelper.getAsString(ingredientObject, "nbtId3", "");
        String nbtValue3 = GsonHelper.getAsString(ingredientObject, "nbtValue3", "");
        String nbtId4 = GsonHelper.getAsString(ingredientObject, "nbtId4", "");
        String nbtValue4 = GsonHelper.getAsString(ingredientObject, "nbtValue4", "");

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + itemName);
        }
        ItemStack itemStack = new ItemStack(item, 1);
        setExtraCounts(itemStack,nbtId1, nbtValue1, nbtId2, nbtValue2, nbtId3, nbtValue3, nbtId4, nbtValue4);

        return Ingredient.of(itemStack);
    }

    private static void setExtraCounts(ItemStack stack,String nbtId1, String nbtValue1, String nbtId2, String nbtValue2,
                                       String nbtId3, String nbtValue3, String nbtId4, String nbtValue4) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("nbtId1", nbtId1);
        tag.putString("nbtValue1", nbtValue1);
        tag.putString("nbtId2", nbtId2);
        tag.putString("nbtValue2", nbtValue2);
        tag.putString("nbtId3", nbtId3);
        tag.putString("nbtValue3", nbtValue3);
        tag.putString("nbtId4", nbtId4);
        tag.putString("nbtValue4", nbtValue4);


    }

    public static String getNbtId(ItemStack stack, int i) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            switch (i){
                case 1: return tag.getString("nbtId1");
                case 2: return tag.getString("nbtId2");
                case 3: return tag.getString("nbtId3");
                case 4: return tag.getString("nbtId4");
            }

        }
        return ""; // 默认为空字符串
    }
    public static String getNbtValue(ItemStack stack, int i) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            switch (i){
                case 1: return tag.getString("nbtValue1");
                case 2: return tag.getString("nbtValue2");
                case 3: return tag.getString("nbtValue3");
                case 4: return tag.getString("nbtValue4");
            }
        }
        return ""; // 默认为空字符串
    }



}