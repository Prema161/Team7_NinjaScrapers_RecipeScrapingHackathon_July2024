package com.recipescrapers.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;

public class RecipeScraperTest {
	
	public static void main(String[] args) throws IOException {
		
		WebDriverManager.chromedriver().setup();
	    WebDriver  driver = new ChromeDriver();
	    
	   ObjectMapper om = new ObjectMapper();
	   EliminatorsAndToAdd eliminator = om.readValue(new File("src/test/resources/list.json"),EliminatorsAndToAdd.class);
	    
	    driver.get("https://www.tarladalal.com/");
	    driver.manage().window().maximize();
	    
	    driver.findElement(By.xpath("//a[text()='Recipe A To Z']")).click();
	    JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0, 700)");
		
		
		 
		for (char i = 'A'; i <= 'B'; i++) {
		int	next_page=1;
		String paginationSelector1 = eliminator.url + i + "&pageindex="+ next_page;
		  driver.get(paginationSelector1);
			
		    
		    List<WebElement> pagination1 = driver.findElements(By.xpath("//div[contains(text(),'Goto Page')]/a"));
		    System.out.println(pagination1.size());

		    WebElement element = pagination1.get(pagination1.size() - 1);

		    String pagination_last_page = element.getText();
		    System.out.println(pagination_last_page); // last page number for i
		   
		    int last_page = Integer.parseInt(pagination_last_page);

		    for (int j = 1; j < last_page; j++) {
		        String paginationSelector = eliminator.url + i + "&pageindex=" + j;
		       // System.out.println(j);
		        driver.get(paginationSelector);
		        
		       
		    }
		}

	  }}
	
	    
     

// List<WebElement> recipes = driver.findElements(By.className("rcc_recipename"));
       
      // for(int i=0; i<recipes.size();i++) {
    	   
    	// recipes.get(i).click();
    	 
       
	/*	for (char k = 'A'; k <= 'B'; k++) {
			  int next_page = 1;
		String paginationSelector = "https://www.tarladalal.com/RecipeAtoZ.aspx?beginswith=" + k + "&pageindex="+ next_page;
			  driver.get(paginationSelector);

	List<WebElement> pages_full_data = driver.findElements(By.xpath("//div[contains(text(),'Goto Page')]/a"));
				if (pages_full_data.isEmpty()) {
						System.out.println("No recipe found");
						continue;
					}

					System.out.println(pages_full_data.size());//30
					WebElement element_1_XX = pages_full_data.get(pages_full_data.size() - 1);

					String pagenation_last_page_XX = element_1_XX.getText();
					System.out.println( pagenation_last_page_XX);//22
					int last_page_number = Integer.parseInt(pagenation_last_page_XX);
					System.out.println("Page Data: " + "for alphabet " + k + " " + pagenation_last_page_XX);

					for (int j = 0; j <= last_page_number - 1; j++) {
						paginationSelector = "https://www.tarladalal.com/RecipeAtoZ.aspx?beginswith=" + k + "&pageindex="
								+ next_page;
						driver.get(paginationSelector);
	 * 
	 * 
	 */
		
