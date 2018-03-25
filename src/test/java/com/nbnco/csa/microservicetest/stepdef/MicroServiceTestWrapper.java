package com.nbnco.csa.microservicetest.stepdef;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import redis.clients.jedis.Jedis;
import com.nbnco.csa.microservicetest.base.CucumberBase;
import com.nbnco.csa.microservicetest.util.ConstantsUtil;
import com.nbnco.csa.microservicetest.util.MessageConsumerUtil;
import com.nbnco.csa.microservicetest.util.GenericUtil;

import static com.nbnco.csa.microservicetest.util.JsonUtils.replaceJsonValues;


public class MicroServiceTestWrapper extends CucumberBase{
    HttpURLConnection connection = null;
    String responseContent = null;
    JSONObject responseJson = null;
    List<String> notificationList = null;
    List<String> filteredNotificationList = null;
    public static String transactionId = null;
    protected String correlationId = null;
    protected String PostInstallGUIID = null;
    protected static final String STATUS_KEY = "Remedy_Red_Optical-";
    Map<String, String> cacheResponseList = null;

    public void filterKafkaMessages() {
        filteredNotificationList = new ArrayList<String>();
        String regex = "\\{.*";
        String tempString = null;
        final Pattern pattern = Pattern.compile(regex);
        for (int i = 0; i < notificationList.size(); i++) {
            if (notificationList.get(i).contains(transactionId)) {
                System.out.println("filterKafkaMessages: FOUND for transactionId:" + transactionId);
                final Matcher matcher = pattern.matcher(notificationList.get(i));
                while (matcher.find()) {
                    tempString = matcher.group(0);
                    System.out.println("filterKafkaMessages tempString: "+ tempString);
                    filteredNotificationList.add(tempString);
                }
            }
            else {
                System.out.println("filterKafkaMessages unmatched for transactionId: " +transactionId + " : " + notificationList.get(i));
            }
        }
    }

    protected boolean validateNotification(String response, String fileName){
        String expectedOutcome =GenericUtil.readJSONFileAndConvertToJSONObject(fileName);
        scenario.write("[validateNotification] filename:" + fileName +
                "\nActual JSON: "+response+
                "\nExpected JSON: "+expectedOutcome.replace("\t", ""));
        //scenario.write("Actual JSON: "+response);
        return GenericUtil.validateJson(modifyResponse(response), fileName);
    }

