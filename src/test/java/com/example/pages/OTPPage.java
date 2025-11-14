package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OTPPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public OTPPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void enterOtp(String otp) {
        // try common OTP selectors
        By[] otpSelectors = new By[] {
            By.cssSelector("input[name*='otp']"), 
            By.cssSelector("input[id*='otp']"), 
            By.cssSelector("input[type='tel']"), 
            By.xpath("//input[@inputmode='numeric']") 
        };
        for (By sel : otpSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                if (el != null && el.isDisplayed()) {
                    el.clear();
                    el.sendKeys(otp);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // fallback: if OTP is split into multiple inputs, try to find inputs inside a specific container
        try {
            WebElement first = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form//input[1]")));
            if (first != null) {
                first.sendKeys(otp);
                return;
            }
        } catch (Exception ignored) {
        }

        // handle common split-OTP patterns (multiple single-char inputs)
        try {
            List<WebElement> otpInputs = driver.findElements(By.cssSelector("input.otp-input-figma, input.otp-input, input[data-qa='otp-input']"));
            if (otpInputs != null && otpInputs.size() >= otp.length()) {
                for (int i = 0; i < otp.length() && i < otpInputs.size(); i++) {
                    try {
                        WebElement in = otpInputs.get(i);
                        wait.until(ExpectedConditions.elementToBeClickable(in));
                        in.clear();
                        in.sendKeys(String.valueOf(otp.charAt(i)));
                    } catch (Exception ignored) {
                    }
                }
                return;
            }
        } catch (Exception ignored) {
        }
        // Debugging: save page source and list iframes to help diagnose why OTP wasn't found
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        Path htmlOut = Paths.get("target", "screenshots", "otp-failure-" + timestamp + ".html");
        Path logOut = Paths.get("target", "screenshots", "otp-failure-" + timestamp + ".log");
        try {
            Files.createDirectories(htmlOut.getParent());
            Files.write(htmlOut, driver.getPageSource().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            // ignore writing errors
        }

        List<WebElement> frames = driver.findElements(By.tagName("iframe"));
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(frames.size()).append(" iframe(s)\n");
        for (int i = 0; i < frames.size(); i++) {
            try {
                String src = frames.get(i).getAttribute("src");
                sb.append(i).append(": src=").append(src).append("\n");
            } catch (Exception ignored) {
            }
        }
        try {
            Files.write(logOut, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }

        // Try searching inside each iframe (switch into frame and try selectors)
        for (int i = 0; i < frames.size(); i++) {
            try {
                driver.switchTo().frame(i);
                for (By sel : otpSelectors) {
                    try {
                        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                        if (el != null && el.isDisplayed()) {
                            el.clear();
                            el.sendKeys(otp);
                            driver.switchTo().defaultContent();
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
            }
        }

        throw new RuntimeException("OTP input not found (page saved to: " + htmlOut.toAbsolutePath() + ", frames log: " + logOut.toAbsolutePath() + ")");
    }

    public void submitOtp() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Verify') or contains(., 'Submit') or contains(., 'Continue') or contains(., 'Login')]") ));
            btn.click();
        } catch (Exception e) {
            // ignore and rely on automatic submit if any
        }
    }
}
