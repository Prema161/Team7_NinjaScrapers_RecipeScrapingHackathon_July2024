package com.recipescrapers.main;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.github.bonigarcia.wdm.WebDriverManager;



public class RecipeScraperTest {

	int last_page;
	WebDriver driver;
	DatabaseClass db;
	List<Recipe> allRecipesList = new ArrayList<Recipe>();
	List<Recipe> lchfAddRecipes = new ArrayList<Recipe>();
	List<Recipe> lchfEliminationRecipes = new ArrayList<Recipe>();
	List<Recipe> lfvEliminationRecipes = new ArrayList<Recipe>();
	List<Recipe> lfvAddRecipes = new ArrayList<Recipe>();
	List<Recipe> lfvToAddRecipes = new ArrayList<Recipe>();
	List<Recipe> lfvToAddEliminationRecipes = new ArrayList<Recipe>();
	List<Recipe> lfvNutAllergyEliminationRecipes=new ArrayList<Recipe>();
	List<Recipe> lfvOtherAllergyEliminationRecipes=new ArrayList<Recipe>();
	List<Recipe> lchfNutAllergyEliminationRecipes=new ArrayList<Recipe>();
	List<Recipe> lchfOtherAllergyEliminationRecipes=new ArrayList<Recipe>();
	List<Recipe> lfvOptionalRecipes=new ArrayList<Recipe>();

	private static final Logger logger = LoggerFactory.getLogger(RecipeScraperTest.class);

	String[] tableNames = {"recipes", "LCHFEliminatedRecipe","lchfAddRecipes","lfvEliminationRecipes","lfvAddRecipes","lfvToAddRecipes","lfvnutallergy","lfvotherallergy","lchfnutallergy","lchfotherallergy","lfvOptionalRecipes"};



