package com.recipescrapers.main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.testng.annotations.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;


public class RecipeScraperTest {

	int last_page;
	WebDriver driver;
	DatabaseClass db;

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

		driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		db = new DatabaseClass();
		db.createDatabase();
		db.connect();
		db.createTable();
		 
		
		try {
			driver.get("https://www.tarladalal.com/");
			driver.manage().window().maximize();


			// Click on "Recipe A To Z" link
			driver.findElement(By.xpath("//*[@id='toplinks']/a[contains(text(),'Recipe A To Z')]")).click();
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0, 700)");

			// Get total number of alphabet links
			int alphaPageSize = driver.findElements(By.xpath("//td[@onmouseover='Menu_HoverStatic(this)']")).size();

			for (int i = 2; i < alphaPageSize; i++) {
				// Click on each alphabet link
				if (i > 2) {
					driver.findElement(By.xpath("//td[@onmouseover='Menu_HoverStatic(this)'][" + i + "]")).click();
				}

				if (i != 25) {
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
			if (driver != null) {
				driver.quit();// closing driver at the end
			}
		}

	}
	public void recipeDataScraper(WebDriver driver) {

		//System.out.println("Extracting data from: " + driver.getCurrentUrl());

		//extracting recipe id from the current url
		String currentUrl = driver.getCurrentUrl();
		//Split the URL by hyphen and 'r' to get the parts
		String[] parts = currentUrl.split("-");
		// The recipe ID is the last part before 'r'
		String recipeId = parts[parts.length - 1].replace("r", "");
		System.out.println("*************************************************");
		System.out.println("Recipe Id : " + recipeId);

		//Getting the recipe name
		WebElement recipeTitleElement = driver.findElement(By.xpath("//div[@id='recipehead']//span//span"));
		String recipeTitle = recipeTitleElement.getText();
		System.out.println("Recipe Name : " + recipeTitle);

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
		for (WebElement e1 : ingredintsLoc) {
			ingredients = ingredients + "\n" + e1.getText();
		}
		System.out.println("Ingredients : " + ingredients);

		List<WebElement> ingredintsNameLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']/a/span"));

		String ingredientsName = "";
		for (WebElement e1 : ingredintsNameLoc) {
			ingredientsName = ingredientsName + "\n" + e1.getText();
		}
		System.out.println("Ingredients Name : " + ingredientsName);

		//Getting Cuisine Category
		String cuisineCategory = driver.findElement(By.xpath("//div[@class='breadcrumb']/span[7]/a/span")).getText();
		System.out.println(cuisineCategory);

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
			if (driver != null) {
				driver.quit();// closing driver at the end
			}
			//db.closeConnection();
		}

	}
	public void recipeDataScraper(WebDriver driver) throws JsonParseException, JsonMappingException, IOException, SQLException {
		
		 //ObjectMapper om = new ObjectMapper();
	      //  EliminatorsAndToAdd eliminator = om.readValue(new File("src/test/resources/list.json"), EliminatorsAndToAdd.class);

	        
		//System.out.println("Extracting data from: " + driver.getCurrentUrl());

		//extracting recipe id from the current url
		String currentUrl = driver.getCurrentUrl();
		//Split the URL by hyphen and 'r' to get the parts
		String[] parts = currentUrl.split("-");
		// The recipe ID is the last part before 'r'
		String recipeId = parts[parts.length - 1].replace("r", "");
		System.out.println("*************************************************");
		System.out.println("Recipe Id : " + recipeId);

		//Getting the recipe name
		WebElement recipeTitleElement = driver.findElement(By.xpath("//div[@id='recipehead']//span//span"));
		String recipeTitle = recipeTitleElement.getText();
		System.out.println("Recipe Name : " + recipeTitle);

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
		for (WebElement e1 : ingredintsLoc) {
			ingredients = ingredients + "\n" + e1.getText();
		}
		System.out.println("Ingredients : " + ingredients);

		List<WebElement> ingredintsNameLoc = driver
				.findElements(By.xpath("//span[@itemprop='recipeIngredient']/a/span"));

		String ingredientsName = "";
		for (WebElement e1 : ingredintsNameLoc) {
			ingredientsName = ingredientsName + "\n" + e1.getText();
		}
		System.out.println("Ingredients Name : " + ingredientsName);

		//Getting Cuisine Category
		String cuisineCategory = driver.findElement(By.xpath("//div[@class='breadcrumb']/span[7]/a/span")).getText();
		System.out.println(cuisineCategory);

		//Getting No of Servings
		String numOfServings = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblServes']")).getText();
		System.out.println("No of Servings : "+numOfServings);
	
	db.insertRecipeData(recipeId, recipeTitle, preperationTime, cookingTime, ingredients, cuisineCategory, numOfServings);

	}
}
