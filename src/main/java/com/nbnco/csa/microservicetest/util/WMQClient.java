package com.nbnco.csa.microservicetest.util;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQTopicConnectionFactory;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.junit.Assert;

public class WMQClient {

    public QueueConnection connInputQueue;
    public TopicConnection connInputTopic;
    public QueueConnection connOutputQueue;
    public Queue replyToQueue;
    public String correlationID;
    public MessageConsumer consumer;


    public void createWMQConnection(String host, String port, String queueManager) throws JMSException {
        correlationID = UUID.randomUUID().toString();
        MQQueueConnectionFactory cfInputQueue = new MQQueueConnectionFactory();
        cfInputQueue.setHostName(host);
        cfInputQueue.setPort(Integer.parseInt(port));
        cfInputQueue.setQueueManager(queueManager);//service provider
        cfInputQueue.setTransportType(1);
        /*Create Connection */
        connInputQueue = cfInputQueue.createQueueConnection();
        connInputQueue.start();

        //Create Connection Topic Queue
        MQTopicConnectionFactory cfInputTopic = new MQTopicConnectionFactory();
        cfInputTopic.setHostName(host);
        cfInputTopic.setPort(Integer.parseInt(port));
        cfInputTopic.setQueueManager(queueManager);//service provider
        cfInputTopic.setTransportType(1);
        connInputTopic = cfInputTopic.createTopicConnection();

        //Create Connection Output Queue
        MQQueueConnectionFactory cfOutputQueue = new MQQueueConnectionFactory();
        cfOutputQueue.setHostName(host);
        cfOutputQueue.setPort(Integer.parseInt(port));
        cfOutputQueue.setQueueManager(queueManager);//service provider
        cfOutputQueue.setTransportType(1);
        /*Create Connection */
        connOutputQueue = cfOutputQueue.createQueueConnection();
    }


    public void sendMessageToWMQ(String xmlFileName, String queueName, String replyTo) throws Exception {
        String xmlData = GenericUtil.xmlread(xmlFileName);
        String messageText = updateXML(xmlData);
        Assert.assertTrue("JMS Queue Name is empty/null !!", queueName != null && !queueName.isEmpty());
        /*Create session */
        QueueSession session = connInputQueue.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue destination = session.createQueue(queueName);

        /*Create sender queue */
        QueueSender sender = session.createSender(destination);

        /*Create text message */
        TextMessage message = session.createTextMessage(messageText);
        //message.setJMSMessageID(correlationID);
        message.setJMSCorrelationID(correlationID);

        if (replyTo != null) {
            replyToQueue = session.createQueue(replyTo);
            message.setJMSReplyTo(replyToQueue);
        }
        sender.send(message);
        sender.close();
        session.close();
    }

    public HashMap<String, String> getMessageFromJMSQueue(String name) throws JMSException, UnsupportedEncodingException {
        long timeout = 90000;
        String filter = "JMSCorrelationID='" + correlationID + "'";
        Assert.assertTrue("JMS Queue Name is empty/null !!", name != null && !name.isEmpty());
        QueueSession session = connOutputQueue.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        MQQueue from = (MQQueue) session.createQueue(name);
        consumer = session.createConsumer(from, filter);
        connOutputQueue.start();
        Message message = consumer.receive(timeout);
        HashMap<String, String> ret = splitJmsMessage(message.toString());
        connOutputQueue.stop();
        return ret;
    }


    public String updateXML(String xml) {
        if (xml.contains("<correlationId>"))
            xml = xml.replaceAll("<correlationId>([\\s\\S]*)<\\/correlationId>", "<correlationId>" + correlationID + "</correlationId>");
        else if (xml.contains("correlationId:"))
            xml = xml.replaceAll("correlationId:([\\s\\S]*)", "correlationId:" + correlationID);
        else if (xml.contains("correlationId="))
            xml = xml.replaceAll("correlationId=([\\s\\S]{37})", "correlationId=\"" + correlationID);

        return xml;
    }


    public HashMap<String, String> splitJmsMessage(String message) {
        // Split the message response into a HashMap with keys - body, header and raw response
        HashMap<String, String> hashMap = new HashMap<>();
        Pattern pattern;
        if (message.contains("soap:")) {
            pattern = Pattern.compile("<soap:Header>([\\s\\S]*)<\\/soap:Header>[\\s\\S]*<soap:Body>([\\s\\S]*)<\\/soap:Body>");
        } else {
            pattern = Pattern.compile("<soapenv:Header>([\\s\\S]*)<\\/soapenv:Header>[\\s\\S]*<soapenv:Body>([\\s\\S]*)<\\/soapenv:Body>");
        }

        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            hashMap.put("header", matcher.group(1));
            hashMap.put("body", matcher.group(2));
        } else {
            hashMap.put("header", "");
            hashMap.put("body", "");
        }
        hashMap.put("raw", message);
        return hashMap;
    }

    public HashMap<String, String> splitRawData(String rawMessage) {
        String[] jmsRawMsgList = rawMessage.split("\n");
        String[] tempSplit;
        HashMap<String, String> refMap = new HashMap<String, String>();
        for (String currentMsg : jmsRawMsgList) {
            if (currentMsg.length() != 0) {
                tempSplit = currentMsg.split(":");
                refMap.put(tempSplit[0].trim(), tempSplit[tempSplit.length - 1].trim());
            }
        }
        return refMap;
    }

}