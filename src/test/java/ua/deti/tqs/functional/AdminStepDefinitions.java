package ua.deti.tqs.functional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class AdminStepDefinitions {

    @Given("I am on the login page")
    public void i_am_on_login_page() {
        CucumberTest.driver.get("http://localhost/login");
    }

    @When("I enter admin credentials {string} and {string}")
    public void i_enter_admin_credentials(String email, String password) {
        CucumberTest.driver.findElement(By.id("email")).sendKeys(email);
        CucumberTest.driver.findElement(By.id("password")).sendKeys(password);
    }

    @When("I click the login button")
    public void i_click_login_button() {
        CucumberTest.driver.findElement(By.cssSelector(".bg-primary")).click();
    }

    @Then("I should be logged into the admin dashboard")
    public void i_should_be_logged_in() throws InterruptedException {
        sleep(2000);
        assertTrue(CucumberTest.driver.getCurrentUrl().contains("dashboard"));
    }

    @Given("the admin user is logged into the system")
    public void admin_user_logged_in() throws InterruptedException {
        i_am_on_login_page();
        i_enter_admin_credentials("admin@admin.com", "password123");
        i_click_login_button();
        i_should_be_logged_in();
    }

    @When("I navigate to the charger management section")
    public void i_navigate_to_charger_management() {
        WebElement element = CucumberTest.driver.findElement(By.cssSelector(".bg-primary"));
        Actions builder = new Actions(CucumberTest.driver);
        builder.moveToElement(element).perform();
        CucumberTest.driver.findElement(By.cssSelector(".hover\\3A bg-accent:nth-child(1)")).click();
    }

    @When("I interact with the map interface")
    public void i_interact_with_map() {
        CucumberTest.driver.findElement(By.cssSelector(".inline-flex:nth-child(2)")).click();
        CucumberTest.driver.findElement(By.cssSelector(".space-y-2")).click();
        CucumberTest.driver.findElement(By.cssSelector(".inset-0")).click();
        CucumberTest.driver.findElement(By.cssSelector(".leaflet-marker-icon:nth-child(1)")).click();
    }

    @When("I select a charger location")
    public void i_select_charger_location() {
        CucumberTest.driver.findElement(By.cssSelector("a:nth-child(2) > .h-9")).click();
    }

    @When("I enter charger name {string}")
    public void i_enter_charger_name(String chargerName) {
        CucumberTest.driver.findElement(By.id("name")).click();
        CucumberTest.driver.findElement(By.id("name")).sendKeys(chargerName);
    }

    @When("I save the charger configuration")
    public void i_save_charger_configuration() {
        CucumberTest.driver.findElement(By.cssSelector(".bg-primary")).click();
    }

    @Then("the charger should be successfully created")
    public void charger_should_be_created() {
        assertTrue("Charger creation should be successful",
                CucumberTest.driver.getPageSource().contains("Downtown Charger!"));
    }
}