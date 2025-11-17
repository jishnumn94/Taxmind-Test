package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ApplicationPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ApplicationPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void openApplicationsFromProfile() {
        // First, try direct navigation to /application endpoint
        try {
            String currentUrl = driver.getCurrentUrl();
            String baseUrl = currentUrl.replaceAll("(/profile.*|/$)", "");
            driver.navigate().to(baseUrl + "/application");
            // Wait for application items to load
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'application-item')]")));
            Thread.sleep(1000); // Additional wait for rendering
            System.out.println("Successfully navigated to applications page via direct URL");
            return;
        } catch (Exception ex) {
            System.out.println("Direct navigation failed: " + ex.getMessage());
        }

        // Fallback: Try to click the user menu / username at top-right
        By[] userSelectors = new By[] {
            By.xpath("//button[contains(., 'Jishnu')]"),
            By.xpath("//button[contains(@class, 'v-btn') and contains(., 'MN')]"),
            By.cssSelector("button[aria-haspopup='true'][aria-expanded='false']"),
            By.cssSelector("header button:last-of-type"),
        };
        for (By sel : userSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (el != null && el.isDisplayed()) {
                    el.click();
                    Thread.sleep(500);
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        // Then click Applications or Application link in dropdown
        By[] appsSelectors = new By[] {
            By.xpath("//a[contains(., 'Application')]"),
            By.xpath("//button[contains(., 'Application')]"),
            By.xpath("//div[contains(@class, 'v-menu__content')]//a[contains(., 'Application')]")
        };
        for (By sel : appsSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (el != null && el.isDisplayed()) {
                    el.click();
                    // Wait for application items to load after clicking
                    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'application-item')]")));
                    Thread.sleep(1000);
                    System.out.println("Successfully navigated to applications page via menu click");
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        throw new RuntimeException("Unable to navigate to Applications page");
    }

    public WebElement findApplicationForYear(String year) {
        // Look for the application container that contains the year text
        // The structure is: application-item > timeline-container > ... > year-info (contains "Year: 2026")
        try {
            // Find all application items
            List<WebElement> appItems = driver.findElements(By.xpath("//div[contains(@class, 'application-item')]"));
            System.out.println("Found " + appItems.size() + " application items");
            
            for (WebElement item : appItems) {
                try {
                    // Look for the year-info div within this application item
                    WebElement yearInfo = item.findElement(By.xpath(".//div[contains(@class, 'year-info')]"));
                    String yearText = yearInfo.getText();
                    System.out.println("Found year text: " + yearText);
                    
                    // Check if this year-info contains the target year
                    if (yearText.contains("Year: " + year) || yearText.contains(year)) {
                        System.out.println("Matched application for year: " + year);
                        return item;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        // Fallback: try generic search for the year on the page
        try {
            List<WebElement> matches = driver.findElements(By.xpath("//*[contains(text(), 'Year: " + year + "')]"));
            for (WebElement m : matches) {
                try {
                    // Look for the closest ancestor application-item
                    WebElement appItem = m.findElement(By.xpath("ancestor::div[contains(@class, 'application-item')][1]"));
                    return appItem;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        // Save page source for debugging
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
            String ts = java.time.LocalDateTime.now().format(fmt);
            java.nio.file.Path out = java.nio.file.Paths.get("target", "screenshots", "applications-" + ts + ".html");
            java.nio.file.Files.createDirectories(out.getParent());
            java.nio.file.Files.write(out, driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        throw new RuntimeException("No application found for year " + year);
    }

    public void continueApplication(WebElement appElement) {
        // Look for the "Continue Application" footer/link within the appElement
        By[] continueSelectors = new By[] {
            By.xpath(".//div[contains(@class, 'timeline-footer') and contains(., 'Continue Application')]"),
            By.xpath(".//span[contains(., 'Continue Application')]"),
            By.xpath(".//*[contains(., 'Continue Application')]")
        };
        
        for (By sel : continueSelectors) {
            try {
                List<WebElement> els = appElement.findElements(sel);
                for (WebElement e : els) {
                    try {
                        if (e.isDisplayed()) {
                            System.out.println("Found Continue Application element: " + e.getText());
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
                            Thread.sleep(500);
                            // Use JavaScript click to bypass overlay elements
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                            Thread.sleep(2000);
                            return;
                        }
                    } catch (Exception ex) {
                        System.out.println("Error clicking Continue Application: " + ex.getMessage());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback: search in parent containers
        try {
            WebElement parent = appElement;
            for (int i = 0; i < 5; i++) {
                try {
                    parent = (WebElement) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].parentElement;", parent);
                    if (parent == null) break;
                    
                    for (By sel : continueSelectors) {
                        try {
                            List<WebElement> els = parent.findElements(sel);
                            for (WebElement e : els) {
                                try {
                                    if (e.isDisplayed()) {
                                        System.out.println("Found Continue Application in parent: " + e.getText());
                                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
                                        Thread.sleep(500);
                                        e.click();
                                        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                                        Thread.sleep(1000);
                                        return;
                                    }
                                } catch (Exception ex) {}
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        // Debug: save page source
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
            String ts = java.time.LocalDateTime.now().format(fmt);
            java.nio.file.Path out = java.nio.file.Paths.get("target", "screenshots", "app-card-debug-" + ts + ".html");
            java.nio.file.Files.createDirectories(out.getParent());
            // Save the appElement HTML
            String html = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].outerHTML;", appElement);
            java.nio.file.Files.write(out, html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Saved appElement HTML to: " + out);
        } catch (Exception ex) {
            System.out.println("Error saving debug artifacts: " + ex.getMessage());
        }

        throw new RuntimeException("Unable to find 'Continue Application' button on application card for year 2026");
    }
}
