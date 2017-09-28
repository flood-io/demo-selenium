import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;

import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.openqa.selenium.support.ui.Select;

import io.flood.selenium.FloodSump;

public class ChallengerWindows  {
  public static void main(String[] args) throws Exception {
    int iterations = 0;

    // Create a new instance of the html unit driver
    // Notice that the remainder of the code relies on the interface,
    // not the implementation.
    System.setProperty("webdriver.chrome.driver", "C:/Webdriver/chromedriver.exe");

    WebDriver driver = new RemoteWebDriver(new URL("http://" + System.getenv("WEBDRIVER_HOST") + ":" + System.getenv("WEBDRIVER_PORT") + "/wd/hub"), DesiredCapabilities.chrome());
    JavascriptExecutor js = (JavascriptExecutor)driver;

    // Create a new instance of the Flood IO agent
    FloodSump flood = new FloodSump();

    // Inform Flood IO the test has started
    flood.started();

    // It's up to you to control test duration / iterations programatically.
    while( iterations < 5 ) {
      try {
        System.out.println("Starting iteration " +  String.valueOf(iterations));

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        // And now use this to visit the target site
        driver.get("https://challengers.flood.io/");

        // Log a passed transaction in Flood IO
        flood.passed_transaction(driver);

        driver.findElement(By.cssSelector("input[type='submit'][value='Start']")).click();

        // Log a passed transaction with custom label
        Select ageDropDown = new Select(driver.findElement(By.id("challenger_age")));
        ageDropDown.selectByVisibleText("30");
        driver.findElement(By.cssSelector("input[type='submit'][value='Next']")).click();

        flood.passed_transaction(driver, "Challenge Step 2");

        // Log a failed transaction
        driver.findElement(By.cssSelector("input[type='submit'][value='Bingo']")).click();

        iterations++;

        // Good idea to introduce some form of pacing / think time into your scripts
        Thread.sleep(1000);
      } catch (WebDriverException e) {
        String[] lines = e.getMessage().split("\\r?\\n");
        System.err.println("Webdriver exception: " + lines[0]);
        flood.failed_transaction(driver, "Challenge Step 3 Failed", 400);
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
        String[] lines = e.getMessage().split("\\r?\\n");
        System.err.println("Browser terminated early: " + lines[0]);
      } catch(Exception e) {
        String[] lines = e.getMessage().split("\\r?\\n");
        System.err.println("Other exception: " + lines[0]);
      } finally {
        iterations++;
      }
    }

    driver.quit();

    // Inform Flood IO the test has finished
    flood.finished();
  }
}
