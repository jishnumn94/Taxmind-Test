package com.example;

import com.example.pages.LoginPage;
import com.example.pages.SignupPage;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

public class SignupTest extends BaseTest {
    private static final String SITE_URL = "https://dev.taxmind.ie/";

    @Test
    public void testSignupFlow() {
        SignupPage sp = new SignupPage(driver, wait);
        LoginPage lp = new LoginPage(driver, wait);

        try {
            sp.openLanding(SITE_URL);
            lp.acceptCookiesIfPresent();
            sp.clickApplyButton();

            // Use the email the user requested
            String email = "jishnu+888@ileafsolutions.com";
            sp.fillSignupForm(
                "Test",                      // firstName
                "User",                      // lastName
                email,                       // email
                "0712345678",               // phone
                "Test@123",                 // password
                "15/01/1990",               // dateOfBirth
                "Software Engineer",        // profession
                "1234567AB",                // ppsNumber (example format)
                "D01 ABCD",                 // eircode
                "123 Main Street\nDublin",  // address
                "Single"                    // maritalStatus
            );

            // OTPs (both email and phone) are '123456'
            // Enter once then submit; if there are two separate steps the second enter will be attempted too.
            sp.enterOtp("123456");
            sp.submitOtpIfPresent();
            // small wait for second OTP step if any
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            sp.enterOtp("123456");
            sp.submitOtpIfPresent();

            boolean blocked = sp.waitForESignBlockPopup(email, 12);
                System.out.println("ESign block popup found: " + blocked);
            
                // Diagnostic: Check if OTP inputs or buttons still exist on page
                try {
                    List<WebElement> otpInputs = driver.findElements(By.xpath("//input[contains(@placeholder, 'OTP') or contains(@placeholder, 'code') or contains(@name, 'otp')]"));
                    System.out.println("[diagnostic] OTP inputs still on page: " + otpInputs.size());
                
                    List<WebElement> otpButtons = driver.findElements(By.xpath("//button[contains(., 'Verify') or contains(., 'Submit') or contains(., 'Continue')]"));
                    System.out.println("[diagnostic] Verification buttons on page: " + otpButtons.size());
                
                    String bodyText = driver.findElement(By.tagName("body")).getText();
                    if (bodyText.contains("OTP") || bodyText.contains("verification")) {
                        System.out.println("[diagnostic] Page still shows OTP/verification section");
                    }
                } catch (Exception e) {
                    System.out.println("[diagnostic] Error checking page state: " + e.getMessage());
                }

            if (!blocked) {
                // Save screenshot and page source for debugging
                try {
                    long now = System.currentTimeMillis();
                    Path outDir = Paths.get("target/surefire-reports/signup-diagnostics");
                    Files.createDirectories(outDir);

                    if (driver instanceof TakesScreenshot) {
                        File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        Path dest = outDir.resolve("signup-failure-" + now + ".png");
                        Files.copy(scr.toPath(), dest);
                        System.out.println("Saved screenshot to: " + dest.toAbsolutePath());
                    }

                    try {
                        String page = driver.getPageSource();
                        Path html = outDir.resolve("signup-failure-" + now + ".html");
                        Files.writeString(html, page);
                        System.out.println("Saved page source to: " + html.toAbsolutePath());
                    } catch (IOException ioe) {
                        System.out.println("Failed saving page source: " + ioe.getMessage());
                    }
                } catch (Exception e1) {
                    System.out.println("Failed to capture diagnostics: " + e1.getMessage());
                }
            }

            assertTrue("Expected e-sign block popup mentioning " + email, blocked);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Signup flow failed: " + e.getMessage());
        }
    }
}
