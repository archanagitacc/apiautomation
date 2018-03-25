package com.nbnco.csa.microservicetest.util;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class ConstantsUtil {
	
	public static final String initialDgnosticHost= GenericUtil.getProprty("initialDignostcHost");
	public static final String postInstallHost= GenericUtil.getProprty("postInstallHost");
	public static final String jedishostName = GenericUtil.getProprty("jedishostName");
	public static final String REPORTS_PATH =  GenericUtil.getProprty("extentReport");
	public static final String ENV = GenericUtil.getCurrentEnvironmentName();
	public static String correlationId= "0e8cedd0-ad98-11e6-bf2e-47644ada7c0f";
	
	//public static final Long JMS_TIMEOUT = Long.parseLong(PropertiesUtil.getProprty("JMS_TIMEOUT"));
	public static final Long JMS_TIMEOUT = 250000L;
	public final static String NOTIFICATION_INPUT_TOPIC = GenericUtil.getProprty("notificationInputTopic");
	public final static String BOOTSTRAP_SERVERS =GenericUtil.getProprty("kafkaBootstrapServer");
	
	public static Connection connection = null;
	public static Session session = null;
	public static MessageProducer messageProducer = null;
	public static MessageConsumer messageConsumer = null;

}
