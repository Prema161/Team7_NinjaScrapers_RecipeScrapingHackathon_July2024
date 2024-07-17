
package com.recipescrapers.main;

public class Recipe {
	
	private String recipeID,recipeName,ingredients,recipeDescription,preparationMethod,nutritionValues,recipeUrl,preperationTime,cookingTime,numOfServings,cuisineCategory,recipeCategory,foodCategory,tags,ingredientsName;
	private boolean lfvRecipesToAvoid;
	
	//constructor
	  public Recipe(String recipeId, String recipeTitle, String recipeDescription, String ingredientsName,
	            String preperationTime, String cookingTime, String preparationMethod, String numOfServings,
	            String cuisineCategory, String foodCategory,String recipeCategory, String tags, String nutritionValues, String recipeUrl) {
	        this.recipeID = recipeId;
	        this.recipeName = recipeTitle;
	        this.recipeDescription = recipeDescription;
	        this.ingredientsName = ingredientsName;
	        this.preperationTime = preperationTime;
	        this.cookingTime = cookingTime;
	        this.preparationMethod = preparationMethod;
	        this.numOfServings = numOfServings;
	        this.cuisineCategory = cuisineCategory;
	        this.foodCategory = foodCategory;
	        this.recipeCategory = recipeCategory;
	        this.tags = tags;
	        this.nutritionValues = nutritionValues;
	        this.recipeUrl = recipeUrl;
	    }
	
	
	
	
	public String getCuisineCategory() {
		return cuisineCategory;
	}

	public void setCuisineCategory(String cuisineCategory) {
		this.cuisineCategory = cuisineCategory;
	}
	
	//RecipeCategory
	public String getRecipeCategory() {
		return recipeCategory;
	}

	public void setRecipeCategory(String recipeCategory) {
		this.recipeCategory = recipeCategory;
	}
	
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	

	
	public String getIngredientsName() {
		return ingredientsName;
	}

	public void setIngredientsName(String ingredientsName) {
		this.ingredientsName = ingredientsName;
	}

	public String getFoodCategory() {
		return foodCategory;
	}

	public void setFoodCategory(String foodCategory) {
		this.foodCategory = foodCategory;
	}

	public String getPreperationTime() {
		return preperationTime;
	}

	public String getNumOfServings() {
		return numOfServings;
	}

	public void setNumOfServings(String numOfServings) {
		this.numOfServings = numOfServings;
	}

	public void setPreperationTime(String preperationTime) {
		this.preperationTime = preperationTime;
	}

	public String getCookingTime() {
		return cookingTime;
	}

	public void setCookingTime(String cookingTime) {
		this.cookingTime = cookingTime;
	}

	public String getRecipeDescription() {
		return recipeDescription;
	}

	public void setRecipeDescription(String recipeDescription) {
		this.recipeDescription = recipeDescription;
	}

	public String getPreparationMethod() {
		return preparationMethod;
	}

	public void setPreparationMethod(String preparationMethod) {
		this.preparationMethod = preparationMethod;
	}

	public String getNutritionValues() {
		return nutritionValues;
	}

	public void setNutritionValues(String nutritionValues) {
		this.nutritionValues = nutritionValues;
	}

	public String getRecipeUrl() {
		return recipeUrl;
	}

	public void setRecipeUrl(String recipeUrl) {
		this.recipeUrl = recipeUrl;
	}

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
		return "ReceipeName :"+ recipeName + "Ingredenients :" + ingredients;
	}

	public boolean isLfvRecipesToAvoid() {
		return lfvRecipesToAvoid;
	}

	public void setLfvRecipesToAvoid(boolean lfvRecipesToAvoid) {
		this.lfvRecipesToAvoid = lfvRecipesToAvoid;
	
	
	
	

	
	
	
	

	
	
}
