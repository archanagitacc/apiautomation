package com.nbnco.csa.microservicetest.stepdef;


import com.nbnco.csa.microservicetest.util.GenericUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import cucumber.api.java.en.Then;
import com.nbnco.csa.microservicetest.util.ConstantsUtil;

import java.util.Iterator;

public class KafkaStepDefinitions extends MicroServiceTestWrapper {

    // TODO: Vineel : Parameterize this
    @Then("^Consume messages from kafka topic for (.*?) and (.*?)$")
    public void consumeKafkaMessages(String kafkaTopic, String expectedMessageQueue) {
        consumeMessagesFromKafka(GenericUtil.getProprty(kafkaTopic), getMessageQueueLength(expectedMessageQueue));
        System.out.println("consumeKafkaMessages on kafkaTopic:" + kafkaTopic + ", expectedMessageQueue:" + expectedMessageQueue);
    }

    @Then("^Consume the DPU Status notification messages from kafka topic (.*?)$")
    public void consumeDpuStatusKafkaMessages(String msgQueue) {
        consumeMessagesFromKafka(ConstantsUtil.NOTIFICATION_INPUT_TOPIC, getMessageQueueLength(msgQueue));
        System.out.println("consumeDpuStatusKafkaMessages on msgQueue:" + msgQueue);
    }

    @Then("^Validate Kafka messages for response (.*?)$")
    public void validateKafkaMessages(String responseCode) {

        filterKafkaMessages();
        if (filteredNotificationList.size() == 0) {
            Assert.assertFalse("No messages from kafka topic", true);
        }
        String jsonObjectString = GenericUtil.readJSONFileAndConvertToJSONObject("/src/test/resources/config/referenceDataMap.json");
        JSONObject jsonObject = GenericUtil.isJSON(jsonObjectString);
        if (jsonObject.has(responseCode)) {
            try {
                JSONObject jsonObject1 = (JSONObject) jsonObject.get(responseCode);
                Iterator iterator = jsonObject1.keys();
                String key;
                if (jsonObject1.has("Notification")) {
                    if (!(Integer.valueOf((String) jsonObject1.get("Notification")) == filteredNotificationList.size())) {
                        System.out.println("Expected " + jsonObject1.get("Notification") + " notification for " + responseCode + ", actual notification count is " + filteredNotificationList.size());
                        Assert.assertFalse("Expected " + jsonObject1.get("Notification") + " notification for " + responseCode + ", actual notification count is " + filteredNotificationList.size(), true);
                    }
                } else {
                    if (filteredNotificationList.size() != jsonObject1.length()) {
                        System.out.println("Expected " + jsonObject1.length() + " notification for " + responseCode + ", actual notification count is " + filteredNotificationList.size());
                        Assert.assertFalse("Expected " + jsonObject1.length() + " notification for " + responseCode + ", actual notification count is " + filteredNotificationList.size(), true);
                    }
                    String fileName;
                    while (iterator.hasNext()) {
                        key = (String) iterator.next();
                        fileName = (String) jsonObject1.get(key);
                        if (!validateNotification(filteredNotificationList.get(Integer.valueOf(key)), fileName)) {
                            System.out.println("Failed to validation notification for " + responseCode + " failed to compare " + fileName + " content");
                            Assert.assertFalse("Failed to validation notification for " + responseCode + " failed to compare " + fileName + " content", true);
                        }
                    }
                }
            } catch (JSONException e) {
                System.out.println("No Property defined in referenceDataMap.json for response code:" + responseCode);
                Assert.assertFalse("No Property defined in referenceDataMap.json for response code:" + responseCode, true);
            }
        }
        System.out.println("Validation successful for response code: " + responseCode);
        Assert.assertTrue("Validation successful for response code: " + responseCode, true);
    }

    @Then("^Validate Kafka DPU Status for response (.*?)$")
    public void validateDPUStatusMessages(String outcome) {

        filterKafkaMessages();
        if (filteredNotificationList.size() == 0) {
            Assert.assertFalse("No messages from kafka topic", true);
        }

        switch (outcome) {
            case "CompletedPassed":
                if (filteredNotificationList.size() == 3) {
                    validateNotification(filteredNotificationList.get(0), GenericUtil.getDataFile("NotifyDPUStatusAccepted"));
                    validateNotification(filteredNotificationList.get(1), GenericUtil.getDataFile("NotifyDPUStatusInProgress"));
                    validateNotification(filteredNotificationList.get(2), GenericUtil.getDataFile("NotifyDPUStatusCompletedPassed"));
                } else {
                    Assert.assertFalse("Expected 3 notifications for Test Completed-Passed scenario,  actual notification count is " + filteredNotificationList.size(), true);
                }

                break;

            case "CompletedFailed":
                if (filteredNotificationList.size() == 3) {
                    validateNotification(filteredNotificationList.get(0), GenericUtil.getDataFile("NotifyDPUStatusAccepted"));
                    validateNotification(filteredNotificationList.get(1), GenericUtil.getDataFile("NotifyDPUStatusInProgress"));
                    validateNotification(filteredNotificationList.get(2), GenericUtil.getDataFile("NotifyDPUStatusCompletedFailed"));
                } else {
                    Assert.assertFalse("Expected 3 notifications for Test Completed-Failed scenario,  actual notification count is " + filteredNotificationList.size(), true);
                }

                break;

            case "Rejected":
                if (filteredNotificationList.size() == 1) {
                    validateNotification(filteredNotificationList.get(0), GenericUtil.getDataFile("NotifyDPUStatusRejected"));
                } else {
                    Assert.assertFalse("Expected 1 notification for Test Rejected scenario,  actual notification count is " + notificationList.size(), true);
                }

                break;

            case "Cancelled":
                if (filteredNotificationList.size() == 3) {
                    validateNotification(filteredNotificationList.get(0), GenericUtil.getDataFile("NotifyDPUStatusAccepted"));
                    validateNotification(filteredNotificationList.get(1), GenericUtil.getDataFile("NotifyDPUStatusInProgress"));
                    validateNotification(filteredNotificationList.get(2), GenericUtil.getDataFile("NotifyDPUStatusCancelled"));
                } else {
                    Assert.assertFalse("Expected 3 notifications for Test Cancelled scenario,  actual notification count is " + filteredNotificationList.size(), true);
                }

                break;
        }
    }
}