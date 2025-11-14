package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProfilePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ProfilePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isAtProfile() {
        try {
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains("profile") || d.findElements(By.xpath("//*[contains(translate(., 'PROFILE', 'profile'), 'profile') or contains(., 'My Account') or contains(., 'My Profile')]")).size() > 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