	@Test
	public void RecipeScrape() throws SQLException {

		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--blink-settings=imagesEnabled=false");//disabling the images
		options.addArguments("--disable-images");
		options.addArguments("--disable-javascript");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--headless");//executing in headless mode
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-extensions");
		options.addArguments("enable-automation");
		options.addArguments("--no-sandbox");
		options.addArguments("--dns-prefetch-disable");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-software-rasterizer");
		options.addArguments("--disable-features=SharedStorageAPI");

		driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		db = new DatabaseClass();
		db.createDatabase();
		db.connect();
		for (String tableName : tableNames) {
			db.createTable(tableName);
		}

		try {
			driver.get("https://www.tarladalal.com/");
			driver.manage().window().maximize();

			// Click on "Recipe A To Z" link
			driver.findElement(By.xpath("//*[@id='toplinks']/a[contains(text(),'Recipe A To Z')]")).click();
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0, 700)");

			// Get total number of alphabet links
			int alphaPageSize = driver.findElements(By.xpath("//td[@onmouseover='Menu_HoverStatic(this)']")).size();

			for (int i = 2; i <= alphaPageSize; i++) {
				// Click on each alphabet link
				if (i > 2) {
					driver.findElement(By.xpath("//td[@onmouseover='Menu_HoverStatic(this)'][" + i + "]")).click();
				}
				if (i != 25) {//there is no pagination for letter 'x' with index 25
					// Get pagination links
					List<WebElement> pagination1 = driver.findElements(By.xpath("//div[contains(text(),'Goto Page')]/a"));
					WebElement lastPageElement = pagination1.get(pagination1.size() - 1);
					String paginationLastPage = lastPageElement.getText();
					last_page = Integer.parseInt(paginationLastPage);
				}

				// Iterate through each page
				for (int j = 1; j <= last_page; j++) {
					// Navigate to next page if applicable
					if (j > 1) {
						driver.findElement(By.xpath("//div[@style='text-align:right;padding-bottom:15px;'][1]/a[contains(text()," + j + ")]")).click();
					}
					// Getting All the Recipe Links
					List<WebElement> recipeLinks = null;
					try {
						recipeLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='rcc_recipename']/a")));
					} catch (Exception e) {
						logger.info("TimeoutException occurred while waiting for recipe links: " + e.getMessage());
						continue; // Skip to the next iteration if exception occurs
					}
					// Open each recipe link in a new tab
					for (WebElement link : recipeLinks) {
						String recipeUrl = link.getAttribute("href");
						((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", recipeUrl);
					}
					// Switch to each tab(recipe) and scrape data
					ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
					String parentTab = driver.getWindowHandle();
					for (String tab : tabs) {
						if (!tab.equals(parentTab)) {
							driver.switchTo().window(tab);
							try {
								recipeDataScraper(driver);
							} catch (Exception e) {
								logger.info("Error scraping data from tab: " + tab);
							}
							driver.close(); // Close the current tab
						}
					}
					driver.switchTo().window(parentTab); // Switch back to the parent tab
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			lchfEliminationRecipes = filterRecipes(allRecipesList, RecipeConstants.LCHF_TO_ELIMINATE, true);					
			lchfAddRecipes = filterRecipes(lchfEliminationRecipes, RecipeConstants.LCHF_ADD, false);
			//getting the recipes that doesnot contain processed food(LCHF recipes to avoid)
			lchfAddRecipes = lchfAddRecipes.stream().filter(rec->
			!rec.isLchfRecipesToAvoid()).collect(Collectors.toList());

			lfvEliminationRecipes = filterRecipes(allRecipesList, RecipeConstants.LFV_TO_ELIMINATE, true);		
			lfvAddRecipes = filterRecipes(lfvEliminationRecipes, RecipeConstants.LFV_ADD, false);
			//we are filtering the recipes from lfvAddRecipes to check if it contains !isLfvRecipesToAvoid(it does not contain recipes to avoid), then only those recipes are added to the same list
			lfvAddRecipes = lfvAddRecipes.stream().filter(rec->
			!rec.isLfvRecipesToAvoid()).collect(Collectors.toList());

			lfvToAddEliminationRecipes = filterRecipes(allRecipesList, RecipeConstants.LFV_TO_ELIMINATE_NFV, true);//here we get recipe list after elimination inlcuding butter
			lfvToAddRecipes = filterRecipes(lfvToAddEliminationRecipes, RecipeConstants.LFV_TO_ADD, false);//filtering recipes with butter with add ingredients
			lfvToAddRecipes.addAll(lfvAddRecipes);//adding "lfv to add recipes" with "lfv add recipes" to get partial vegan LFV recipes
			//// getting the recipes that has LFV optional recipes
			// getting recipe the list that contains herbal drinks,tea,coffee without sugar
			lfvOptionalRecipes = allRecipesList.stream().filter(rec ->
			Arrays.stream(RecipeConstants.LFV_OPTONAL_RECIPES_OPTIONS.toLowerCase().split(",")).anyMatch(rec.getRecipeName().toLowerCase()::contains))
					.collect(Collectors.toList());	
			lfvOptionalRecipes = filterRecipes(lfvOptionalRecipes, RecipeConstants.LFV_OPTONAL_RECIPES__TO_AVOID, true);//milk,sugar is excluded from ingredients
			lfvOptionalRecipes =  lfvOptionalRecipes.stream().filter(rec ->
			!Arrays.stream(RecipeConstants.LFV_OPTONAL_RECIPES__TO_ELIMINATE.toLowerCase().split(",")).anyMatch(rec.getRecipeName().toLowerCase()::contains))
					.collect(Collectors.toList());

			//to eliminate Allergy Nut Recipes from LFV To Eliminate Recipes		
			lfvNutAllergyEliminationRecipes=filterRecipes(lfvAddRecipes,RecipeConstants.LFV_NUT_ALLERGY,true);
			logger.info("******lfvEliminateNutAllergy RECIPES : " + lfvNutAllergyEliminationRecipes);

			//lfv eliminate Allergy Soy,Sesame,Egg-lfvOtherAllergyEliminationRecipes
			lfvOtherAllergyEliminationRecipes=filterRecipes(lfvAddRecipes,RecipeConstants.LFV_OTHER_ALLERGY,true);
			logger.info("******lfvEliminateNutAllergy RECIPES : " + lfvOtherAllergyEliminationRecipes);


			//to eliminate Allergy Nut Recipes from LCHF To Eliminate Recipes		
			lchfNutAllergyEliminationRecipes=filterRecipes(lchfAddRecipes,RecipeConstants.LFV_NUT_ALLERGY,true);
			logger.info("******lchfEliminateNutAllergy RECIPES : " + lchfNutAllergyEliminationRecipes);

			//lfv eliminate Allergy Soy,Sesame,Egg from LCHF to eliminate recipes
			lchfOtherAllergyEliminationRecipes=filterRecipes(lchfAddRecipes,RecipeConstants.LFV_OTHER_ALLERGY,true);
			logger.info("******lchfEliminateNutAllergy RECIPES : " + lchfOtherAllergyEliminationRecipes);

			logger.info("******lchfElimination Recipe List : " + lchfEliminationRecipes.size());
			logger.info("*****lchfAdd Recipe List : " + lchfAddRecipes.size());
			logger.info("******lfvElimination RECIPES : " + lfvEliminationRecipes.size());
			logger.info("******lfvAdd(Fully VeganLFV) RECIPES : " + lfvAddRecipes.size());
			//logger.info("******lfvToAddEliminationRecipes RECIPES : " + lfvToAddEliminationRecipes.size());
			logger.info("******PartialVeganLFV RECIPES : " + lfvToAddRecipes.size());

			insertRecipesIntoTable("recipes", allRecipesList);
			insertRecipesIntoTable("LCHFEliminatedRecipe", lchfEliminationRecipes);
			insertRecipesIntoTable("lchfAddRecipes", lchfAddRecipes);
			insertRecipesIntoTable("lfvEliminationRecipes", lfvEliminationRecipes);
			insertRecipesIntoTable("lfvAddRecipes", lfvAddRecipes);
			insertRecipesIntoTable("lfvToAddRecipes", lfvToAddRecipes);
			insertRecipesIntoTable("lfvnutallergy", lfvNutAllergyEliminationRecipes);
			insertRecipesIntoTable("lfvotherallergy", lfvOtherAllergyEliminationRecipes);
			insertRecipesIntoTable("lchfnutallergy", lchfNutAllergyEliminationRecipes);
			insertRecipesIntoTable("lchfotherallergy", lchfOtherAllergyEliminationRecipes);
			insertRecipesIntoTable("lfvOptionalRecipes", lfvOptionalRecipes);

			if (driver != null) {
				driver.quit();
			}
		}
	}

	public void recipeDataScraper(WebDriver driver) throws JsonParseException, JsonMappingException, IOException, SQLException {

		boolean lfvRecipesToAvoid = false ;
		boolean lchfRecipesToAvoid = false;
		//extracting recipe id from the current url
		String recipeUrl = driver.getCurrentUrl();
		//Split the URL by hyphen and 'r' to get the parts
		String[] parts = recipeUrl.split("-");
		// The recipe ID is the last part before 'r'
		String recipeId = parts[parts.length - 1].replace("r", "");
		logger.info("*************************************************");
		logger.info("Recipe Id : " + recipeId);

		//Getting the recipe name
		WebElement recipeTitleElement = driver.findElement(By.xpath("//div[@id='recipehead']//span//span"));
		String recipeTitle = recipeTitleElement.getText();
		//scraping "LFV recipes to avoid" by filtering the constants like (microwave,fried,...)from the recipe titile, if it finds any match it returns true.
		lfvRecipesToAvoid = Arrays.stream(RecipeConstants.LFV_RECIPES_TO_AVOID).anyMatch(recipeTitle.toLowerCase()::contains);

		logger.info("Recipe Name : " + recipeTitle);
		//getting preparation time
		String preperationTime = driver.findElement(By.xpath("//time[@itemprop='prepTime']")).getText();
		logger.info("Preparation Time : " + preperationTime);
		//getting cooking time
		String cookingTime = driver.findElement(By.xpath("//time[@itemprop='cookTime']")).getText();
		logger.info("Cooking Time : " + cookingTime);
		// Getting Ingredients
		List<WebElement> ingredintsLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']"));
		String ingredients = "";
		for (WebElement e1 : ingredintsLoc) {
			if(!lchfRecipesToAvoid) {
				lchfRecipesToAvoid = e1.getText().toLowerCase().contains("processed");	
			}
			ingredients = ingredients + " " + e1.getText();
		}
		logger.info("Ingredients : " + ingredients);

		List<WebElement> ingredintsNameLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']/a/span"));
		String ingredientsName = "";
		for (WebElement e1 : ingredintsNameLoc) {
			ingredientsName = ingredientsName + "\n" + e1.getText();
		}
		logger.info("Ingredients Name : " + ingredientsName);

		//Getting No of Servings
		String numOfServings = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblServes']")).getText();
		logger.info("No of Servings : "+numOfServings);

		//Getting tags
		List<WebElement> tagsLoc = driver.findElements(By.xpath("//div[@id='recipe_tags']/a"));
		String tags = "";
		for (WebElement tag : tagsLoc) {
			//scraping "LFV recipes to avoid" by filtering the constants like (microwave,fried,...)from each recipe tag, if it finds any match it returns true and stops checking the condition.
			if(!lfvRecipesToAvoid) {
				lfvRecipesToAvoid = Arrays.stream(RecipeConstants.LFV_RECIPES_TO_AVOID).anyMatch(tag.getText().toLowerCase()::contains);	
			}
			tags = tags + " " + tag.getText();
		}
		logger.info("Recipe Tags : " + tags);

		//Getting Recipe Description
		String recipeDescription = driver.findElement(By.id("recipe_description")).getText();
		logger.info("Recipe Description : " + recipeDescription );

		//Getting Preperation Method
		List<WebElement> prepMethod = driver.findElements(By.xpath("//*[@id='recipe_small_steps']/span[1]//span[@itemprop='text']"));
		String preparationMethod = "";
		for (WebElement method : prepMethod) {
			preparationMethod = preparationMethod + " " + method.getText();
		}
		logger.info("Preparation Method : " + preparationMethod );

		//Getting Nutrition values
		List<WebElement> nutritionLoc = driver.findElements(By.xpath("//*[@id='rcpnutrients']//tr"));
		String nutritionValues = "";
		for (WebElement nutrition : nutritionLoc) {
			nutritionValues = nutritionValues + " " + nutrition.getText();
		}
		logger.info("Nutrition Values : " + nutritionValues );

		//Recipe Url
		logger.info("Recipe Url: " + recipeUrl);

		// Determining the food category based on the tags and ingredients
		String foodCategory = "Vegetarian";//by default food category is vegetarian
		String combinedText = (tags + ingredientsName).toLowerCase();//combining tags and ingredientname for filtering
		//using streams to check if there is any match with the ingredients in arraylist and the string
		boolean isEggetarian = !Arrays.stream(RecipeConstants.EGGETARION_ELEMINATE_OPTIONS).anyMatch(combinedText::contains);
		boolean isVegan = !Arrays.stream(RecipeConstants.VEGAN_EMINATE_OPTIONS).anyMatch(combinedText::contains);
		if(combinedText.contains("egg") && isEggetarian ){
			foodCategory = "Eggetarian"; 
		} else if(combinedText.contains("jain")) { 
			foodCategory = "Jain"; 
		} else if(isVegan || combinedText.contains("vegan")
				||recipeUrl.contains("vegan")
				){ 
			foodCategory = "Vegan"; 
		} 
		logger.info("Food Category : " + foodCategory );

		//Getting Cuisine Category
		String cuisineCategory = "";
		String combinedText2 = (tags + recipeTitle).toLowerCase();
		String[] cuisineList = RecipeConstants.CUISIN_CATEGORY.split(",");
		for(String e1 : cuisineList) { 
			if((combinedText2.toLowerCase()).contains(e1.toLowerCase())) {
				cuisineCategory = e1 + ", " +cuisineCategory; 
			}
		} 
		logger.info("Cuisine Category:" + cuisineCategory); 

		//Getting Recipe Category
		String recipeCategory = "";
		for(String recipeCategoryOption : RecipeConstants.RECIPE_CATEGORY_OPTIONS) {
			if(tags.toLowerCase().contains(recipeCategoryOption.toLowerCase()))
			{
				recipeCategory = recipeCategoryOption ;
				break;
			}
		}
		logger.info("Recipe Category:" + recipeCategory); 

		Recipe recipe = new Recipe(recipeId, recipeTitle, recipeDescription, ingredients,preperationTime, cookingTime,preparationMethod, numOfServings, cuisineCategory, foodCategory, recipeCategory,tags, nutritionValues, recipeUrl);
		recipe.setRecipeID(recipeId);
		recipe.setRecipeName(recipeTitle);
		recipe.setIngredientsName(ingredientsName);
		recipe.setIngredients(ingredients);
		recipe.setPreperationTime(preperationTime);
		recipe.setCookingTime(cookingTime);
		recipe.setNumOfServings(numOfServings);
		recipe.setRecipeDescription(recipeDescription);
		recipe.setPreparationMethod(preparationMethod);
		recipe.setNutritionValues(nutritionValues);
		recipe.setTags(tags);
		recipe.setCuisineCategory(cuisineCategory);
		recipe.setRecipeUrl(recipeUrl);
		recipe.setFoodCategory(foodCategory);
		recipe.setRecipeCategory(recipeCategory);
		recipe.setLfvRecipesToAvoid(lfvRecipesToAvoid);

		allRecipesList.add(recipe);	

	}

	public void insertRecipesIntoTable(String tableName, List<Recipe> recipes) throws SQLException {
		for (Recipe recipe : recipes) {
			db.insertData(tableName, recipe.getRecipeID(), recipe.getRecipeName(), recipe.getRecipeDescription(),
					recipe.getIngredients(), recipe.getPreperationTime(), recipe.getCookingTime(),
					recipe.getPreparationMethod(), recipe.getNumOfServings(), recipe.getCuisineCategory(),
					recipe.getFoodCategory(), recipe.getRecipeCategory() ,recipe.getTags(), recipe.getNutritionValues(), recipe.getRecipeUrl());
		}
	}
	/**
	 * filtering recipe list based on ingredient names eliminatiom and add ingrediant conditions
	 * @param recipeList
	 * @param filterString
	 * @param toBeNotIncluded
	 * @return
	 */
	public List<Recipe> filterRecipes(List<Recipe> recipeList,String filterString, boolean toBeNotIncluded)
	{
		List<Recipe> filteredRecipes = null;
		//using java streams(lambda expression) for filtering data
		//using streams to check if there is any match with the ingredients in array list and the string
		if(toBeNotIncluded) {
			filteredRecipes	= recipeList.stream().filter(rec ->
			!Arrays.stream(filterString.toLowerCase().split(",")).anyMatch(rec.getIngredientsName().toLowerCase()::contains))
					.collect(Collectors.toList());
		}
		else {
			filteredRecipes = recipeList.stream().filter(rec ->
			Arrays.stream(filterString.toLowerCase().split(",")).anyMatch(rec.getIngredientsName().toLowerCase()::contains))
					.collect(Collectors.toList());
		}
		logger.info("Filtered Recipes: " + filteredRecipes.size() + " for filter: " + filterString);
		return filteredRecipes;
	}
}
