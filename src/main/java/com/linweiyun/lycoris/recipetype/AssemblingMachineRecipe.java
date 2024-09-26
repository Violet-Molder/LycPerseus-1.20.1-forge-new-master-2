package com.linweiyun.lycoris.recipetype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.linweiyun.lycoris.LycPerseusMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class AssemblingMachineRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    private final int time;
    public AssemblingMachineRecipe(ResourceLocation id, ItemStack output,
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

        // 创建一个StackedContents对象，用于追踪容器内物品堆叠情况
        StackedContents stackedContents = new StackedContents();


        // 创建一个Map来存储每个非空槽位的物品及其数量
        Map<Item, Integer> itemCounts = new HashMap<>();

        // 初始化计数器，用于统计非空物品槽位的数量
        int i = 0;

        // 遍历容器内除首尾槽位之外的所有槽位
        for (int j = 0; j < 6; ++j) {
            // 获取当前槽位中的物品
            ItemStack itemStack = pContainer.getItem(j);

            // 如果物品不是空的
            if (!itemStack.isEmpty()) {
                // 增加计数器
                ++i;

                //记录每一个非空物品的数量
                itemCounts.merge(itemStack.getItem(), itemStack.getCount(), Integer::sum);

                // 将当前物品的信息计入StackedContents，用于后续合成验证
                stackedContents.accountStack(itemStack, itemStack.getCount());
            }
        }

        List<ItemStack> allItemStacks = new ArrayList<>();
        for (Ingredient ingredient : this.getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                allItemStacks.add(stack);
            }
        }
        boolean canCraft = false;

        // 检查容器内的物品数量是否满足配方要求
        for (ItemStack itemStack : allItemStacks) {
                if (itemStack.isEmpty()) {
                    continue;
                }

                int requiredCount = itemStack.getCount();

                //读取每个非空槽位的数量
                Integer count = itemCounts.get(itemStack.getItem());
                if (count == null || count < requiredCount){
                    return canCraft = false;
                } else if (count >= requiredCount){
                    canCraft = true;
                }
        }

        // 直接判断计数器i所代表的非空物品槽位数量是否等于本配方所需物品数量
        // 并使用StackedContents确认容器内的物品组合是否满足本配方的合成条件
        // 同时检查容器中首个槽位的物品是否满足本配方定义的催化剂条件
        return i == this.recipeItems.size() && stackedContents.canCraft(this, (IntList) null) && canCraft;
    }

    // 获得合成表所需要的item stacks
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }
    public int getTime(){
        return this.time;
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
    public static class Type implements RecipeType<AssemblingMachineRecipe>{
        private Type(){}
        public static final Type INSTANCE = new Type();
        // 标识了合成的类型，和json文件中的type一致
        public static final String ID = "assembling_machine_recipe";
    }

    // 负责解码JSON并通过网络通信
    // 需要注册
    public static class Serializer implements RecipeSerializer<AssemblingMachineRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final  ResourceLocation ID =
                new ResourceLocation(LycPerseusMod.MOD_ID,"assembling_machine_recipe");
        // 将JSON解码为recipe子类型
        @Override
        public AssemblingMachineRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe,"output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe,"ingredients");

            //读取催化剂
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(),Ingredient.EMPTY);
            int time = GsonHelper.getAsInt(pSerializedRecipe,"time");
            for(int i =0;i<inputs.size();i++){
                JsonElement ingredientElement = ingredients.get(i);
                Ingredient ingredient = parseIngredientWithCount(ingredientElement);
                inputs.set(i,ingredient);
            }
            return new AssemblingMachineRecipe(pRecipeId,output,inputs, time);
        }

        //处理原料的count
        private static Ingredient parseIngredientWithCount(JsonElement ingredientElement) {
            if (!ingredientElement.isJsonObject()) {
                throw new IllegalArgumentException("Ingredient must be a JSON object");
            }

            JsonObject ingredientObject = ingredientElement.getAsJsonObject();

            String itemName = GsonHelper.getAsString(ingredientObject, "item");
            int count = GsonHelper.getAsInt(ingredientObject, "count", 1); // 默认值为1

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item == null) {
                throw new IllegalArgumentException("Unknown item: " + itemName);
            }

            return Ingredient.of(new ItemStack(item, count));
        }
        // 从服务器中发送的数据中解码recipe，配方标识符不需要解码。
        @Override
        public @Nullable AssemblingMachineRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(),Ingredient.EMPTY);
            for (int i=0;i < inputs.size();i++){
                inputs.set(i,Ingredient.fromNetwork(pBuffer));
            }
            ItemStack output = pBuffer.readItem();
            int time = pBuffer.readInt();
            return new AssemblingMachineRecipe(pRecipeId,output,inputs, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, AssemblingMachineRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.getIngredients().size());
            for (Ingredient ing : pRecipe.getIngredients()){
                ing.toNetwork(pBuffer);
            }
            pBuffer.writeItemStack(pRecipe.getResultItem(null),false);
            pBuffer.writeInt(pRecipe.getTime());
        }
    }


}
