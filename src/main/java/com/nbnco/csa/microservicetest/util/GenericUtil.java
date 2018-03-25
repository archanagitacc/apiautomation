package com.nbnco.csa.microservicetest.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

public class GenericUtil extends Properties {

	public static String currentEnvironment = null;

	public static String getCurrentEnvironmentName() {

		//----------------Start Eclipse Settings----------------------
		Properties propMainEnvFile = new Properties();
		InputStream inputStreamMain = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/env.properties");
		try {
			propMainEnvFile.load(inputStreamMain);
			currentEnvironment = propMainEnvFile.getProperty("env");
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		//-------------------------End Eclipse Settings-----------------------

		//Below command to execute from command line parameter -Denv=ENV_NAME
		//currentEnvironment =  System.getProperty("env");//A17_VEN_ST_4
		return currentEnvironment;
	}

	public static String getProprty(String key) {
		String config = "config/"+getCurrentEnvironmentName()+".properties";
		//String config = "config/config.properties";
		Properties properties = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
		try {
			properties.load(inputStream);
			inputStream.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		String value = properties.getProperty(key);
		return value;
	}
	public static String getDataFile(String key) {
		String config = "config/datafilepaths.properties";
		//String config = "config/config.properties";
		Properties properties = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
		try {
			properties.load(inputStream);
			inputStream.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		String value = properties.getProperty(key);
		return value;
	}

	public  static String generateCorrelationId()
	{
		return UUID.randomUUID().toString();
	}

	public static String readJSONFileAndConvertToJSONObject(String jsonFile){
		String fileContent = null;
		String filePath = System.getProperty("user.dir")+jsonFile;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			StringBuilder sb = new StringBuilder();
			String responseString;
			while ((responseString = br.readLine()) != null) {
				sb.append(responseString);
			}
			fileContent = sb.toString();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return fileContent;
	}

	public static JSONObject isJSON(String inputString) {

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(inputString);

		} catch (Throwable t) {

			try {
				new JSONArray(inputString);
			} catch (Throwable tt) {
				return null;
			}
		}
		return jsonObject;
	}

	public static String getJSONParameterValue(JSONObject jsonObject, String key){
		String value = null;
		try {
			if(jsonObject.has(key)){
				value = jsonObject.getString(key).toString();
				if(value.equals("null")){
					value = null;
				}
			}else{
				//log.debug(key+" is not present in the JSON");
			}

		} catch (Throwable t) {
			Assert.assertFalse(key+" not found in the JSON object", true);
		}

		return value;
	}

	public static  boolean validateJson(String response, String jsonFName) {
		boolean result =true;
		try {
			if(jsonFName == null || jsonFName == "")
				return true;

			String expectedOutcome =readJSONFileAndConvertToJSONObject(jsonFName);
			System.out.println(expectedOutcome);
			System.out.println(response);
			JSONAssert.assertEquals(expectedOutcome, response, LENIENT);

		} catch (Exception e) {
			//CucumberBase.scenario.write("Exception while validating with JSON validator");
			e.printStackTrace();
		}
		return true;
	}


	public static boolean verifyJSONData(JSONObject actualObj, JSONObject expectedObj, String hierarchyPath) {
		try {

			Iterator<String> iterator = (expectedObj.keys());

			if( hierarchyPath != "" )
				hierarchyPath += ".";

			while (iterator.hasNext()) {
				String name = iterator.next();
				Object actualValue;
				Object expectedValue =  expectedObj.get(name);

				if( expectedValue == null )
					continue;

				if (expectedValue instanceof JSONObject) {
					actualValue =  actualObj.get(name);
					if (! verifyJSONData ((JSONObject) actualValue, (JSONObject) expectedValue , hierarchyPath + name)) {
						return false;
					}
				} else if (expectedValue instanceof JSONArray) {

					if( ((JSONArray)expectedValue).length() > 0 ) {

						if( actualObj.has(name)) {
							actualValue =  actualObj.get(name);
							int expectedListSize = ((JSONArray) expectedValue).length();

							for(int indexArray = 0; indexArray < expectedListSize ; indexArray++) {
								if (! verifyJSONData ( (JSONObject) ((JSONArray)actualValue).get(indexArray),
										(JSONObject) ((JSONArray)expectedValue).get(indexArray) , hierarchyPath + name)
										) {
									return false;
								}
							}
						} else {
							//CucumberFWBase.scenario.write("Key name="+hierarchyPath+name + " does not exist in actual response");
							return false;
						}
					}
				} else {

					if( expectedValue == null || expectedValue == "")
						continue;

					if( actualObj.has(name)) {
						actualValue =  actualObj.getString(name);

						if (!expectedValue.equals(actualValue)) {
							//CucumberFWBase.scenario.write("Key name="+hierarchyPath+name + ", actualValue="+ actualValue + ": expectedValue="+ expectedValue);
							return false;
						}
					} else {
						//CucumberFWBase.scenario.write("Key name="+hierarchyPath+name + " does not exist in actual response" );
						return false;
					}
				}
			}
			return true;
		} catch (Throwable exception) {
			return false;
		}
	}

	/**
	 * Function Desc - Read the given xml file and return xml in string format
	 * @param srcXML
	 * @return xml content in String format
	 */
	public static String xmlread(String srcXML) {
		String xmlInStringFormat = "" ;
		try {
			BufferedReader in
					= new BufferedReader(new FileReader(srcXML));
			StringBuffer output = new StringBuffer();
			String st;
			while ((st=in.readLine()) != null) {
				output.append(st);
			}
			xmlInStringFormat = output.toString();
			System.out.println(output.toString());
			in.close();
		}
		catch (Throwable t) {
			//Log in report
		}
		return xmlInStringFormat;
	}
	public static synchronized void xmlWrite(String xmlSource,String output) throws IOException {
		java.io.FileWriter fw = new java.io.FileWriter(output);
		fw.write(xmlSource);
		System.out.println("Write completed");
		fw.close();
	}


}

