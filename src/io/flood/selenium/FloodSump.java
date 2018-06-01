package io.flood.selenium;

import org.json.simple.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import com.google.common.base.Predicate;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.WebDriverException;

import java.lang.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.ByteArrayOutputStream;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.net.*;
import java.net.URLEncoder;

public class FloodSump {
  private DatagramSocket socket;
  private InetAddress address;

  private String account;
  private String region;
  private String grid;
  private String node;
  private String flood;
  private String project;
  private String host;
  private String port;
  private String name;
  private String transactionName;

  private static Logger log = Logger.getLogger(FloodSump.class.getName());

  public void started() {
    try {
      name = getValueOrDefault(System.getenv("FLOOD_SUMP_NAME"), "results");
      host = getValueOrDefault(System.getenv("FLOOD_SUMP_HOST"), "localhost");
      port = getValueOrDefault(System.getenv("FLOOD_SUMP_PORT"), "35663");
      flood = getValueOrDefault(System.getenv("FLOOD_SEQUENCE_ID"), "1");
      account = getValueOrDefault(System.getenv("FLOOD_ACCOUNT_ID"), "1");
      project = getValueOrDefault(System.getenv("FLOOD_PROJECT_ID"), "1");
      region = getValueOrDefault(System.getenv("FLOOD_GRID_REGION"), "local");
      grid = getValueOrDefault(System.getenv("FLOOD_GRID_SQEUENCE_ID"), "1");
      node = getValueOrDefault(System.getenv("FLOOD_GRID_NODE_SEQUENCE_ID"), "1");
      openClient();
    } catch (IOException ex) {
      log.severe("problem opening client " + ex.getMessage());
    }
  }

  protected void openClient() throws IOException {
    socket = new DatagramSocket();
    address = InetAddress.getByName(host);
    log.info("connecting to flood sump " + name + " at " + host + ":" + port);
  }

  String getTransactionName() {
    return transactionName;
  }

  public void start_transaction(String transactionName) {
    this.transactionName = transactionName;
  }

  public void passed_transaction(WebDriver driver, String label, Integer responseCode, Double responseTime) {
    transaction(driver, label, "true", responseCode, responseTime, null);
  }

  public void passed_transaction(WebDriver driver, String label, Integer responseCode) {
    transaction(driver, label, "true", responseCode, null, null);
  }

  public void passed_transaction(WebDriver driver, String label) {
    transaction(driver, label, "true", 200, null, null);
  }

  public void passed_transaction(WebDriver driver) {
    transaction(driver, driver.getTitle(), "true", 200, null, null);
  }

  public void webdriver_exception(WebDriver driver, WebDriverException exception) {
    transaction(driver, transactionName, "false", 0, null, exception);
  }

  public void failed_transaction(WebDriver driver, String label, Integer responseCode, Double responseTime) {
    transaction(driver, label, "false", responseCode, responseTime, null);
  }

  public void failed_transaction(WebDriver driver, String label, Integer responseCode) {
    transaction(driver, label, "false", responseCode, null, null);
  }

  public void failed_transaction(WebDriver driver, Integer responseCode) {
    transaction(driver, driver.getTitle(), "false", responseCode, null, null);
  }

  public void failed_transaction(WebDriver driver) {
    transaction(driver, driver.getTitle(), "false", 500, null, null);
  }

  public void get_mark(WebDriver driver, String mark) {
    waitForUserTiming(driver, "mark", mark);

    JavascriptExecutor js = (JavascriptExecutor)driver;
    double responseTime = new Double(js.executeScript("return window.performance.getEntriesByName('" + mark +"')[0].startTime;").toString());

    transaction(driver, mark, "true", 200, responseTime, null);
  }

  public void get_measure(WebDriver driver, String measure) {
    waitForUserTiming(driver, "measure", measure);

    JavascriptExecutor js = (JavascriptExecutor)driver;
    double responseTime = new Double(js.executeScript("return window.performance.getEntriesByName('" + measure +"')[0].duration;").toString());

    transaction(driver, measure, "true", 200, responseTime, null);
  }

  protected void waitForUserTiming(final WebDriver driver, final String type, final String name) {
    // final JavascriptExecutor js = (JavascriptExecutor)driver;

    // new FluentWait<JavascriptExecutor>(js) {
    //   protected RuntimeException timeoutException(String message, Throwable lastException) {
    //     transaction(driver, name, "false", null, null, null);
    //     return null;
    //   }
    // }.
    // withMessage("UserTiming API timed out waiting for " + name).
    // withTimeout(60 ,TimeUnit.SECONDS).
    // pollingEvery(2 ,TimeUnit.SECONDS).
    // until(new Predicate<JavascriptExecutor>(){
    //   public Boolean apply(Object d){
    //       JavascriptExecutor js = (JavascriptExecutor) d;
    //       return (Boolean)js.executeScript(
    //         "return window.performance.getEntriesByType('" + type + "').length > 0;");
    //   }
    // });
  }

