// AuthenticationStepDefinitions.java
package ua.deti.tqs.functional;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class AuthenticationStepDefinitions {

    @When("I enter credentials {string} and {string}")
    public void enter_credentials(String email, String password) {
        BaseStepDefinitions.driver.findElement(By.id("email")).sendKeys(email);
        BaseStepDefinitions.driver.findElement(By.id("password")).sendKeys(password);
    }

    @Then("I should see the {string} page")
    public void verify_expected_page(String expectedPage) throws InterruptedException {
        sleep(2000); // Wait for page to load, adjust as necessary
        switch (expectedPage) {
            case "admin_dashboard":
                assertTrue(BaseStepDefinitions.driver.getCurrentUrl().contains("dashboard"));
                break;
            case "user_dashboard":
                assertTrue(BaseStepDefinitions.driver.getCurrentUrl().contains("dashboard"));
                break;
            case "login_error":
                assertTrue(BaseStepDefinitions.driver.findElement(By.cssSelector(".error-message")).isDisplayed());
                break;
        }
    }
}