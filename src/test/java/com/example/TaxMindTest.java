package com.example;

import com.example.pages.LoginPage;
import com.example.pages.OTPPage;
import com.example.pages.ProfilePage;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.example.pages.ApplicationPage;
import com.example.pages.QuestionnairePage;

import static org.junit.Assert.assertTrue;

public class TaxMindTest extends BaseTest {
    private static final String EMAIL = "jishnu+1@ileafsolutions.com";
    private static final String PASSWORD = "Test@123";
    private static final String SITE_URL = "https://dev.taxmind.ie/";

    @Test
    public void testLoginFlow() {
        LoginPage loginPage = new LoginPage(driver, wait);
        OTPPage otpPage = new OTPPage(driver, wait);
        ProfilePage profilePage = new ProfilePage(driver, wait);

        try {
            loginPage.open(SITE_URL);
            loginPage.acceptCookiesIfPresent();
            loginPage.clickLoginButton();
            loginPage.enterUsername(EMAIL);
            loginPage.enterPassword(PASSWORD);
            loginPage.submitLogin();

            // enter OTP
            otpPage.enterOtp("123456");
            otpPage.submitOtp();

            // verify profile
            boolean atProfile = profilePage.isAtProfile();
            System.out.println("At profile: " + atProfile);
            assertTrue("Expected to land on profile page after OTP", atProfile);

            // navigate to Applications and continue 2026 application
            ApplicationPage appPage = new ApplicationPage(driver, wait);
            appPage.openApplicationsFromProfile();
            WebElement app2026 = appPage.findApplicationForYear("2026");
            appPage.continueApplication(app2026);

            // fill questionnaire and submit
            QuestionnairePage qp = new QuestionnairePage(driver, wait);
            qp.fillMandatoryQuestions();
            qp.submitQuestionnaire();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Login flow failed: " + e.getMessage());
        }
    }
}
