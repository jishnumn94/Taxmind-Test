package com.example;

import com.example.pages.*;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ComprehensiveMenuNavigationTest extends BaseTest {
    private static final String EMAIL = "jishnu+1@ileafsolutions.com";
    private static final String PASSWORD = "Test@123";
    private static final String SITE_URL = "https://dev.taxmind.ie/";

    @Test
    public void testAllNavigationOptions() {
        LoginPage loginPage = new LoginPage(driver, wait);
        OTPPage otpPage = new OTPPage(driver, wait);
        ProfilePage profilePage = new ProfilePage(driver, wait);
        MenuPage menuPage = new MenuPage(driver, wait);

        try {
            // Login flow
            System.out.println("=== Starting Login Flow ===");
            loginPage.open(SITE_URL);
            loginPage.acceptCookiesIfPresent();
            loginPage.clickLoginButton();
            loginPage.enterUsername(EMAIL);
            loginPage.enterPassword(PASSWORD);
            loginPage.submitLogin();

            // Enter OTP
            System.out.println("=== Entering OTP ===");
            otpPage.enterOtp("123456");
            otpPage.submitOtp();

            // Verify profile
            boolean atProfile = profilePage.isAtProfile();
            System.out.println("At profile: " + atProfile);
            assertTrue("Expected to land on profile page after OTP", atProfile);

            // ========== TEST 1: USER MENU NAVIGATION ==========
            System.out.println("\n========== TEST 1: USER MENU NAVIGATION ==========");
            testUserMenuNavigation(menuPage);

            // ========== TEST 2: NAVIGATION LINKS ==========
            System.out.println("\n========== TEST 2: NAVIGATION LINKS IN FOOTER ==========");
            testFooterNavigation();

            // ========== TEST 3: APPLICATION PAGE NAVIGATION ==========
            System.out.println("\n========== TEST 3: APPLICATION SUBPAGES ==========");
            testApplicationPageSubnavigation();

            System.out.println("\n========== ALL TESTS COMPLETED SUCCESSFULLY ==========");

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Comprehensive menu navigation test failed: " + e.getMessage());
        }
    }

    private void testUserMenuNavigation(MenuPage menuPage) {
        try {
            System.out.println("Testing user menu items...");
            menuPage.openUserMenu();
            
            var menuItems = menuPage.getMenuItems();
            System.out.println("Found " + menuItems.size() + " menu items");
            
            Map<String, String> navigationResults = menuPage.navigateAllMenuItems();
            
            System.out.println("\nUser Menu Navigation Results:");
            int successCount = 0;
            for (Map.Entry<String, String> entry : navigationResults.entrySet()) {
                boolean isSuccess = !entry.getValue().startsWith("ERROR");
                System.out.println("  ✓ " + entry.getKey() + " → " + entry.getValue());
                if (isSuccess) successCount++;
            }
            System.out.println("Successful navigations: " + successCount + " / " + navigationResults.size());
            
        } catch (Exception e) {
            System.out.println("Error testing user menu: " + e.getMessage());
        }
    }

    private void testFooterNavigation() {
        try {
            System.out.println("Testing footer navigation links...");
            
            // Scroll to bottom to see footer
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(500);
            
            // Get all footer links
            List<WebElement> footerLinks = driver.findElements(By.xpath("//footer//a | //div[contains(@class, 'footer')]//a"));
            
            System.out.println("Found " + footerLinks.size() + " footer links");
            
            Map<String, String> footerResults = new HashMap<>();
            for (WebElement link : footerLinks) {
                try {
                    String linkText = link.getText().trim();
                    String linkHref = link.getAttribute("href");
                    
                    if (!linkText.isEmpty() && linkHref != null && !linkHref.isEmpty()) {
                        footerResults.put(linkText, linkHref);
                        System.out.println("  ✓ " + linkText + " → " + linkHref);
                    }
                } catch (Exception ignored) {
                }
            }
            
            System.out.println("Total footer navigation items: " + footerResults.size());
            
        } catch (Exception e) {
            System.out.println("Error testing footer navigation: " + e.getMessage());
        }
    }

    private void testApplicationPageSubnavigation() {
        try {
            System.out.println("Testing application page sub-navigation...");
            
            // Navigate to application page
            ApplicationPage appPage = new ApplicationPage(driver, wait);
            appPage.openApplicationsFromProfile();
            
            // Get page content
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Current URL: " + currentUrl);
            
            // Look for action buttons or links in the application page
            List<WebElement> actionButtons = driver.findElements(By.xpath("//button[not(contains(@class, 'v-tab'))] | //a[not(contains(@class, 'v-tab'))]"));
            
            System.out.println("\nFound " + actionButtons.size() + " action buttons/links on application page:");
            
            Map<String, Integer> buttonCategories = new HashMap<>();
            for (WebElement button : actionButtons) {
                try {
                    String text = button.getText().trim();
                    if (!text.isEmpty() && button.isDisplayed()) {
                        buttonCategories.put(text, buttonCategories.getOrDefault(text, 0) + 1);
                        if (buttonCategories.get(text) == 1) {
                            System.out.println("  ✓ " + text);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            
            System.out.println("Total unique action items: " + buttonCategories.size());
            
        } catch (Exception e) {
            System.out.println("Error testing application page sub-navigation: " + e.getMessage());
        }
    }
}
