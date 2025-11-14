package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void acceptCookiesIfPresent() {
        List<By> cookieSelectors = Arrays.asList(
            By.id("accept"),
            By.id("cookie-consent"),
            By.xpath("//button[contains(translate(., 'ACCEPT', 'accept'), 'accept') or contains(., 'I agree') or contains(., 'Accept') or contains(., 'Agree') ]"),
            By.cssSelector("button.cookie-accept"),
            By.cssSelector("button[aria-label='Accept cookies']"),
            By.xpath("//button[contains(@class,'accept') and contains(.,'Accept')]")
        );

        for (By sel : cookieSelectors) {
            try {
                WebElement cookieBtn = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (cookieBtn != null && cookieBtn.isDisplayed()) {
                    cookieBtn.click();
                    Thread.sleep(700);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void open(String url) {
        driver.navigate().to(url);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    public void clickLoginButton() {
        // try common selectors for login
        By[] loginSelectors = new By[] {
            By.id("login"),
            By.xpath("//button[contains(translate(., 'LOGIN', 'login'), 'login') or contains(., 'Login') or contains(., 'Sign in') or contains(., 'Log in') ]"),
            By.xpath("//a[contains(., 'Login') or contains(., 'Sign in') or contains(., 'Log in')]")
        };
        for (By sel : loginSelectors) {
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (btn != null && btn.isDisplayed()) {
                    btn.click();
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Login button not found using known selectors");
    }

    public void enterUsername(String username) {
        By[] usernameSelectors = new By[] {
            By.cssSelector("input[type='email']"),
            By.name("username"),
            By.id("username"),
            By.cssSelector("input[placeholder*='email']"),
            By.cssSelector("input[placeholder*='Email']")
        };
        for (By sel : usernameSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                if (el != null && el.isDisplayed()) {
                    el.clear();
                    el.sendKeys(username);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Username/email input not found");
    }

    public void enterPassword(String password) {
        By[] passwordSelectors = new By[] {
            By.cssSelector("input[type='password']"),
            By.name("password"),
            By.id("password")
        };
        for (By sel : passwordSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                if (el != null && el.isDisplayed()) {
                    el.clear();
                    el.sendKeys(password);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Password input not found");
    }

    public void submitLogin() {
        By[] submitSelectors = new By[] {
            By.xpath("//button[@type='submit' and (contains(., 'Login') or contains(., 'Sign in') or contains(., 'Log in'))]"),
            By.xpath("//button[contains(., 'Login') or contains(., 'Sign in') or contains(., 'Log in')]")
        };
        for (By sel : submitSelectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(sel));
                if (el != null && el.isDisplayed()) {
                    el.click();
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // fallback: submit form by sending ENTER to password field
        try {
            WebElement pw = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='password']")));
            pw.sendKeys("\n");
        } catch (Exception e) {
            throw new RuntimeException("Unable to submit login form");
        }
    }
}
