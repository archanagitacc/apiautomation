package com.nbnco.csa.microservicetest.stepdef;

import cucumber.api.java.en.Then;

public class JedisStepDefinitions{

    @Then("^Consume messages from jeddis cache (.*?)$")
    public void consumeMessagesFromJeddis(String notificationCount){
        //cacheResponseList = fetchResponseFromCacheProxy(Integer.parseInt(notificationCount));
    }

    @Then("^Validate jeddis messages (.*?)$")
    public void validateJeddisMessages(String ExpectedNotification){
        switch (ExpectedNotification) {
            case "OverallCompleted":
        }
    }

}
