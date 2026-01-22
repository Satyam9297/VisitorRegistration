package vregTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Vreg {

    private WebDriver driver;
    private WebDriverWait wait;

    private final By loginBtn = By.xpath("//span[normalize-space()='Log in/Sign up']");
    private final By mobileInput = By.xpath("//input[@placeholder='Enter Your Mobile']");
    private final By sendCodeBtn = By.xpath("//button[@type='submit']");
    private final By verifyOtpBtn = By.xpath("//button[@class='btn set-phone-no']");
    private final By buyTicketBtn = By.xpath("//div[@class='mobile-div']//button[@class='button']//i[@class='fa fa-arrow-right']");
    private final By plusIcon = By.xpath("(//i[@class='fa-solid fa-plus'])[1]");
    private final By nextBtn = By.xpath("//button[text()=' Next ']");
    private final By makePaymentBtn = By.xpath("//button[text()=' Make Payment ']");
    private final By razorpayFrame = By.cssSelector("iframe.razorpay-checkout-frame");
    private final By netbankingBy = By.xpath("//div[@data-value='netbanking']//span[@data-testid='Netbanking']");  
    private final By iciciBy = By.xpath("(//div[@data-value='ICIC'])[1]");
    private final By successBtn = By.xpath("//button[@class='success']");
    private final By cardNumber = By.xpath("//input[@placeholder='Card Number']");
    private final By date = By.xpath("//input[@placeholder='MM / YY']");
    private final By cvvNumber = By.xpath("//input[@placeholder='CVV']");
    private final By cardOption = By.xpath("//div[@data-value='card']");
    private final By submitButton = By.xpath("//button[@name='button']");
    private final By cardContinueBtnAndroid = By.xpath("(//button[@name='button'])[2]");


	@BeforeClass
    public void setUp() throws MalformedURLException {
        driver = new ChromeDriver();
//		driver = new RemoteWebDriver(
//				new URL("https://hub-cloud.browserstack.com/wd/hub"),
//		        new ChromeOptions()
//		);
		
		


        wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(60));
        driver.get("https://farmers-dev.kisan.in/");
       
    }

    @Test(priority = 1)
    public void LoginTest() throws InterruptedException {

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        
        System.out.println("User is on the enter mobile page");

        wait.until(ExpectedConditions.visibilityOfElementLocated(mobileInput)).sendKeys("9699746935");
        
        System.out.println("Mobile number entered");

        wait.until(ExpectedConditions.elementToBeClickable(sendCodeBtn)).click();
        
        System.out.println("Clicked submit button");

        // enter otp
        String otp = "123123";
        for (int i = 0; i < otp.length(); i++) {
            By otpBox = By.xpath("//input[@name='otp" + (i + 1) + "']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(otpBox))
                    .sendKeys(String.valueOf(otp.charAt(i)));
        }
        

        wait.until(ExpectedConditions.elementToBeClickable(verifyOtpBtn)).click();
        
        System.out.println("Submitted OTP and submittited");

        // buy ticket flow
        wait.until(ExpectedConditions.elementToBeClickable(buyTicketBtn)).click();
        System.out.println("Clicked Buy ticket button on the Home screen");
        
        wait.until(ExpectedConditions.elementToBeClickable(plusIcon)).click();
        System.out.println("Added one ticket");
        
        wait.until(ExpectedConditions.elementToBeClickable(nextBtn)).click();
        System.out.println("Clicked next button after selecting quantity");
        
        wait.until(ExpectedConditions.elementToBeClickable(makePaymentBtn)).click();
        System.out.println("Clicked on the make payment button");

        Thread.sleep(30000);

     // 1️⃣ Switch to main Razorpay iframe
     wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(razorpayFrame));
     System.out.println("Switched to Razorpay main iframe");

     // 2️⃣ Click "Card" option
     wait.until(ExpectedConditions.elementToBeClickable(cardOption));
     clickWithScrollAndStaleRetry(cardOption);
     System.out.println("Clicked Card option");

     // ❗ IMPORTANT: No second iframe on mobile Razorpay
     // Card fields appear in SAME iframe

     // 3️⃣ Enter card details
     wait.until(ExpectedConditions.visibilityOfElementLocated(cardNumber)).sendKeys("2305 3242 5784 8228");
     wait.until(ExpectedConditions.visibilityOfElementLocated(date)).sendKeys("12/28");
     wait.until(ExpectedConditions.visibilityOfElementLocated(cvvNumber)).sendKeys("111");


  // verify and pay button
     if (driver.findElements(submitButton).size() > 0) {
         wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
         System.out.println("Clicked Android submit");
     } else {
         wait.until(ExpectedConditions.elementToBeClickable(cardContinueBtnAndroid)).click();
         System.out.println("Clicked Web submit");
     }


       
        // Back out of iframe before window switching
      //  driver.switchTo().defaultContent();

        // window switching (Razorpay success button)
        String parentWindow = driver.getWindowHandle();

        // wait until popup window opens (or at least handles > 1)
        wait.until(d -> d.getWindowHandles().size() > 1);

        Set<String> handles = driver.getWindowHandles();
        for (String windowHandle : handles) {
            if (!windowHandle.equals(parentWindow)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.elementToBeClickable(successBtn)).click();
                
                System.out.println("Clicked Success button");
            }
        }

        driver.switchTo().window(parentWindow);

        // assertion - verify user is on assign ticket page
        wait.until(ExpectedConditions.urlContains("assign-ticket"));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("assign-ticket"),
                "FAIL: User not redirected to assign ticket page. URL was: " + currentUrl);

        System.out.println("PASS: User redirected to assign ticket page");
    }

    private void clickWithScrollAndStaleRetry(By locator) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
