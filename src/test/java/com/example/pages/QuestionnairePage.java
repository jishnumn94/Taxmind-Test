package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class QuestionnairePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public QuestionnairePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void fillMandatoryQuestions() {
        // Find inputs marked required, or labels with '*' then their inputs
        List<WebElement> requiredInputs = driver.findElements(By.cssSelector("input[required], textarea[required], select[required], input[aria-required='true']"));
        for (WebElement el : requiredInputs) {
            try {
                String tag = el.getTagName().toLowerCase();
                String type = el.getAttribute("type") == null ? "" : el.getAttribute("type").toLowerCase();
                if (tag.equals("input") && (type.equals("text") || type.equals("tel") || type.equals("email") || type.equals("number") || type.equals(""))) {
                    wait.until(ExpectedConditions.elementToBeClickable(el)).clear();
                    el.sendKeys("Test answer");
                } else if (tag.equals("textarea")) {
                    wait.until(ExpectedConditions.elementToBeClickable(el)).clear();
                    el.sendKeys("Test answer");
                } else if (tag.equals("select")) {
                    Select s = new Select(el);
                    s.selectByIndex(0);
                } else if (tag.equals("input") && type.equals("checkbox")) {
                    if (!el.isSelected()) el.click();
                } else if (tag.equals("input") && type.equals("radio")) {
                    // pick the first radio in the group (same name)
                    String name = el.getAttribute("name");
                    if (name != null && !name.isEmpty()) {
                        List<WebElement> group = driver.findElements(By.cssSelector("input[type='radio'][name='" + name + "']"));
                        if (!group.isEmpty()) {
                            WebElement pick = group.get(0);
                            if (!pick.isSelected()) pick.click();
                        }
                    } else {
                        if (!el.isSelected()) el.click();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Additionally, look for labels with '*' and try to fill associated input
        List<WebElement> labels = driver.findElements(By.xpath("//label[contains(., '*')]"));
        for (WebElement lab : labels) {
            try {
                String forAttr = lab.getAttribute("for");
                if (forAttr != null && !forAttr.isEmpty()) {
                    WebElement target = driver.findElement(By.id(forAttr));
                    if (target != null && target.isDisplayed()) {
                        String tag = target.getTagName().toLowerCase();
                        if (tag.equals("input") || tag.equals("textarea")) {
                            target.clear();
                            target.sendKeys("Test answer");
                        } else if (tag.equals("select")) {
                            new Select(target).selectByIndex(0);
                        }
                    }
                } else {
                    // try sibling input
                    WebElement sibling = lab.findElement(By.xpath(".//following::input[1] | .//following::select[1] | .//following::textarea[1]"));
                    if (sibling != null) {
                        String tag = sibling.getTagName().toLowerCase();
                        if (tag.equals("input") || tag.equals("textarea")) {
                            sibling.clear();
                            sibling.sendKeys("Test answer");
                        } else if (tag.equals("select")) {
                            new Select(sibling).selectByIndex(0);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void submitQuestionnaire() {
        // Wait for the questionnaire modal/dialog to be visible and the form to be rendered
        try {
            // Wait for dialog container to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'v-dialog__container')]//div[contains(@class, 'v-card')]")));
            Thread.sleep(1000); // Give time for form content to render
            System.out.println("Questionnaire modal detected");
        } catch (Exception e) {
            System.out.println("Could not find questionnaire modal dialog: " + e.getMessage());
        }

        // Wait for any form inputs to be visible (indicates form is ready)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[not(contains(@type, 'hidden'))] | //textarea | //select")));
            System.out.println("Form inputs detected");
        } catch (Exception e) {
            System.out.println("Could not find form inputs: " + e.getMessage());
        }
        
        // click submit button on questionnaire (broadened selectors, search within dialog first)
        By[] submitSelectors = new By[] {
            // Most specific: look in dialog container
            By.xpath("//div[contains(@class, 'v-dialog__container')]//div[contains(@class, 'v-card')]//button[contains(., 'Submit') or contains(., 'Finish') or contains(., 'Continue')]"),
            // Look for buttons in dialog with action-btn class
            By.xpath("//div[contains(@class, 'v-dialog__container')]//button[contains(@class, 'action') and not(contains(@class, 'questionnaire-btn')) and not(contains(@class, 'comments-btn'))]"),
            // Generic button with action class (but exclude questionnaire/comments buttons)
            By.xpath("//button[contains(@class, 'action') and not(contains(@class, 'questionnaire-btn')) and not(contains(@class, 'comments-btn'))]"),
            // Button in card that's not part of applications list
            By.xpath("//div[contains(@class, 'v-card')]//button[contains(., 'Submit') or contains(., 'Finish') or contains(., 'Continue')][not(ancestor::div[contains(@class, 'application-item')])]"),
            // Look for buttons with primary class in dialog
            By.xpath("//div[contains(@class, 'v-dialog__container')]//button[contains(@class, 'primary')]"),
            // Fallback: any button containing action text
            By.xpath("//button[contains(text(), 'Submit') or contains(text(), 'Finish') or contains(text(), 'Continue')][not(ancestor::div[contains(@class, 'application-item')])]"),
            By.xpath("//button[contains(., 'Submit') or contains(., 'Finish') or contains(., 'Complete') or contains(., 'Save') or contains(., 'Save & Continue') or contains(., 'Continue') or contains(., 'Finalise') or contains(., 'Finalize')]"),
            By.xpath("//input[@type='submit']"),
            By.cssSelector("button.submit, button.btn-submit, .btn-submit"),
            By.cssSelector("button[data-qa='submit'], button[data-test='submit']"),
            By.xpath("//div[@role='dialog']//button[contains(., 'Submit') or contains(., 'Finish') or contains(., 'Complete') or contains(., 'Save') or contains(., 'Continue')]"),
            By.xpath("//form//button[not(@type='button') or @type='submit']")
        };
        boolean clicked = false;
        for (By sel : submitSelectors) {
            try {
                List<WebElement> candidates = driver.findElements(sel);
                for (WebElement btn : candidates) {
                    try {
                        if (btn.isDisplayed()) {
                            String btnText = btn.getText();
                            System.out.println("Checking button: " + btnText + " with selector: " + sel);
                            // Skip if it's a header button like "New Claim"
                            if (btnText.contains("New Claim") || btnText.contains("Questionnaire") || btnText.contains("Comments") || btnText.contains("Amendment")) {
                                System.out.println("Skipping non-form button: " + btnText);
                                continue;
                            }
                            System.out.println("Clicking button: " + btnText);
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
                            Thread.sleep(200);
                            btn.click();
                            clicked = true;
                            break;
                        }
                    } catch (Exception ex) {
                        System.out.println("Error with button: " + ex.getMessage());
                    }
                }
                if (clicked) break;
            } catch (Exception ignored) {
            }
        }
        if (!clicked) {
            // save page source and screenshot for debugging
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
                String ts = java.time.LocalDateTime.now().format(fmt);
                java.nio.file.Path out = java.nio.file.Paths.get("target", "screenshots", "questionnaire-" + ts + ".html");
                java.nio.file.Files.createDirectories(out.getParent());
                java.nio.file.Files.write(out, driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                try {
                    org.openqa.selenium.TakesScreenshot tsnap = (org.openqa.selenium.TakesScreenshot) driver;
                    byte[] bytes = tsnap.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                    java.nio.file.Path png = java.nio.file.Paths.get("target", "screenshots", "questionnaire-" + ts + ".png");
                    java.nio.file.Files.write(png, bytes);
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
            throw new RuntimeException("Submit button on questionnaire not found");
        }

        // handle confirmation popup - click submit/confirm
        try {
            By[] confirmSelectors = new By[] {
                By.xpath("//button[contains(., 'Submit') or contains(., 'Confirm') or contains(., 'Yes') or contains(., 'OK')]")
            };
            for (By sel : confirmSelectors) {
                try {
                    WebElement conf = wait.until(ExpectedConditions.elementToBeClickable(sel));
                    if (conf != null && conf.isDisplayed()) {
                        conf.click();
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}
