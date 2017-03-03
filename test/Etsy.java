import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.openqa.selenium.support.ui.Select;

import io.flood.selenium.FloodSump;

public class Etsy  {
  public static void main(String[] args) throws Exception {
    int iterations = 0;

    // Create a new instance of the html unit driver
    // Notice that the remainder of the code relies on the interface,
    // not the implementation.
    WebDriver driver = new RemoteWebDriver(new URL("http://" + System.getenv("WEBDRIVER_HOST") + ":" + System.getenv("WEBDRIVER_PORT") + "/wd/hub"), DesiredCapabilities.chrome());
    JavascriptExecutor js = (JavascriptExecutor)driver;

    // Create a new instance of the Flood IO agent
    FloodSump flood = new FloodSump();

    // Inform Flood IO the test has started
    flood.started();

    // It's up to you to control test duration / iterations programatically.
    while( iterations < 1 ) {
      try {
        // And now use this to visit the target site
        driver.get("https://www.etsy.com/");

        // Log a passed transaction in Flood IO
        flood.passed_transaction(driver);

        // Log a custom mark from the User Timing API
        flood.get_mark(driver, "timer_loadstart_jquery");

        iterations++;

        // Good idea to introduce some form of pacing / think time into your scripts
        Thread.sleep(5000);
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
        String[] lines = e.getMessage().split("\\r?\\n");
        System.err.println("Browser terminated early: " + lines[0]);
      } catch (WebDriverException e) {
        String[] lines = e.getMessage().split("\\r?\\n");
        System.err.println(lines[0]);
      }
    }

    driver.quit();

    // Inform Flood IO the test has finished
    flood.finished();
  }
}
