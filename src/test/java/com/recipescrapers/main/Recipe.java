package com.recipescrapers.main;

public class Recipe {
	
	private String recipeID,recipeName,ingredients;
	/*
	 * Recipe Category(Breakfast/lunch/snack/dinner) Food
	 * Category(Veg/non-veg/vegan/Jain)
	 * 
	 * Preparation Time Cooking Time Tag No of servings Cuisine category Recipe
	 * Description Preparation method Nutrient values Recipe URL
	 */

	public String getRecipeID() {
		return recipeID;
	}

	public void setRecipeID(String recipeID) {
		this.recipeID = recipeID;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}
	
	public String toString() {
		return recipeName;	
	}
	
	
}
