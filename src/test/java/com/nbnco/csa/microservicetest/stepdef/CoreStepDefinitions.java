package com.nbnco.csa.microservicetest.stepdef;

import com.nbnco.csa.microservicetest.util.*;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.json.JSONObject;
import org.junit.Assert;

import static com.nbnco.csa.microservicetest.util.GenericUtil.getJSONParameterValue;
import static com.nbnco.csa.microservicetest.util.GenericUtil.isJSON;
import static com.nbnco.csa.microservicetest.util.MessageConsumerUtil.getRequest;


public class CoreStepDefinitions extends MicroServiceTestWrapper{


    /* The following functionality are step definitions for Cucumber*/


    @Given("^I have a request for (.*?) service with (.*?) on (.*?)$")
    public void makeHTTPPostRequest(String key, String jsonFile, String endPoint) {
        String url = GenericUtil.getProprty(key) + endPoint;
        System.out.println("POST request to " + endPoint + " with file " + jsonFile);
        connection = MessageConsumerUtil.postDataGetConnectionObject(url, jsonFile);
    }

    @Then("^Verify Post Response (.*?)$")
    public void verifyDiagnosticPostResponse(String responseCode) {
        try {
            int statusCode = connection.getResponseCode();
            scenario.write("Microservice response code : " + statusCode + ". Expected response code: " + responseCode);
            if (statusCode == Integer.parseInt(responseCode) && Integer.parseInt(responseCode) == 202) {
                responseContent = MessageConsumerUtil.getHTTPConectionResponseContent(connection);
                System.out.println("Microservice Response : " + responseContent);
                responseJson = isJSON(responseContent);
                // TODO: Vineel: Fix this for validation of key before reading the value
                MicroServiceTestWrapper.transactionId = getJSONParameterValue(responseJson, "transactionId");
                correlationId = getJSONParameterValue(responseJson, "x-nbn-breadcrumbId");
                PostInstallGUIID = getJSONParameterValue(responseJson,"correlationId");
            } else if (statusCode == Integer.parseInt(responseCode) && Integer.parseInt(responseCode) == 400) {
                //Assert.assertTrue("Response code is as expected "+statusCode, Integer.parseInt(responseCode) == 400);
                Assert.assertTrue("Response code is as expected " + statusCode, Integer.parseInt(responseCode) == 400);
            } else {
                //Assert.assertFalse("Not a valid response code from post "+statusCode, true);
                Assert.assertFalse("Not a valid response code from post " + statusCode, true);
                Assert.assertFalse("HTTP Response code returned ("+statusCode+") does not match expected response code ("+responseCode+")", true);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Given("^I submit a DiagnosticRequest message via HTTP POST, sending <RequestJSON> in the body to <endPoint>$")
    public void iSubmitADiagnosticRequestMessageViaHTTPPOSTSendingRequestJSONInTheBodyToEndPoint() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^verify Initial diagnostic response$")
    public void VerifyResponse(){
        // TODO: Vineel: investigate why is this different from verifyDiagnosticPostResponse
        responseContent = MessageConsumerUtil.getHTTPConectionResponseContent(connection);
        responseJson = isJSON(responseContent);
        correlationId = getJSONParameterValue(responseJson,"X-nbn-breadCrumbId");
    }

    @Then("^verify Initial diagnostic response (.*?)$")
    public void VerifyResponseWithAssertion(String responseCode){
        // TODO: Vineel: investigate why is this different from verifyDiagnosticPostResponse & Verify Response method above
        int serverResponseCode = MessageConsumerUtil.getHTTPConectionResponseCode(connection);
        if(Integer.parseInt(responseCode)!=serverResponseCode){
            Assert.assertFalse("Response Code is not "+responseCode, true);
        }
    }

    @Then("^Get the response from (.*?) with correlationId with status (.*?) for service (.*?)$")
    public void getMicroserviceResponse(String endPoint, String status, String key){
        getServiceResponse(endPoint, status, key, false);

    }

    @Then("^Poll and get response from (.*?) with correlationId with status (.*?) for service (.*?)$")
    public void getPollingResponse(String endPoint, String status, String key){
        getServiceResponse(endPoint, status, key, true);
    }

    @Then("^Validate Response from microservice based on (.*?) json file$")
    public boolean verifyMicroserviceOutputforValidatedScenarios(String verificationJsonFileName){
        try {
            responseJson = isJSON(responseContent);
            return verifyResponseAttributes(responseJson, verificationJsonFileName );

        } catch (Exception e) {
            return false;
        }
    }

    @Then("^Validate Polling Responses$")
    public void validatePollingResponses(){

    }

    @Then("^Wait for Status (.*?) in GET Response for service (.*?) on (.*?) for (.*?) seconds$")
    public void waitUntilStatusCodeForGet(String statusCode, String hostUrl, String endPoint, String timeOut) {
        String url = GenericUtil.getProprty(hostUrl) + endPoint + "/" + MicroServiceTestWrapper.transactionId;
        long startTime = System.nanoTime();
        final long NANOSEC_PER_SEC = 1000l*1000*1000;
        JSONObject jsonObject;
        String jsonValue;
        while((System.nanoTime()-startTime)< Integer.valueOf(timeOut)*NANOSEC_PER_SEC) {
            String getResponse = getRequest(url);
            jsonObject = isJSON(getResponse);
            jsonValue = getJSONParameterValue(jsonObject, "status");
            if(jsonValue != null) {
                if (jsonValue.equalsIgnoreCase(statusCode)) {
                    Assert.assertTrue("Found Status " + statusCode + " from get response", true);
                    return;
                }
            }
        }
        Assert.assertFalse("Failed to find status code "+statusCode+"within given timeout "+timeOut+" seconds", true);
    }


}
