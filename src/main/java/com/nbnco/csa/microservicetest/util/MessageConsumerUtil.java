package com.nbnco.csa.microservicetest.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import javax.jms.*;
import javax.jms.Queue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

//Starting kafka server on windows machine - https://dzone.com/articles/running-apache-kafka-on-windows-os
//Study material : http://cloudurable.com/blog/kafka-tutorial-kafka-consumer/index.html


public class MessageConsumerUtil {

	 private static JedisPool redisPool;
	 private final static String TOPIC = "test";
	//private final static String BOOTSTRAP_SERVERS ="localhost:9092,localhost:9093,localhost:9094";
	 private final static String BOOTSTRAP_SERVERS ="localhost:9092";

	 private static Consumer<Long, String> createConsumer(String topic,String bootstrapServer)
	 {
	      final Properties props = new Properties();
	      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
	      props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
	      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,LongDeserializer.class.getName());
	      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

	      // Create the consumer using props.
	      final Consumer<Long, String> consumer =new KafkaConsumer<>(props);

	      // Subscribe to the topic.
	      consumer.subscribe(Collections.singletonList(topic));
	      return consumer;
	  }

	 public static List<String> runConsumer(String topic,String bootstrapServer, String transactionId, int msgLen) throws InterruptedException
	 {
	        final Consumer<Long, String> consumer = createConsumer(topic,bootstrapServer);
	        List<String> consumedMessages = new ArrayList<String>();
	        int msgCount =0;

	        //final int giveUp = 100;   int noRecordsCount = 0;
	     final int giveUp = 150;   int noRecordsCount = 0;

		 while (true) {
	        	msgCount=0;
	            final ConsumerRecords<Long, String> consumerRecords =
	                    consumer.poll(1000);

	            if (consumerRecords.count()==0) {
					noRecordsCount++;
					if (noRecordsCount > giveUp) break;
					else continue;
				}

	           /* consumerRecords.forEach(record -> {
	                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
	                        record.key(), record.value(),
	                        record.partition(), record.offset());
	            });*/
	            
	            for(ConsumerRecord<Long, String> record : consumerRecords){
	            	 consumedMessages.add(record.value().toString());
	             }
	            

	            consumer.commitAsync();
				for(String currentMessage : consumedMessages){
					if(currentMessage.contains(transactionId)) {
						msgCount++;
					}
					if(msgCount==msgLen){
						consumer.close();
						return consumedMessages;
				}

				}
	        }
	        consumer.close();
	        System.out.println("DONE");
	        return consumedMessages;
	    }

	public static void main(String... args) throws Exception {
		if (args.length == 0) {
			runProducer(5);
		} else {
			runProducer(Integer.parseInt(args[0]));
		}
	}

	private static Producer<Long, String> createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return new KafkaProducer<>(props);
	}

	static void runProducer(final int sendMessageCount) throws Exception {
		final Producer<Long, String> producer = createProducer();
		long time = System.currentTimeMillis();

		try {
			for (long index = time; index < time + sendMessageCount; index++) {
				final ProducerRecord<Long, String> record =new ProducerRecord<>(TOPIC, index, "Hello Mom " + index);
				RecordMetadata metadata = producer.send(record).get();
				long elapsedTime = System.currentTimeMillis() - time;
				System.out.printf("sent record(key=%s value=%s) " + "meta(partition=%d, offset=%d) time=%d\n",record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime);
			}
		} finally {
			producer.flush();
			producer.close();
		}
	}


	public static synchronized Session createConnection(String user, String password, String url) throws Exception {
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user,password,url);
			System.out.println("starting connection");
			// Create a Connection
			ConstantsUtil.connection = connectionFactory.createConnection();
			ConstantsUtil.connection.start();

			// Create a Session
			ConstantsUtil.session = ConstantsUtil.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		} catch(Throwable t) {
			//Log message in report
			throw new Exception(t);
		} finally {
			//Log message in report
		}
		return ConstantsUtil.session;
	}


	/**
	 * Method responsible for close the connections, message producer and sessions
	 */
	public synchronized static void close() {
		try {
			if (ConstantsUtil.connection != null) {
				ConstantsUtil.connection.close();
				//Log message in report
			}
			if (ConstantsUtil.session != null) {
				ConstantsUtil.session.close();
				//Log message in report
			}
			if (ConstantsUtil.messageProducer != null) {
				ConstantsUtil.messageProducer.close();
				//Log message in report
			}
			if (ConstantsUtil.messageConsumer != null) {
				ConstantsUtil.messageConsumer.close();
				//Log message in report
			}
		} catch (Throwable t) {
			//Log message in report
		} finally {
			//Log message in report
		}
	}
	/**
	 * @param correlationId
	 * @param MessageXML
	 * @param RequestQueue
	 * @throws Exception - JMS Exception
	 * Study URL - https://docs.oracle.com/javaee/7/api/javax/jms/MessageProducer.html
	 */
	public synchronized static void publishAMQ(String correlationId,String MessageXML,String RequestQueue) throws Exception {
		try {
			createConnection("User", "password","url");
			Queue queue = ConstantsUtil.session.createQueue(RequestQueue);
			System.out.println("In Queue :"+ RequestQueue);
			Message message = ConstantsUtil.session.createTextMessage(GenericUtil.xmlread(MessageXML));

			message.setJMSCorrelationID(correlationId);
			message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);

			ConstantsUtil.messageProducer = ConstantsUtil.session.createProducer(queue);
			ConstantsUtil.messageProducer.send(message);
		} catch (Throwable t) {
			throw new Exception(t);
		}  finally {
			close();
		}
	}


	public static synchronized String ConsumeFromAMQ(Session amqSession, String correlationId,String ResponseQueue) throws Exception{
		try{
			Queue queue = amqSession.createQueue(ResponseQueue);
			System.out.println("In Queue :"+ ResponseQueue);
			if(ConstantsUtil.messageConsumer==null){
				ConstantsUtil.messageConsumer = amqSession.createConsumer(queue, "JMSCorrelationID='" + correlationId + "'");
			}
			TextMessage textMessage = (TextMessage) ConstantsUtil.messageConsumer.receive(ConstantsUtil.JMS_TIMEOUT);

			String message = (textMessage != null ? textMessage.getText() : null);
			/* if (message==null)
        {
               throw new Exception("<------------- ERROR : No Response from Queue : "+ResponseQueue+" ------------->");
        }*/

			return message;

		} catch (Throwable t) {
			throw new Exception(t);
		}  /*finally {
               close();
        }*/

	}

	public static HttpURLConnection postDataGetConnectionObject(String endpointurl, String JSONPath) {
		HttpURLConnection httpcon = null;
		try {

			URL url = new URL(endpointurl);
			String message =GenericUtil.readJSONFileAndConvertToJSONObject(JSONPath);
			//CucumberBase.scenario.write("Input JSON: "+message+"  Endpoint "+endpointurl);
			//Modify JSON data before posting
			if(message.contains("DynamicCorrelation")){
				ConstantsUtil.correlationId = GenericUtil.generateCorrelationId();
				message = message.replace("DynamicCorrelation", ConstantsUtil.correlationId);
			}
			System.out.println("Message After Replacing Correlation "+message);

			httpcon = (HttpURLConnection) url.openConnection();
			httpcon.setDoOutput(true);
			httpcon.setRequestMethod("POST");
			httpcon.setRequestProperty("Content-Type", "application/json");
			httpcon.setRequestProperty("nbn-correlation-id", ConstantsUtil.correlationId);
			httpcon.setRequestProperty("Authorization", "Basic bXNVc2VyOm1zUFcxMjNxd2U=");
			OutputStreamWriter output = new OutputStreamWriter(
					httpcon.getOutputStream());
			output.write(message);
			output.flush();
			output.close();
			httpcon.connect();

		} catch (Throwable t) {
			t.printStackTrace();
			Assert.assertFalse( "Connection to Microservice is refused", true);
		}
		return httpcon;
	}

	public static  String getHTTPConectionResponseContent(HttpURLConnection httpcon) {
		String serverResponseContent = null;
		try {
			String serverResponseCodeDescription = httpcon.getResponseMessage();
			int serverResponseCode = httpcon.getResponseCode();

			if (serverResponseCode == 200 || serverResponseCode == 202) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(httpcon.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String responseString;
				while ((responseString = br.readLine()) != null) {
					sb.append(responseString);
				}
				serverResponseContent = sb.toString();

				return serverResponseContent;
			} else {
				return serverResponseCodeDescription;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;

		}
	}


	public static String getRequest(String endPoint) {

		StringBuffer response = null;
		//String  url = null;

		try {
			//url = FWConstants.microServiceHost+endPoint+"/"+correlationId;

			URL url = new URL(endPoint);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			//con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			if(responseCode==200 || responseCode==202){

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}else{
				//getTest().log(LogStatus.FAIL, "Get response is not successfull. Server response code is "+responseCode);
			}

		} catch (Throwable t) {
			t.printStackTrace();
			Assert.assertFalse( "No output from the get request", true);
			return "";
			//getTest().log(LogStatus.FATAL, "Unable to get response from server");
			//Assert.assertFalse(true,"Unable to get response from server");
		}
		// print result
//		log.debug(response.toString());
		//getTest().log(LogStatus.INFO, "Get response from Servelet: "+response.toString());
		return ( (response != null )?response.toString(): "");
	}

	public static int getHTTPConectionResponseCode(HttpURLConnection httpcon) {
		int serverResponseCode = 0;
		try {
			serverResponseCode = httpcon.getResponseCode();
		} catch (Throwable t) {
			t.printStackTrace();
			return 0;
		}
		return serverResponseCode;
	}

	//--------------------------jeddis Connection--------------------------------------------------
	public static Jedis getConnection() {
		if (redisPool == null) {
			//log.debug("Getting Connection from Jedis..");
			JedisPoolConfig jedisConfig = new JedisPoolConfig();
			jedisConfig.setMaxTotal(JedisPoolConfig.DEFAULT_MAX_TOTAL);
			redisPool = new JedisPool(jedisConfig,ConstantsUtil.jedishostName, Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT);
		}
		return redisPool.getResource();
	}

}
