package io.flood.selenium;

import java.lang.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import com.google.common.base.Predicate;
import org.openqa.selenium.support.ui.FluentWait;

public class FloodSump {
  private int width = 0, height = 0 ;

  public FloodSump()
  {
  }

  public FloodSump( int initWidth, int initHeight )
  {
  }

  public void started() {
    System.out.println("test started");
  }

  public void finished() {
    System.out.println("test finished");
  }

  public void passed_transaction(WebDriver driver, String label, Integer responseCode, Double responseTime) {
    transaction(driver, label, "true", responseCode, responseTime);
  }

  public void passed_transaction(WebDriver driver, String label, Integer responseCode) {
    transaction(driver, label, "true", responseCode, null);
  }

  public void passed_transaction(WebDriver driver, String label) {
    transaction(driver, label, "true", 200, null);
  }

  public void passed_transaction(WebDriver driver) {
    transaction(driver, driver.getTitle(), "true", 200, null);
  }

  public void failed_transaction(WebDriver driver, String label, Integer responseCode, Double responseTime) {
    transaction(driver, label, "false", responseCode, responseTime);
  }

  public void failed_transaction(WebDriver driver, String label, Integer responseCode) {
    transaction(driver, label, "false", responseCode, null);
  }

  public void failed_transaction(WebDriver driver, Integer responseCode) {
    transaction(driver, driver.getTitle(), "false", responseCode, null);
  }

  public void failed_transaction(WebDriver driver) {
    transaction(driver, driver.getTitle(), "false", 500, null);
  }

  public void get_mark(WebDriver driver, String mark) {
    waitForUserTiming(driver, "mark", mark);

    JavascriptExecutor js = (JavascriptExecutor)driver;
    double responseTime = new Double(js.executeScript("return window.performance.getEntriesByName('" + mark +"')[0].startTime;").toString());

    transaction(driver, mark, "true", 200, responseTime);
  }

  public void get_measure(WebDriver driver, String measure) {
    waitForUserTiming(driver, "measure", measure);

    JavascriptExecutor js = (JavascriptExecutor)driver;
    double responseTime = new Double(js.executeScript("return window.performance.getEntriesByName('" + measure +"')[0].duration;").toString());

    transaction(driver, measure, "true", 200, responseTime);
  }

  protected void waitForUserTiming(final WebDriver driver, final String type, final String name) {
    final JavascriptExecutor js = (JavascriptExecutor)driver;

    new FluentWait<JavascriptExecutor>(js) {
        protected RuntimeException timeoutException(String message, Throwable lastException) {
          transaction(driver, name, "false", null, null);
          return null;
        }
      }.
      withMessage("UserTiming API timed out waiting for " + name).
      withTimeout(30 ,TimeUnit.SECONDS).
      pollingEvery(1 ,TimeUnit.SECONDS).
      until(new Predicate<JavascriptExecutor>(){
        public boolean apply(JavascriptExecutor e) {
        return (Boolean)js.executeScript(
          "return window.performance.getEntriesByType('" + type + "').length > 0;");
      }
    });
  }

  private static boolean isNotNullOrEmpty(String str){
    return (str != null && !str.isEmpty());
  }

  public String getValueOrDefault(String value, String defaultValue) {
    return isNotNullOrEmpty(value) ? value : defaultValue;
  }

  public void transaction(WebDriver driver, String label, String successful, Integer responseCode, Double responseTime) {
    JavascriptExecutor js = (JavascriptExecutor)driver;

    ConcurrentMap data = new ConcurrentHashMap();

    long loadEventEnd = (Long)js.executeScript("return window.performance.timing.loadEventEnd;");
    long navigationStart = (Long)js.executeScript("return window.performance.timing.navigationStart;");
    long responseStart = (Long)js.executeScript("return window.performance.timing.responseStart;");
    long connectStart = (Long)js.executeScript("return window.performance.timing.connectStart;");

    data.put("timestamp", String.valueOf(loadEventEnd));
    data.put("start_time", String.valueOf(navigationStart));
    data.put("end_time", String.valueOf(loadEventEnd));
    data.put("source_host", getValueOrDefault(System.getenv("HOSTNAME"), "localhost"));
    data.put("response_time", (responseTime == null) ? String.valueOf(loadEventEnd - navigationStart) : String.format("%.0f", responseTime));
    data.put("latency", String.valueOf(responseStart - navigationStart));
    data.put("connect_time", String.valueOf(connectStart - navigationStart));
    data.put("label", String.valueOf(label));
    data.put("url", driver.getCurrentUrl());
    data.put("uuid", System.getProperty("uuid"));
    data.put("successful", (successful.equals("false")) ? false : true);
    data.put("error_count", (successful.equals("false")) ? "1" : "0");
    data.put("sample_count", "1");
    data.put("thread_id", getValueOrDefault(System.getenv("THREAD_ID"), "1"));
    data.put("active_threads", getValueOrDefault(System.getenv("ACTIVE_THREADS"), "1"));
    data.put("active_threads_in_group", getValueOrDefault(System.getenv("ACTIVE_THREADS"), "1"));
    data.put("request_headers", "");
    data.put("response_headers", "");
    data.put("request_data", "");
    // data.put("response_data", (successful.equals("false")) ? driver.getPageSource() : "");
    data.put("response_code", getValueOrDefault(String.valueOf(responseCode), ""));
    data.put("bytes", getValueOrDefault( String.valueOf(driver.getPageSource().length()), ""));

    System.out.println("Transaction: " + String.valueOf(label));
    System.out.println(data);
    System.out.println("---");
  }
}
