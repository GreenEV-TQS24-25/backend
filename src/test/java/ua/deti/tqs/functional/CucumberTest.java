package ua.deti.tqs.functional;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@SuppressWarnings("java:S2187")
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("ua/deti/tqs/functional")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "ua.deti.tqs.functional")
public class CucumberTest {

}