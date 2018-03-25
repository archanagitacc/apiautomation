package com.nbnco.csa.microservicetest.stepdef;

import com.nbnco.csa.microservicetest.util.WMQClient;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.json.JSONException;
import org.junit.Assert;

import java.util.*;

public class WMQStepDefinitions extends MicroServiceTestWrapper {

    public WMQClient wmqClient;
    public HashMap<String, String> wmqMessageRead;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
        wmqClient = new WMQClient();

    }

    @Given("^Connect to WMQ host (.*?) at port (.*?) with queue (.*?)$")
    public void createAMQConnection(String host, String port, String queueManager) throws JSONException {
        try {
            wmqClient.createWMQConnection(host, port, queueManager);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Then("^Post WMQ Request for queue (.*?) and replyTo (.*?) with request (.*?)$")
    public void postRequest(String queueName, String replyTo, String xmlFileName) throws Exception {
        wmqClient.sendMessageToWMQ(xmlFileName, queueName, replyTo);
    }

    @Then("^Get WMQ Response for queue (.*?)$")
    public void getResponse(String queueName) throws Exception {
        wmqMessageRead = wmqClient.getMessageFromJMSQueue(queueName);
    }

    @Then("^Validate WMQ Response for (.*?) key and (.*?) value$")
    public void validateWMQResponse(String key, String value) {
        HashMap<String, String> actualData = wmqClient.splitRawData(wmqMessageRead.get("raw"));
        if (actualData.containsKey(key)) {
            if (actualData.get(key).equalsIgnoreCase(value)) {
                Assert.assertTrue("WMQ response validation successful", true);
                return;
            }
        }
        System.out.println("Excepted key: " + key + " value: " + value);
        System.out.println("Actual Response: " + actualData);
        Assert.assertFalse("Failed to validate WMQ response", true);
    }

}
