package com.example;

import com.example.pages.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MenuNavigationTest extends BaseTest {
    private static final String EMAIL = "jishnu+1@ileafsolutions.com";
    private static final String PASSWORD = "Test@123";
    private static final String SITE_URL = "https://dev.taxmind.ie/";

    @Test
    public void testAllMenuNavigation() {
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

            // Now test menu navigation
            System.out.println("\n=== Starting Menu Navigation Test ===");
            menuPage.openUserMenu();
            
            // Get all menu items
            var menuItems = menuPage.getMenuItems();
            System.out.println("\nTotal menu items found: " + menuItems.size());
            for (String item : menuItems) {
                System.out.println("  - " + item);
            }

            // Navigate through all menu items
            System.out.println("\n=== Navigating Through All Menu Items ===");
            Map<String, String> navigationResults = menuPage.navigateAllMenuItems();
            
            // Get unique pages and their content
            System.out.println("\n=== Page Content Verification ===");
            Map<String, String> pageContents = new HashMap<>();
            for (String menuItem : navigationResults.keySet()) {
                String url = navigationResults.get(menuItem);
                if (!url.startsWith("ERROR") && !pageContents.containsKey(url)) {
                    String content = menuPage.getPageContent();
                    pageContents.put(url, content);
                }
            }

            // Print summary
            System.out.println("\n=== Detailed Navigation Summary ===");
            int successCount = 0;
            for (Map.Entry<String, String> entry : navigationResults.entrySet()) {
                System.out.println("\nMenu Item: '" + entry.getKey() + "'");
                System.out.println("  Result URL: " + entry.getValue());
                if (!entry.getValue().startsWith("ERROR")) {
                    successCount++;
                    if (pageContents.containsKey(entry.getValue())) {
                        System.out.println("  Page Content:");
                        System.out.println(pageContents.get(entry.getValue()));
                    }
                }
            }

            System.out.println("\n=== Test Summary ===");
            System.out.println("Total menu items tested: " + navigationResults.size());
            System.out.println("Successful navigations: " + successCount);
            System.out.println("Unique pages found: " + pageContents.size());

            // Assert that we successfully navigated to at least some pages
            assertTrue("Should have successfully navigated to at least one menu item", successCount > 0);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Menu navigation test failed: " + e.getMessage());
        }
    }
}
