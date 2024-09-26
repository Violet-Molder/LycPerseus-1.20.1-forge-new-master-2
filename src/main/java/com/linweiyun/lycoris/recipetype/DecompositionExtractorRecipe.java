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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecompositionExtractorRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient output;
    private final NonNullList<Ingredient> recipeItems;
    private final int time;
    public DecompositionExtractorRecipe(ResourceLocation id, Ingredient output,
                                        NonNullList<Ingredient> recipeItems, int time){
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.time = time;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        // 如果当前环境为客户端，则返回false，不在客户端进行合成匹配验证
        if (pLevel.isClientSide()) {
            return false;
        }

        // 直接判断计数器i所代表的非空物品槽位数量是否等于本配方所需物品数量
        // 并使用StackedContents确认容器内的物品组合是否满足本配方的合成条件
        // 同时检查容器中首个槽位的物品是否满足本配方定义的催化剂条件
        return output.test(pContainer.getItem(7));
    }

    // 获得合成表所需要的item stacks
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    public Ingredient getOutput(){
        return output;
    }
    public int getTime(){
        return this.time;
    }

    // 构建配方
    // 返回了合成表的结果output
    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess registryAccess) {
        return output.getItems()[0];
    }
    // 这个方法用于判断合成表是否可以在指定的dimensions合成。
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
    // 获得合成表物品的copy()
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.getItems()[0].copy();
    }

    //
    @Override
    public ResourceLocation getId() {
        return id;
    }
    // 返回Serializer 必须返回
    @Override
    public RecipeSerializer<?> getSerializer() {
        return DecompositionExtractorRecipe.Serializer.INSTANCE;
    }
    // 返回type
    @Override
    public RecipeType<?> getType() {
        return DecompositionExtractorRecipe.Type.INSTANCE;
    }

    // 注册新的合成的type
    public static class Type implements RecipeType<DecompositionExtractorRecipe>{
        private Type(){}
        public static final DecompositionExtractorRecipe.Type INSTANCE = new DecompositionExtractorRecipe.Type();
        // 标识了合成的类型，和json文件中的type一致
        public static final String ID = "decomposition_extractor_recipe";
    }

    // 负责解码JSON并通过网络通信
    // 需要注册
    public static class Serializer implements RecipeSerializer<DecompositionExtractorRecipe> {
        public static final DecompositionExtractorRecipe.Serializer INSTANCE = new DecompositionExtractorRecipe.Serializer();
        public static final  ResourceLocation ID =
                new ResourceLocation(LycPerseusMod.MOD_ID,"decomposition_extractor_recipe");
        // 将JSON解码为recipe子类型
        @Override
        public DecompositionExtractorRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            Ingredient output = Ingredient.fromJson(GsonHelper.getAsJsonObject(pSerializedRecipe,"output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe,"ingredients");

            //读取催化剂
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(),Ingredient.EMPTY);
            int time = GsonHelper.getAsInt(pSerializedRecipe,"time");
            for(int i =0;i<inputs.size();i++){
                JsonElement ingredientElement = ingredients.get(i);
                Ingredient ingredient = parseIngredientWithCount(ingredientElement);
                inputs.set(i,ingredient);
            }
            return new DecompositionExtractorRecipe(pRecipeId,output,inputs, time);
        }

        private static Ingredient parseIngredientWithCount(JsonElement ingredientElement) {
            if (!ingredientElement.isJsonObject()) {
                throw new IllegalArgumentException("Ingredient must be a JSON object");
            }

            JsonObject ingredientObject = ingredientElement.getAsJsonObject();

            String itemName = GsonHelper.getAsString(ingredientObject, "item");
            int minCount = GsonHelper.getAsInt(ingredientObject, "minCount", 1); // 默认值为1
            int maxCount = GsonHelper.getAsInt(ingredientObject, "maxCount", 1); // 默认值为最大整数
            int isEmpty = GsonHelper.getAsInt(ingredientObject, "isEmpty", 0);
            String nbtId = GsonHelper.getAsString(ingredientObject, "nbtId", "");
            String nbtValue = GsonHelper.getAsString(ingredientObject, "nbtValue", "");

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item == null) {
                throw new IllegalArgumentException("Unknown item: " + itemName);
            }
            ItemStack itemStack = new ItemStack(item, 1);
            setExtraCounts(itemStack, minCount, maxCount, isEmpty,nbtId, nbtValue);

            return Ingredient.of(itemStack);
        }
        // 从服务器中发送的数据中解码recipe，配方标识符不需要解码。
        @Override
        public @Nullable DecompositionExtractorRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(),Ingredient.EMPTY);
            for (int i=0;i < inputs.size();i++){
                inputs.set(i,Ingredient.fromNetwork(pBuffer));
            }
            Ingredient output = Ingredient.fromNetwork(pBuffer);
            int time = pBuffer.readInt();
            return new DecompositionExtractorRecipe(pRecipeId,output,inputs, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, DecompositionExtractorRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.getIngredients().size());
            for (Ingredient ing : pRecipe.getIngredients()){
                ing.toNetwork(pBuffer);
            }
            pRecipe.getOutput().toNetwork(pBuffer);
            pBuffer.writeInt(pRecipe.getTime());
        }


        private static void setExtraCounts(ItemStack stack, int minCount, int maxCount, int isEmpty,String nbtId, String nbtValue) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("minCount", minCount);
            tag.putInt("maxCount", maxCount);
            tag.putInt("isEmpty", isEmpty);
            tag.putString("nbtId", nbtId);
            tag.putString("nbtValue", nbtValue);

        }



    }
    public static int getMinCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getInt("minCount");
        }
        return 1; // 默认最小数量
    }

    public static int getMaxCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getInt("maxCount");
        }
        return Integer.MAX_VALUE; // 默认最大数量
    };
    public static int getIsEmpty(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getInt("isEmpty");
        }
        return 0; // 默认为0
    }

    public static String getNbtId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getString("nbtId");
        }
        return ""; // 默认为空字符串
    }
    public static String getNbtValue(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getString("nbtValue");
        }
        return ""; // 默认为空字符串
    }


}
