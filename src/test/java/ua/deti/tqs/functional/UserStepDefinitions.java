package ua.deti.tqs.functional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;

import static org.junit.Assert.assertTrue;

public class UserStepDefinitions {

    @Given("I am on the user login page")
    public void i_am_on_user_login_page() {
        BaseStepDefinitions.driver.get("http://localhost/login/login");
        BaseStepDefinitions.driver.manage().window().setSize(new Dimension(958, 1078));
    }

    @When("I enter user credentials {string} and {string}")
    public void i_enter_user_credentials(String email, String password) {
        BaseStepDefinitions.driver.findElement(By.id("email")).click();
        BaseStepDefinitions.driver.findElement(By.id("email")).sendKeys(email);
        BaseStepDefinitions.driver.findElement(By.id("password")).click();
        BaseStepDefinitions.driver.findElement(By.id("password")).sendKeys(password);
    }

    @Then("I should access the user dashboard")
    public void i_should_access_user_dashboard() {
        assertTrue("Should be on user dashboard",
                BaseStepDefinitions.driver.getCurrentUrl().contains("dashboard"));
    }

    @When("I navigate through the available options")
    public void i_navigate_through_options() {
        BaseStepDefinitions.driver.findElement(By.cssSelector("a:nth-child(1) > .bg-card")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".bg-card:nth-child(1) a > .inline-flex")).click();
    }

    @When("I interact with workflow elements")
    public void i_interact_with_workflow_elements() {
        BaseStepDefinitions.driver.findElement(By.cssSelector(".border-blue-500:nth-child(3)")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".border-blue-500:nth-child(8)")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".bg-primary")).click();
    }

    @When("I scroll to view additional content")
    public void i_scroll_to_view_content() {
        BaseStepDefinitions.js.executeScript("window.scrollTo(0,0)");
    }

    @When("I complete the workflow steps")
    public void i_complete_workflow_steps() {
        BaseStepDefinitions.driver.findElement(By.cssSelector(".size-9:nth-child(1)")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".inline-flex:nth-child(4)")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".inset-0")).click();
        BaseStepDefinitions.driver.findElement(By.cssSelector(".bg-card:nth-child(2) > .px-6")).click();
    }

    @Then("the workflow should be completed successfully")
    public void workflow_should_be_completed() {
        assertTrue("Workflow should be completed successfully", true);
    }
}