package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MenuPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /**
     * Click on the user menu (usually top-right corner)
     */
    public void openUserMenu() {
        try {
            // Try different selectors for the user menu button
            By[] userMenuSelectors = new By[] {
                By.xpath("//button[contains(@class, 'v-btn') and contains(., 'MN')]"),
                By.xpath("//button[contains(., 'Jishnu')]"),
                By.cssSelector("button[aria-haspopup='menu']"),
                By.cssSelector("header button:last-of-type"),
                By.xpath("//button[@aria-expanded='false'][1]"),
            };

            for (By selector : userMenuSelectors) {
                try {
                    WebElement userMenuBtn = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (userMenuBtn != null && userMenuBtn.isDisplayed()) {
                        userMenuBtn.click();
                        Thread.sleep(500);
                        System.out.println("Successfully opened user menu");
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to open user menu: " + e.getMessage());
        }
    }

    /**
     * Get all menu items from the dropdown menu
     */
    public List<String> getMenuItems() {
        List<String> menuItems = new ArrayList<>();
        try {
            // Wait for menu to be visible
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'v-menu__content')]//a | //div[contains(@class, 'v-menu__content')]//button")));
            
            // Get all menu items
            List<WebElement> items = driver.findElements(By.xpath("//div[contains(@class, 'v-menu__content')]//a | //div[contains(@class, 'v-menu__content')]//button"));
            
            for (WebElement item : items) {
                String text = item.getText().trim();
                if (!text.isEmpty()) {
                    menuItems.add(text);
                    System.out.println("Found menu item: " + text);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get menu items: " + e.getMessage());
        }
        return menuItems;
    }

    /**
     * Click on a specific menu item by name
     */
    public void clickMenuItem(String menuItemName) {
        try {
            // Try to find and click the menu item
            By[] selectors = new By[] {
                By.xpath("//div[contains(@class, 'v-menu__content')]//a[contains(., '" + menuItemName + "')]"),
                By.xpath("//div[contains(@class, 'v-menu__content')]//button[contains(., '" + menuItemName + "')]"),
                By.xpath("//div[contains(@class, 'v-menu__content')]//*[contains(text(), '" + menuItemName + "')]"),
            };

            for (By selector : selectors) {
                try {
                    WebElement menuItem = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (menuItem != null && menuItem.isDisplayed()) {
                        // Scroll into view if needed
                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", menuItem);
                        Thread.sleep(300);
                        menuItem.click();
                        Thread.sleep(1000); // Wait for page load after clicking
                        System.out.println("Successfully clicked menu item: " + menuItemName);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to click menu item '" + menuItemName + "': " + e.getMessage());
        }
    }

    /**
     * Get current page URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Get current page title
     */
    public String getCurrentPageTitle() {
        try {
            // Try to get page title from various sources
            String title = driver.getTitle();
            if (title != null && !title.isEmpty()) {
                return title;
            }

            // Try to get from h1 or main heading
            By[] headingSelectors = new By[] {
                By.tagName("h1"),
                By.tagName("h2"),
                By.xpath("//div[contains(@class, 'title')]"),
                By.xpath("//span[contains(@class, 'title')]"),
            };

            for (By selector : headingSelectors) {
                try {
                    List<WebElement> headings = driver.findElements(selector);
                    if (!headings.isEmpty() && headings.get(0).isDisplayed()) {
                        String text = headings.get(0).getText().trim();
                        if (!text.isEmpty()) {
                            return text;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get page title: " + e.getMessage());
        }
        return "Unknown Page";
    }

    /**
     * Navigate through all menu items and return a map of menu item -> page URL
     */
    public Map<String, String> navigateAllMenuItems() {
        Map<String, String> menuNavigationMap = new HashMap<>();
        
        try {
            // Get unique menu items (remove duplicates)
            List<String> allItems = getMenuItems();
            List<String> menuItems = new ArrayList<>();
            for (String item : allItems) {
                if (!menuItems.contains(item)) {
                    menuItems.add(item);
                }
            }
            
            if (menuItems.isEmpty()) {
                System.out.println("No menu items found");
                return menuNavigationMap;
            }

            // For each menu item, click it and capture the URL and page title
            for (String menuItem : menuItems) {
                try {
                    System.out.println("\n--- Processing menu item: " + menuItem + " ---");
                    
                    // Close the current menu and reopen for fresh state
                    Thread.sleep(500);
                    driver.navigate().refresh();
                    Thread.sleep(1000);
                    
                    // Reopen the menu
                    openUserMenu();
                    Thread.sleep(500);
                    
                    // Click the menu item
                    clickMenuItem(menuItem);
                    
                    // Wait for page to load
                    Thread.sleep(2000);
                    
                    // Get page details
                    String pageUrl = getCurrentUrl();
                    String pageTitle = getCurrentPageTitle();
                    
                    menuNavigationMap.put(menuItem, pageUrl);
                    System.out.println("✓ Menu item: '" + menuItem + "' → URL: " + pageUrl + " | Title: " + pageTitle);
                    
                } catch (Exception e) {
                    System.out.println("✗ Failed to process menu item '" + menuItem + "': " + e.getMessage());
                    menuNavigationMap.put(menuItem, "ERROR: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to navigate all menu items: " + e.getMessage());
        }

        return menuNavigationMap;
    }

    /**
     * Close the menu by clicking elsewhere or pressing Escape
     */
    public void closeMenu() {
        try {
            // Try pressing Escape key
            driver.navigate().refresh();
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println("Failed to close menu: " + e.getMessage());
        }
    }

    /**
     * Get page content indicators (headings, main content)
     */
    public String getPageContent() {
        StringBuilder content = new StringBuilder();
        try {
            // Get page title
            content.append("Title: ").append(driver.getTitle()).append("\n");

            // Get current URL
            content.append("URL: ").append(driver.getCurrentUrl()).append("\n");

            // Get main headings
            By[] headingSelectors = new By[] {
                By.tagName("h1"),
                By.tagName("h2"),
                By.xpath("//div[contains(@class, 'title') or contains(@class, 'header')]"),
            };

            for (By selector : headingSelectors) {
                try {
                    List<WebElement> headings = driver.findElements(selector);
                    for (WebElement heading : headings) {
                        String text = heading.getText().trim();
                        if (!text.isEmpty() && !content.toString().contains(text)) {
                            content.append("Heading: ").append(text).append("\n");
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Check for specific page indicators
            if (driver.getCurrentUrl().contains("/profile")) {
                content.append("Page Type: PROFILE\n");
                try {
                    List<WebElement> profileElements = driver.findElements(By.xpath("//*[contains(text(), 'Profile') or contains(text(), 'My Account')]"));
                    if (!profileElements.isEmpty()) {
                        content.append("Profile elements found: ").append(profileElements.size()).append("\n");
                    }
                } catch (Exception ignored) {
                }
            } else if (driver.getCurrentUrl().contains("/application")) {
                content.append("Page Type: APPLICATION\n");
                try {
                    List<WebElement> appElements = driver.findElements(By.xpath("//div[contains(@class, 'application')]"));
                    if (!appElements.isEmpty()) {
                        content.append("Application elements found: ").append(appElements.size()).append("\n");
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            content.append("Error getting page content: ").append(e.getMessage()).append("\n");
        }
        return content.toString();
    }
}
