package com.nbnco.csa.microservicetest;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features="src/test/resources/feature/MSDiagnosticTestKafka.feature",plugin = {"pretty", "html:target/reports/cucumber-report"})
public class CucumberJunitTest {
}
