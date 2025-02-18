package com.thevortex.potionsmaster.items.potions.recipes.oresight;

import com.thevortex.potionsmaster.init.ModPotions;

import com.thevortex.potionsmaster.init.ModRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;


public class DiamondPotionRecipe extends BrewingRecipe {


    public DiamondPotionRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        super(input, ingredient, output);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isInput(ItemStack input) {
        return (PotionUtils.getPotion(input) == Potions.MUNDANE);
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return (ingredient.getItem().asItem() == ModRegistry.CALCINATEDDIAMOND_POWDER.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return isInput(input) && isIngredient(ingredient) ? getOutput().copy() : ItemStack.EMPTY;

    }

    public ItemStack getOutput() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.DIAMOND_SIGHT);
    }

}