  public String numberOfBrowsers() {
    try {
      return String.valueOf(Files.list(Paths.get("/test/lock")).count());
    } catch (IOException ex) {
      return "1";
    }
  }

  public void transaction(WebDriver driver, String label, String successful, Integer responseCode, Double responseTime, WebDriverException exception) {
    try {
      JavascriptExecutor js = (JavascriptExecutor)driver;

      long loadEventEnd = (Long)js.executeScript("return window.performance.timing.loadEventEnd;");
      long navigationStart = (Long)js.executeScript("return window.performance.timing.navigationStart;");
      long responseStart = (Long)js.executeScript("return window.performance.timing.responseStart;");
      long connectStart = (Long)js.executeScript("return window.performance.timing.connectStart;");

      JSONObject json = new JSONObject();
      String enc_label = String.valueOf(URLEncoder.encode(label, "UTF-8"));
      String response_code = getValueOrDefault(String.valueOf(responseCode), "");
      int passed = 0;
      int failed = 0;

      if (successful.equals("false")) {
        ++failed;
      } else {
        ++passed;
      }

      sendPacket("response_time", (responseTime == null) ? String.valueOf(loadEventEnd - navigationStart) : String.format("%.0f", responseTime), enc_label, response_code);
      sendPacket("latency", String.valueOf(responseStart - navigationStart), enc_label, response_code);
      sendPacket("passed", String.valueOf(passed), enc_label, response_code);
      sendPacket("failed", String.valueOf(failed), enc_label, response_code);
      sendPacket("transaction_rate", String.valueOf(passed + failed), enc_label, response_code);
      sendPacket("concurrency", getValueOrDefault(String.valueOf(numberOfBrowsers()), "1"), enc_label, response_code);

      ConcurrentMap trace = new ConcurrentHashMap();
      trace.put("label", enc_label);
      trace.put("url", driver.getCurrentUrl());
      trace.put("response_code", response_code);
      trace.put("source_host", host);
      trace.put("start_time", String.valueOf(navigationStart));
      trace.put("end_time",String.valueOf(loadEventEnd));
      trace.put("sample_count", "1");
      trace.put("error_count", (successful.equals("false")) ? "1" : "0");
      trace.put("active_threads_in_group", getValueOrDefault(String.valueOf(numberOfBrowsers()), "1"));
      trace.put("request_headers", "");
      trace.put("response_headers", "");
      if (successful.equals("false")) {
        if(exception != null) {
          String[] lines = exception.getMessage().split("\\r?\\n");
          trace.put("assertion_1", "true|WebDriverException|" + lines[0]);
          trace.put("response_data", exception.getMessage());

          File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
          trace.put("screenshot", encodeFileToBase64Binary(screenshot));
        } else {
          String response_data = driver.getPageSource();
          trace.put("response_data", response_data.substring(0, Math.min(response_data.length(), 4096)));
        }
      }
      json.putAll( trace );
      sendPacket("trace", compressString(json.toString()), enc_label, response_code);

    } catch (IOException ex) {
      log.severe("problem with sample " + ex.getMessage());
    }
  }
  private static String encodeFileToBase64Binary(File file){
    String encodedfile = null;
    try {
      FileInputStream fileInputStreamReader = new FileInputStream(file);
      byte[] bytes = new byte[(int)file.length()];
      fileInputStreamReader.read(bytes);
      encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return encodedfile;
  }

  public void finished() {
  }

  public void flushSamples() {
  }

  private static boolean isNotNullOrEmpty(String str){
    return (str != null && !str.isEmpty());
  }

  private String getValueOrDefault(String value, String defaultValue) {
    return isNotNullOrEmpty(value) ? value : defaultValue;
  }

  private Integer safeLongToInt(long l) {
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
      throw new IllegalArgumentException
        (l + " cannot be cast to int without changing its value.");
    }
    return (int) l;
  };

  public static String compressString(String srcTxt) throws IOException {
    ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
    GZIPOutputStream zos = new GZIPOutputStream(rstBao);
    zos.write(srcTxt.getBytes());
    IOUtils.closeQuietly(zos);

    byte[] bytes = rstBao.toByteArray();
    return "\"" + Base64.encodeBase64String(bytes) + "\"";
  }

  private void sendPacket(final String measurement, final String value, final String label, final String response_code){
    try {
      StringBuilder builder = new StringBuilder();
        builder.append(measurement);
        builder.append(",account=");
        builder.append(account);
        builder.append(",flood=");
        builder.append(flood);
        builder.append(",region=");
        builder.append(region);
        builder.append(",grid=");
        builder.append(grid);
        builder.append(",node=");
        builder.append(node);
        builder.append(",project=");
        builder.append(project);
        builder.append(",label=");
        builder.append(label);
        builder.append(" value=");
        builder.append(value);
        builder.append(",response_code=");
        builder.append("\"" + response_code + "\"");

      byte[] buffer = builder.toString().getBytes();

      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(port));
      socket.send(packet);
    } catch (IOException ex) {
      log.severe("problem sending packet " + ex.getMessage());
    }
  };
}
