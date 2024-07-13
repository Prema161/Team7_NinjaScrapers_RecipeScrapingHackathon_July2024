package com.recipescrapers.main;

import org.testng.annotations.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

public class RecipeScraperTest {

	int last_page;
	WebDriver driver;
	List<Recipe> allRecipesList = new ArrayList();
	List lchfRecipeList;

	@Test
	public void RecipeScrape() {

		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--blink-settings=imagesEnabled=false");//disabling the images
		options.addArguments("--disable-images");
		options.addArguments("--disable-javascript");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--headless");//executing in headless mode
		options.addArguments("--disable-popup-blocking");
		options.addArguments("-–disable-notifications");
		options.addArguments("-–disable-extensions");

		driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		try {
			driver.get("https://www.tarladalal.com/");
			driver.manage().window().maximize();

			// Click on "Recipe A To Z" link
			driver.findElement(By.xpath("//*[@id='toplinks']/a[contains(text(),'Recipe A To Z')]")).click();
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0, 700)");

			// Get total number of alphabet links
			int alphaPageSize = driver.findElements(By.xpath("//td[@onmouseover='Menu_HoverStatic(this)']")).size();
			for (int i = 2; i <=4; i++) {
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
				for (int j = 1; j <= 1; j++) {
					// Navigate to next page if applicable
					if (j > 1) {
						driver.findElement(By.xpath("//div[@style='text-align:right;padding-bottom:15px;'][1]/a[contains(text()," + j + ")]")).click();
					}

					// Wait for recipe links to be present
					//List<WebElement> recipeLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='rcc_recipename']/a")));
					// Getting All the Recipe Links
					List<WebElement> recipeLinks = null;
					try {
						recipeLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='rcc_recipename']/a")));
					} catch (Exception e) {
						System.out.println("TimeoutException occurred while waiting for recipe links: " + e.getMessage());
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
								System.out.println("Error scraping data from tab: " + tab);
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
			//using java streams(lambda expression) for filtering data
			//using contains any method from collectionutils to filter the ingredients from two arraylist(converted ingredients from arrays to arraylist)
			lchfRecipeList = allRecipesList.stream().filter(rec ->
			!CollectionUtils.containsAny(Arrays.asList(rec.getIngredients().toLowerCase().split(",")),Arrays.asList(RecipeConstants.LCHFToEliminate.toLowerCase().split(",")))).collect(Collectors.toList());
			System.out.println("LCHF Recipe List : " + lchfRecipeList);

			if (driver != null) {
				driver.quit();// closing driver at the end
			}
		}

	}
	public void recipeDataScraper(WebDriver driver) {

		//Getting Recipe Url;
		String recipeUrl = driver.getCurrentUrl();
		//extracting recipe id from the current url
		//Split the URL by hyphen and 'r' to get the parts
		String[] parts = recipeUrl.split("-");
		// The recipe ID is the last part before 'r'
		String recipeId = parts[parts.length - 1].replace("r", "");
		System.out.println("*************************************************");
		System.out.println("Recipe Id : " + recipeId);
		Recipe recipe = new Recipe();
		recipe.setRecipeID(recipeId);

		//Getting the recipe name
		WebElement recipeTitleElement = driver.findElement(By.xpath("//div[@id='recipehead']//span//span"));
		String recipeTitle = recipeTitleElement.getText();
		System.out.println("Recipe Name : " + recipeTitle);
		recipe.setRecipeName(recipeTitle);
		//getting preparation time
		String preperationTime = driver.findElement(By.xpath("//time[@itemprop='prepTime']")).getText();
		System.out.println("Preparation Time : " + preperationTime);

		//getting cooking time
		String cookingTime = driver.findElement(By.xpath("//time[@itemprop='cookTime']")).getText();
		System.out.println("Cooking Time : " + cookingTime);

		// Getting Ingredients
		List<WebElement> ingredintsLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']"));

		String ingredients = "";
		for (WebElement ingredient : ingredintsLoc) {
			ingredients = ingredients + "," + ingredient.getText();
		}
		System.out.println("Ingredients : " + ingredients);

		List<WebElement> ingredintsNameLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']/a/span"));

		String ingredientsName = "";
		for (WebElement ingredientName : ingredintsNameLoc) {
			ingredientsName = ingredientsName + "," + ingredientName.getText();
		}
		System.out.println("Ingredients Name : " + ingredientsName);
		recipe.setIngredients(ingredientsName);
		allRecipesList.add(recipe);

		//Getting No of Servings
		String numOfServings = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblServes']")).getText();
		System.out.println("No of Servings : "+numOfServings);

		//Getting tags
		List<WebElement> tagsLoc = driver.findElements(By.xpath("//div[@id='recipe_tags']/a"));
		String tags = "";
		for (WebElement tag : tagsLoc) {
			tags = tags + "\n" + tag.getText();
		}
		System.out.println("Recipe Tags : " + tags);

		//Getting Recipe Description
		String recipeDescription = driver.findElement(By.id("recipe_description")).getText();
		System.out.println("Recipe Description : " + recipeDescription );

		//Getting Preperation Method
		List<WebElement> prepMethod = driver.findElements(By.xpath("//*[@id='recipe_small_steps']/span[1]//span[@itemprop='text']"));
		String preparationMethod = "";
		for (WebElement method : prepMethod) {
			preparationMethod = preparationMethod + "\n" + method.getText();
		}
		System.out.println("Preparation Method : " + preparationMethod );

		//Getting Nutrition values
		List<WebElement> nutritionLoc = driver.findElements(By.xpath("//*[@id='rcpnutrients']//tr"));
		String nutritionValues = "";
		for (WebElement nutrition : nutritionLoc) {
			nutritionValues = nutritionValues + "\n" + nutrition.getText();
		}
		System.out.println("Nutrition Values : " + nutritionValues );

		//Recipe Url
		System.out.println("Recipe Url: " + recipeUrl);
		
		// Determining the food category based on the tags and ingredients
		String foodCategory = "Vegetarian";//by default food category is vegetarian
		String combinedText = (tags + ingredientsName).toLowerCase();//combining tags and ingredientname for filtering
		//using streams to check if there is any match with the ingredients in arraylist and the string
		boolean isEggetarian = !Arrays.stream(RecipeConstants.eggetarianEliminateOptions).anyMatch(combinedText::contains);
		boolean isVegan = !Arrays.stream(RecipeConstants.veganEliminateOptions).anyMatch(combinedText::contains);
		if(combinedText.contains("egg") && isEggetarian ){
			foodCategory = "Eggetarian"; 
		} else if(combinedText.contains("jain")) { 
			foodCategory = "Jain"; 
		}
		else if(isVegan || combinedText.contains("vegan")
				||recipeUrl.contains("vegan")
				){ 
			foodCategory = "Vegan"; 
		} 
		System.out.println("Food Category : " + foodCategory );

		//Getting Recipe Category (breakfast,lunch,snack,dinner)


	}

}