    public static boolean verifyResponseAttributes( JSONObject responseJson, String verificationJsonFileName) {
        // TODO: Vineel: Review possible duplication of ValidateNotification Method
        try {
            if(verificationJsonFileName == null || verificationJsonFileName == "")
                return true;

            String message = GenericUtil.readJSONFileAndConvertToJSONObject(verificationJsonFileName);
            JSONObject expectedJsonResults = new JSONObject(message);

            if( expectedJsonResults != null ) {

                // Parse verification JSON and iterate over expected key value pairs to compare against actual results

                boolean verificationStatus = GenericUtil.verifyJSONData(responseJson, expectedJsonResults, "") ;

                //Assert.assertTrue(verificationStatus);
                Assert.assertTrue("[verifyResponseAttributes] filename:"+ verificationJsonFileName +
                        "\nActual JSON :"+responseJson +
                        "\nExpected JSON:"+expectedJsonResults, verificationStatus);
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }


    protected String modifyResponse(String response) {
        try {
            JSONObject jsonObj = new JSONObject(response);
            Map<String, Object> map = new HashMap<>();
            map.put("WRI", "WRI000000000000");
            map.put("startDateTime", "DynamicStartTime");
            map.put("endDateTime", "DynamicEndTime");
            map.put("timestamp", "DynamicStartTime");
            map.put("correlationId", "DYNAMIC_CORRELATION_ID");
            map.put("nbnCorrelationId", "DYNAMIC_CORRELATION_ID");
            map.put("AVC", "AVC000000000000");
            map.put("transactionId", "DynamicTransaction");
            map.put("serviceId","DynamicServiceId");
            for (String key : map.keySet()) {
                try {
                    jsonObj = replaceJsonValues(jsonObj, key, (String) map.get(key));
                } catch (JSONException e) {
                    System.out.println("Warning: Could not find the key "+key+" in jsonObject");;
                }
            }
            return jsonObj.toString();
        }
        catch (JSONException e){
            return response;
        }

    }

    public void getServiceResponse(String endPoint, String status, String key, boolean pollForServiceResponse){
        String tempResponse = null;
        int count = 0;
        count++;

        if( correlationId == null|| correlationId == "") {
            Assert.assertFalse("Correlation Id for the initialDiagnostic request is null/empty", true);
        }
        String url = GenericUtil.getProprty("initialDignostcHost")+endPoint+"/"+correlationId;
        responseContent = MessageConsumerUtil.getRequest(url);
        JSONObject microserviceResponse = GenericUtil.isJSON(responseContent);
        System.out.println("Response of get request: "+responseContent);
        String outputKeyStatus = GenericUtil.getJSONParameterValue(microserviceResponse,"status");

        if( !outputKeyStatus.equals(status)){
            for (int i=0; i<=100; i++){
                responseContent = MessageConsumerUtil.getRequest(url);
                microserviceResponse = GenericUtil.isJSON(responseContent);
                outputKeyStatus = GenericUtil.getJSONParameterValue(microserviceResponse,"status");
                if(pollForServiceResponse) {
                    if (!tempResponse.equals(responseContent)) {
                        scenario.write("Notification " + count + ": " + responseContent);
                        tempResponse = responseContent;
                        System.out.println("**************Intermidiate Status key value from Automation code: " + outputKeyStatus);
                        System.out.println("************Response: " + responseContent);
                        count++;
                    }
                }
                if(outputKeyStatus!=null &&outputKeyStatus.equals(status)){
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if(!outputKeyStatus.equals(status) && !outputKeyStatus.equals("null")){
            Assert.assertFalse("Expected status is "+status+", actual is "+outputKeyStatus, true);
        }
        scenario.write("Response through GET request to API: "+responseContent);
    }

    @SuppressWarnings("unused")

    protected Map<String, String> fetchResponseFromCacheProxy(int messageCount) {

        Map<String, String> cacheResponseList = new HashMap<String, String>();
        Map<String, String> redisKeys = new ConcurrentHashMap<String, String>();
        try (Jedis jedis = MessageConsumerUtil.getConnection()) {

            String KeyValue = STATUS_KEY + correlationId;
            redisKeys = jedis.hgetAll(KeyValue);
            Iterator<String> iter = redisKeys.keySet().iterator();
            while(iter.hasNext()){
                String keyData=iter.next();
                cacheResponseList.putAll(redisKeys);
                iter.remove();
            }
            System.out.println(cacheResponseList.size());
            System.out.println(cacheResponseList);
            if (messageCount == cacheResponseList.size()){
                scenario.write("Got all expected notification count: "+cacheResponseList.size());
            }
            scenario.write("Response from Redis Cache:" + cacheResponseList);

        } catch (Exception e) {
            scenario.write("Exception while retrieving a message from Redis Cache: "+e.getMessage());
            Assert.assertFalse("Exception while retrieving a message from Redis Cache: "+e.getMessage(), true);
        }
        return cacheResponseList;
    }

    public void consumeMessagesFromKafka(String kafkaTopic, int msgLen){
        try {
            notificationList = MessageConsumerUtil.runConsumer(kafkaTopic, ConstantsUtil.BOOTSTRAP_SERVERS, transactionId, msgLen);
        } catch (Throwable t) {
            Assert.assertFalse("Exception while consuming messages from kafka", true);
        }
    }

    public int getMessageQueueLength(String msgQueue){
        String jsonObjectString = GenericUtil.readJSONFileAndConvertToJSONObject("/src/test/resources/config/referenceDataMap.json");
        JSONObject jsonObject = GenericUtil.isJSON(jsonObjectString);
        int size =3;
        if (jsonObject.has(msgQueue)) {
            try {
                JSONObject jsonObject1 = (JSONObject) jsonObject.get(msgQueue);
                Iterator iterator = jsonObject1.keys();
                size = jsonObject1.length();
            }
            catch (JSONException e){
                System.out.println("Json Exception");
            }
        }
        return size;
    }



}
