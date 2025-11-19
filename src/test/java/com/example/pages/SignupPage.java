package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Clean SignupPage implementation (single class, no duplicates).
 */
public class SignupPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SignupPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void openLanding(String url) {
        driver.navigate().to(url);
        wait.until(d -> d.getCurrentUrl().toLowerCase().contains("taxmind") || d.getTitle().length() > 0);
    }

    public void clickApplyButton() {
    By[] selectors = new By[] {
        By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'apply') or contains(@href, 'apply') ]"),
        By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'apply')]") ,
        By.cssSelector("a[href*='apply']"),
        By.cssSelector("button.apply")
    };

        for (By sel : selectors) {
            try {
                WebElement e = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (e != null && e.isDisplayed()) {
                    e.click();
                    Thread.sleep(800);
                    return;
                }
            } catch (Exception ignored) {}
        }

        throw new RuntimeException("Apply button not found");
    }

    public void fillSignupForm(String firstName, String lastName, String email, String phone, String password, 
                              String dateOfBirth, String profession, String ppsNumber, String eircode, 
                              String address, String maritalStatus) {
        try {
            // Full Name
            try {
                By fullNameLabel = By.xpath("//span[normalize-space(.)='Full Name']/following::input[1]");
                WebElement fullNameEl = driver.findElement(fullNameLabel);
                fullNameEl.clear(); fullNameEl.sendKeys(firstName + " " + lastName);
                System.out.println("[signup-debug] fullName='" + fullNameEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Email Address
            try {
                By emailLabel = By.xpath("//span[normalize-space(.)='Email Address']/following::input[1]");
                WebElement emailEl = driver.findElement(emailLabel);
                emailEl.clear(); emailEl.sendKeys(email);
                System.out.println("[signup-debug] email='" + emailEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Phone Number (Vue phone component)
            try {
                By phoneInput = By.xpath("//input[@id and contains(@id, 'phone_number')]");
                WebElement phEl = driver.findElement(phoneInput);
                phEl.clear(); phEl.sendKeys(phone);
                System.out.println("[signup-debug] phone='" + phEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Date of Birth (calendar picker) - try to pick the exact date if provided (format: dd/MM/yyyy)
            try {
                if (dateOfBirth != null && dateOfBirth.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                    String[] parts = dateOfBirth.split("/");
                    String day = String.valueOf(Integer.parseInt(parts[0])); // remove leading zeros
                    String month = parts[1];
                    String year = parts[2];

                    By dobInput = By.xpath("//input[@id and contains(@id, 'input-') and (@readonly='true' or @readonly) and (preceding-sibling::div//i[contains(@class,'mdi-calendar')]|following::i[contains(@class,'mdi-calendar')])[1]]");
                    WebElement dobEl = driver.findElement(dobInput);
                    try {
                        dobEl.click();
                        Thread.sleep(500);

                        // Try to click a day in a Vuetify-style date picker
                        List<WebElement> dateButtons = driver.findElements(By.xpath("//div[contains(@class,'v-date-picker') or contains(@class,'v-calendar')]//button[normalize-space(.)='" + day + "']"));
                        if (!dateButtons.isEmpty()) {
                            dateButtons.get(0).click();
                            Thread.sleep(300);
                            System.out.println("[signup-debug] dateOfBirth clicked day=" + day + "");
                        } else {
                            // Fallback: try plain buttons with day text (less specific)
                            List<WebElement> plainDay = driver.findElements(By.xpath("//button[normalize-space(.)='" + day + "']"));
                            if (!plainDay.isEmpty()) {
                                plainDay.get(0).click();
                                Thread.sleep(300);
                                System.out.println("[signup-debug] dateOfBirth clicked plain day=" + day + "");
                            } else {
                                // Last resort: set the input value directly via JS and dispatch events
                                try {
                                    System.out.println("[signup-debug] dateOfBirth falling back to JS set: " + dateOfBirth);
                                    org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                                    js.executeScript("arguments[0].removeAttribute('readonly'); arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));", dobEl, dateOfBirth);
                                    Thread.sleep(300);
                                } catch (Exception jsEx) {
                                    System.out.println("[signup-debug] dateOfBirth JS fallback failed: " + jsEx.getMessage());
                                }
                            }
                        }
                    } catch (Exception clickEx) {
                        System.out.println("[signup-debug] dateOfBirth click flow failed: " + clickEx.getMessage());
                        // try JS set as a fallback
                        try {
                            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                            js.executeScript("arguments[0].removeAttribute('readonly'); arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));", dobEl, dateOfBirth);
                            Thread.sleep(300);
                        } catch (Exception ignored) {}
                    }
                }
                System.out.println("[signup-debug] dateOfBirth attempted: " + dateOfBirth);
            } catch (Exception ignored) {}

            // Profession
            try {
                By profInput = By.xpath("//span[normalize-space(.)='Profession']/following::input[1]");
                WebElement profEl = driver.findElement(profInput);
                profEl.clear(); profEl.sendKeys(profession);
                System.out.println("[signup-debug] profession='" + profEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // PPS Number
            try {
                By ppsInput = By.xpath("//span[normalize-space(.)='PPS Number']/following::input[1]");
                WebElement ppsEl = driver.findElement(ppsInput);
                ppsEl.clear(); ppsEl.sendKeys(ppsNumber);
                System.out.println("[signup-debug] ppsNumber='" + ppsEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Eircode
            try {
                By eircodeInput = By.xpath("//span[normalize-space(.)='Eircode']/following::input[1]");
                WebElement eircodeEl = driver.findElement(eircodeInput);
                eircodeEl.clear(); eircodeEl.sendKeys(eircode);
                System.out.println("[signup-debug] eircode='" + eircodeEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Address (textarea)
            try {
                By addressInput = By.xpath("//span[normalize-space(.)='Address']/following::textarea[1]");
                WebElement addrEl = driver.findElement(addressInput);
                addrEl.clear(); addrEl.sendKeys(address);
                System.out.println("[signup-debug] address='" + addrEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}

            // Password
            try {
                By passInput = By.xpath("//span[normalize-space(.)='Password']/following::input[@type='password'][1]");
                WebElement passEl = driver.findElement(passInput);
                passEl.clear(); passEl.sendKeys(password);
                System.out.println("[signup-debug] password='" + passEl.getAttribute("value") + "'");
            } catch (Exception ignored) {}


            // Marital Status (select dropdown) - choose the provided value (e.g., "Single")
            try {
                By maritalSelect = By.xpath("//span[normalize-space(.)='Marital Status']/following::div[contains(@class, 'v-select')][1]");
                WebElement maritalEl = driver.findElement(maritalSelect);
                maritalEl.click();
                Thread.sleep(400);
                // Try to select option matching the maritalStatus text
                try {
                    String optText = maritalStatus != null ? maritalStatus : "";
                    if (!optText.isEmpty()) {
                        List<WebElement> matching = driver.findElements(By.xpath("//div[contains(@class,'v-list-item__title') and normalize-space(.)='" + optText + "']"));
                        if (!matching.isEmpty()) {
                            matching.get(0).click();
                            Thread.sleep(300);
                            System.out.println("[signup-debug] maritalStatus selected='" + optText + "'");
                        } else {
                            // fallback: pick first available option
                            List<WebElement> options = driver.findElements(By.xpath("//div[contains(@class, 'v-select__menu')]//div[contains(@class, 'v-list-item')]"));
                            if (!options.isEmpty()) { options.get(0).click(); Thread.sleep(300); System.out.println("[signup-debug] maritalStatus fallback to first option"); }
                        }
                    }
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            // Terms & Conditions / Consent checkbox: robustly click the actual input/button next to the label
            try {
                // Find the label/span containing 'I agree' or 'Terms & Conditions'
                List<WebElement> consentLabels = driver.findElements(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'terms & conditions') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'terms and conditions') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'consent')][not(self::script or self::style)]"));
                for (WebElement label : consentLabels) {
                    // Look for a nearby input[type=checkbox] or clickable button/div
                    try {
                        // Try input[type=checkbox] within or next to label
                        WebElement checkbox = null;
                        try { checkbox = label.findElement(By.xpath(".//input[@type='checkbox']")); } catch (Exception ignored) {}
                        if (checkbox == null) {
                            // Try following-sibling or parent container
                            try { checkbox = label.findElement(By.xpath("following::input[@type='checkbox'][1]")); } catch (Exception ignored) {}
                        }
                        if (checkbox == null) {
                            // Try clickable div/button with role=checkbox or aria-checked
                            List<WebElement> divs = label.findElements(By.xpath(".//div[@role='checkbox' or @aria-checked] | following::div[@role='checkbox' or @aria-checked][1]"));
                            if (!divs.isEmpty()) checkbox = divs.get(0);
                        }
                        if (checkbox != null && checkbox.isDisplayed()) {
                            if (!checkbox.isSelected()) {
                                checkbox.click();
                                Thread.sleep(200);
                                System.out.println("[signup-debug] consent checkbox clicked");
                            }
                            break;
                        }
                        // Fallback: click the label itself if clickable
                        if (label.isDisplayed() && label.isEnabled()) {
                            label.click();
                            Thread.sleep(200);
                            System.out.println("[signup-debug] consent label clicked as fallback");
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // Scroll to submit button
            try { driver.findElement(By.xpath("//button[contains(., 'Submit')]")); } catch (Exception ignored) {}

            By[] submitSelectors = new By[] {
                    By.xpath("//button[contains(., 'Submit') or contains(., 'Continue') or contains(., 'Sign up') or contains(., 'Register') or contains(., 'Create Account')]"),
                    By.xpath("//input[@type='submit']"),
                    By.cssSelector("button[type='submit']")
            };

            for (By s : submitSelectors) {
                try {
                    WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(s));
                    if (btn != null && btn.isDisplayed()) { btn.click(); Thread.sleep(800); return; }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fill signup form: " + e.getMessage());
        }
    }

    public void enterOtp(String otp) {
        // Prefer individual digit inputs (common pattern)
        try {
            List<WebElement> digits = driver.findElements(By.xpath("//input[(contains(@class,'otp') or contains(@class,'digit') or contains(@name,'otp') or @maxlength='1')]"));
            if (digits != null && digits.size() >= otp.length()) {
                for (int i = 0; i < otp.length(); i++) {
                    try { digits.get(i).clear(); digits.get(i).sendKeys(String.valueOf(otp.charAt(i))); } catch (Exception ignored) {}
                }
                return;
            }
        } catch (Exception ignored) {}

        // Prefer single input with explicit OTP hints or maxlength
        try {
            By singleOtp = By.xpath("//input[(contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'otp') or contains(translate(@aria-label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'otp') or contains(translate(@name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'otp') or @maxlength='6' or @type='tel' and contains(@class,'otp'))]");
            WebElement single = wait.until(ExpectedConditions.presenceOfElementLocated(singleOtp));
            single.clear(); single.sendKeys(otp);
            return;
        } catch (Exception ignored) {}

        // Last resort: try to locate an input inside a container that mentions OTP
        try {
            WebElement container = driver.findElement(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'otp') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'one-time')]") );
            WebElement input = container.findElement(By.xpath(".//input[1]"));
            if (input != null) { input.clear(); input.sendKeys(otp); return; }
        } catch (Exception ignored) {}
    }

    public void submitOtpIfPresent() {
        By[] submitOtpSelectors = new By[] { By.xpath("//button[contains(., 'Verify') or contains(., 'Confirm') or contains(., 'Submit') or contains(., 'Continue')]") };
        for (By s : submitOtpSelectors) {
            try {
                WebElement b = driver.findElement(s);
                if (b != null && b.isDisplayed()) { b.click(); Thread.sleep(600); return; }
            } catch (Exception ignored) {}
        }
    }

    public boolean waitForESignBlockPopup(String expectedEmail, int timeoutSeconds) {
        try {
            // Print current page state
            System.out.println("[esign-debug] Current URL: " + driver.getCurrentUrl());
            System.out.println("[esign-debug] Page title: " + driver.getTitle());
            
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement popup = shortWait.until(d -> {
                // Try all visible elements containing the email address
                try {
                    List<WebElement> allElements = d.findElements(By.xpath("//*[contains(text(), '" + expectedEmail + "')]"));
                    if (!allElements.isEmpty()) {
                        System.out.println("[esign-debug] Found element with email text");
                        return allElements.get(0);
                    }
                } catch (Exception e1) {}
                
                // Try dialog/modal containers
                try {
                    List<WebElement> candidates = d.findElements(By.xpath("//div[contains(@class,'v-dialog') or contains(@class,'modal') or contains(@role,'dialog') or contains(@class,'toast') or contains(@class,'notification') or contains(@class,'popup')]"));
                    for (WebElement p : candidates) {
                        try { 
                            if (p.isDisplayed() && p.getText() != null && p.getText().contains(expectedEmail)) {
                                System.out.println("[esign-debug] Found dialog with email: " + p.getText().substring(0, Math.min(100, p.getText().length())));
                                return p; 
                            } 
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e2) {}
                
                // Search for text mentioning e-sign, blocked, signing
                try {
                    List<WebElement> others = d.findElements(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'e-sign') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'blocked') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'signing')]"));
                    for (WebElement o : others) {
                        try { 
                            if (o.isDisplayed() && o.getText() != null && (o.getText().contains(expectedEmail) || o.getText().contains("blocked") || o.getText().contains("sign"))) {
                                System.out.println("[esign-debug] Found e-sign related element: " + o.getText().substring(0, Math.min(100, o.getText().length())));
                                return o; 
                            } 
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e3) {}
                
                return null;
            });
            return popup != null;
        } catch (Exception e) {
            System.out.println("ESign block popup not found: " + e.getMessage());
            
            // Additional debugging: print page HTML snippet around potential popup areas
            try {
                String pageSource = driver.getPageSource();
                if (pageSource.contains("e-sign") || pageSource.contains("block")) {
                    System.out.println("[esign-debug] Page contains 'e-sign' or 'block' keywords");
                }
                if (pageSource.contains(expectedEmail)) {
                    System.out.println("[esign-debug] Page contains the email address");
                }
            } catch (Exception ignored) {}
            
            return false;
        }
    }
}
