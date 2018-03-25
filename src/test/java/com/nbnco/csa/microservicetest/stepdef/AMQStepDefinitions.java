package com.nbnco.csa.microservicetest.stepdef;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Session;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import com.nbnco.csa.microservicetest.util.ConstantsUtil;
import com.nbnco.csa.microservicetest.util.MessageConsumerUtil;
import com.nbnco.csa.microservicetest.util.GenericUtil;

public class AMQStepDefinitions extends MicroServiceTestWrapper {

    String PostInstallGUIID = null;
    Session session = null;
    List<String> notificationList = null;
    String message = null;


    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @Then("^Connect to AMQ for (.*?)$")
    public Session createAMQConnection(String brokerName) throws JSONException {
        String jsonObjectString = GenericUtil.readJSONFileAndConvertToJSONObject("/src/test/resources/config/AMQConfigurations.json");
        JSONObject jsonObject = GenericUtil.isJSON(jsonObjectString);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get(brokerName);
        String AMQ_User = GenericUtil.getJSONParameterValue(jsonObject1, "username");
        String AMQ_Password = GenericUtil.getJSONParameterValue(jsonObject1, "password");
        String AMQ_Broker_Url = GenericUtil.getJSONParameterValue(jsonObject1, "url");

        try {
            session = MessageConsumerUtil.createConnection(AMQ_User, AMQ_Password, AMQ_Broker_Url);
            scenario.write("Connection to AMQ is successfull with user:" + AMQ_User + " AMQ Password: " + AMQ_Password + " AMQ Broker Url " + AMQ_Broker_Url);
        } catch (Throwable t) {
            scenario.write("Unable to connect AMQ with user:" + AMQ_User + " AMQ Password: " + AMQ_Password+ " AMQ Broker Url " + AMQ_Broker_Url);
            t.printStackTrace();
        }
        return session;
    }

    @Then("^Consume the message from AMQ for (.*?)$")
    public List<String> consumeQueue(String queue) {
        notificationList = new ArrayList<String>();
        try {
            do {
                message = MessageConsumerUtil.ConsumeFromAMQ(ConstantsUtil.session, PostInstallGUIID, queue);
                System.out.println("Message--------------" + message);
                if (message != null) {
                    notificationList.add(message);
                }
            } while (message != null);
            scenario.write("Total notifications received: " + notificationList.size());
            System.out.println(notificationList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MessageConsumerUtil.close();
        }
        return notificationList;
    }

    @Then("^Validate AMQ Messages for response (.*?)$")
    public void validateAMQNotifications(String ExpectedNotification) {
        //JSONObject responseJson = ClientUtilities.isJSON(message);
        //ClientUtilities.verifyResponseAttributes(responseJson, ExpectedNotification );

        switch (ExpectedNotification) {
            case "OverallCompleted":
                System.out.println("Total Notifications in the AMQ " + notificationList.size());
                if (notificationList.size() == 7) {
                    validateNotification(notificationList.get(0), GenericUtil.getDataFile("PostInstallAcceptedNotification"));
                    validateNotification(notificationList.get(1), GenericUtil.getDataFile("PostInstallOverallInprogressNotification"));
                    validateNotification(notificationList.get(2), GenericUtil.getDataFile("PostInstallOverallInprogressNTDInprogress"));
                    validateNotification(notificationList.get(3), GenericUtil.getDataFile("PostInstallNTDCompletedPass"));
                    validateNotification(notificationList.get(4), GenericUtil.getDataFile("PostInstallLoopbackInprogress"));
                    validateNotification(notificationList.get(5), GenericUtil.getDataFile("PostInstallLoopbackCompletedPass"));
                    validateNotification(notificationList.get(6), GenericUtil.getDataFile("PostInstallOverallCompletedPass"));
                } else {
                    //Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is"+notificationList.size(),true );
                    Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is" + notificationList.size(), true);
                }
                break;
            case "OverallCancelledNTDFail":
                System.out.println("Total Notifications in the AMQ " + notificationList.size());
                if (notificationList.size() == 5) {
                    validateNotification(notificationList.get(0), GenericUtil.getDataFile("PostInstallAcceptedNotification"));
                    validateNotification(notificationList.get(1), GenericUtil.getDataFile("PostInstallOverallInprogressNotification"));
                    validateNotification(notificationList.get(2), GenericUtil.getDataFile("PostInstallOverallInprogressNTDInprogress"));
                    validateNotification(notificationList.get(3), GenericUtil.getDataFile("PostInstallOverallInprogressNTDCompletedFail"));
                    validateNotification(notificationList.get(4), GenericUtil.getDataFile("OverallCompetedFailNTD"));

                } else {
                    //Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is"+notificationList.size(),true );
                    Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is" + notificationList.size(), true);
                }
                break;
            case "OverallCompletedLBFail":
                System.out.println("Total Notifications in the AMQ " + notificationList.size());
                if (notificationList.size() == 7) {
                    validateNotification(notificationList.get(0), GenericUtil.getDataFile("PostInstallAcceptedNotification"));
                    validateNotification(notificationList.get(1), GenericUtil.getDataFile("PostInstallOverallInprogressNotification"));
                    validateNotification(notificationList.get(2), GenericUtil.getDataFile("PostInstallOverallInprogressNTDInprogress"));
                    validateNotification(notificationList.get(3), GenericUtil.getDataFile("PostInstallNTDCompletedPass"));
                    validateNotification(notificationList.get(4), GenericUtil.getDataFile("PostInstallLoopbackInprogress"));
                    validateNotification(notificationList.get(5), GenericUtil.getDataFile("OverallInprogressLBCompletedFail"));
                    validateNotification(notificationList.get(6), GenericUtil.getDataFile("PostInstallOverallCompetedFailLB"));

                } else {
                    //Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is"+notificationList.size(),true );
                    Assert.assertFalse("Expected seven notification for completed pass scenario,  actual notification count is" + notificationList.size(), true);
                }
                break;
            default:
                break;
        }

    }


}
