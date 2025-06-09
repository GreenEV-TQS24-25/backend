package ua.deti.tqs.functional;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@SuppressWarnings("java:S2187")
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("ua/deti/tqs/functional")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "ua.deti.tqs.functional")
public class CucumberTest {
    protected static WebDriver driver;
    protected static JavascriptExecutor js;

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
        js = (JavascriptExecutor) driver;
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}