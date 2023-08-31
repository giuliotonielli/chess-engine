package webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import main.Move;
import java.time.Duration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;


public class Webdriver {
	WebDriver driver;
	String color = "w";
	Hashtable<Double, Character> valueToLetter = new Hashtable<Double, Character>();
	
	public void start() {
		setHashtableUp();
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*");
		driver = new ChromeDriver(options);
		
	    driver.get("https://www.chess.com/play/online");
	     
	   //driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
	    
	    // click on login
	    driver.findElement(By.xpath("/html/body/div[1]/div[9]/div[3]/a[9]")).click();
	    
	    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
	    
	    // username, password and submit
	    try {
	    	// login form 1
	    	driver.findElement(By.xpath("/html/body/div[32]/div/div/div[1]/div[2]/form/div[1]/div/input")).sendKeys("enpassantmate1");
	    	driver.findElement(By.xpath("/html/body/div[32]/div/div/div[1]/div[2]/form/div[2]/div/input")).sendKeys("q$g*c,%t!suBCE2");
		    driver.findElement(By.xpath("/html/body/div[32]/div/div/div[1]/div[2]/form/button")).click();
	    }
	    catch(Exception e) {
	    	 // login form 2
	    	driver.findElement(By.xpath("/html/body/div[1]/div/main/div/form/div[1]/input")).sendKeys("enpassantmate1");
		    driver.findElement(By.xpath("/html/body/div[1]/div/main/div/form/div[2]/input")).sendKeys("q$g*c,%t!suBCE2");
		    driver.findElement(By.xpath("/html/body/div[1]/div/main/div/form/button")).click();
	    }
	    ///html/body/div[1]/div/main/div/form/div[1]/input
	    //driver.findElement(By.xpath("/html/body/div[31]/div/div/div[1]/div[2]/form/div[1]/div/input")).sendKeys("enpassantmate1");
	    
	    // submit password
	    //html/body/div[1]/div/main/div/form/div[2]/input
	    //driver.findElement(By.xpath("/html/body/div[31]/div/div/div[1]/div[2]/form/div[2]/div/input")).sendKeys("q$g*c,%t!suBCE2");
	    
	    // submit button
	    ///html/body/div[1]/div/main/div/form/button
	    //driver.findElement(By.xpath("/html/body/div[31]/div/div/div[1]/div[2]/form/button")).click();
	    
	    // click on play game
	    driver.findElement(By.xpath("/html/body/div[4]/div/div[2]/div/div[1]/div[1]/button")).click();
	
//	     WebElement message = driver.findElement(By.id("message"));
//	     String value = message.getText();
//	     assertEquals("Received!", value);

	    // driver.quit();
	
	}
	
	private void setHashtableUp() {
		valueToLetter.put(1.0, 'p');
		valueToLetter.put(-1.0, 'p');
		valueToLetter.put(3.0, 'n');
		valueToLetter.put(-3.0, 'n');
		valueToLetter.put(3.2, 'b');
		valueToLetter.put(-3.2, 'b');
		valueToLetter.put(5.0, 'r');
		valueToLetter.put(-5.0, 'r');
		valueToLetter.put(9.0, 'q');
		valueToLetter.put(-9.0, 'q');
		valueToLetter.put(100.0, 'k');
		valueToLetter.put(-100.0, 'k');
	}

	public int getColor() {
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));
	    String element;
	    
		try {
			element = driver.findElement(By.xpath("/html/body/div[3]/div[1]/div/div[4]")).getAttribute("class");
		}
		catch (Exception e) {
			return getColor();
		}
		
	    if (element.contains("w")) {
	    	color = "b";
	    	return -1;
	    }
	    
	    else 
	    	return 1;
	}

	public String getOpponentsMove() {
		String elementTo, elementFrom;
		
		wait(1);
		
		while (driver.findElement(By.xpath("/html/body/div[3]/div[1]/div/div[4]")).getAttribute("class").contains("y")) {
			wait(1);
		}
		
		elementFrom = driver.findElement(By.xpath("/html/body/div[3]/div[2]/chess-board/div[2]")).getAttribute("class");
		elementTo = driver.findElement(By.xpath("/html/body/div[3]/div[2]/chess-board/div[3]")).getAttribute("class");
			
		try {
			return "" + elementFrom.substring(17, 19) + elementTo.substring(17, 19);
		} catch (Exception e) {
			return getOpponentsMove();
		}
	}

	private void wait(int t) {
		try {
			TimeUnit.SECONDS.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendMoveToDriver(Move move) {
		String p = color + valueToLetter.get(move.value);
		String d = "square-" + (move.a+1) + "" + (8-move.b);
		int offsetX, offsetY;
		
		WebElement piece = driver.findElement(By.cssSelector(".piece."+p+"."+d));
		piece.click();
		
		int pieceSize = piece.getSize().height;
		
		offsetY = (move.d - move.b)*pieceSize;
		
		// move pointer by offset and click
		if (color == "w") {
			offsetX = (move.c - move.a)*pieceSize;
			new Actions(driver).moveToElement(piece).moveByOffset(offsetX, offsetY).click().perform();
		}
		else {
			offsetX = (move.a - move.c)*pieceSize;
			new Actions(driver).moveToElement(piece).moveByOffset(offsetX, -offsetY).click().perform();
		}
	}
}

