/****************************************************************************
 ** Licensed Materials - Property of IBM 
 ** IBM InfoSphere Change Data Capture
 ** 5724-U70
 ** 
 ** (c) Copyright IBM Corp. 2001, 2016 All rights reserved.
 ** 
 ** The following sample of source code ("Sample") is owned by International 
 ** Business Machines Corporation or one of its subsidiaries ("IBM") and is 
 ** copyrighted and licensed, not sold. You may use, copy, modify, and 
 ** distribute the Sample in any form without payment to IBM.
 ** 
 ** The Sample code is provided to you on an "AS IS" basis, without warranty of 
 ** any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR 
 ** IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 ** MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do 
 ** not allow for the exclusion or limitation of implied warranties, so the above 
 ** limitations or exclusions may not apply to you. IBM shall not be liable for 
 ** any damages you suffer as a result of using, copying, modifying or 
 ** distributing the Sample, even if IBM has been advised of the possibility of 
 ** such damages. */

package com.ibm.replication.cdc.userexit;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

// Singleton class
public class UESettings {

	// There can only be one object of UESettings, this prevents the properties
	// file from being opened for every table in the subscription.
	private static UESettings instance;

	private Properties properties;
	private final String propertiesFileName = "UEReplaceChar.properties";

	// Properties to be retrieved from the file
	private String replaceCharacters = "";
	public boolean debug = false;
	public HashMap<String, String> conversionMap = new HashMap<String, String>();

	UETrace ueTrace = new UETrace();

	long instanceTime = 0;

	// Constructor
	private UESettings() {
		instanceTime = System.currentTimeMillis();
		loadConfiguration(propertiesFileName);
		loadConversionMap();
	}

	// Get the UESettings objects, or instantiate it if is doesn't exist yet or
	// it has been loaded more than 10 seconds ago
	public static synchronized UESettings getInstance() {
		if (instance == null) {
			UETrace.writeAlwaysStatic("Settings not instantiated yet, they will be loaded");
			instance = new UESettings();
		}
		// If it has been longer than 10 seconds since the settings have been
		// loaded the subscription may have been restarted, reload the settings
		long currentTime = System.currentTimeMillis();
		if (currentTime - instance.instanceTime > 10000) {
			UETrace.writeAlwaysStatic("Settings have been loaded more than 10 seconds ago, they will be reloaded");
			instance = new UESettings();
		}
		return instance;
	}

	private void loadConfiguration(String fileName) {
		ueTrace.writeAlways("Reading configuration from properties file " + fileName);
		properties = new Properties();
		try {
			URL fileURL = UESettings.class.getClassLoader().getResource(fileName);
			ueTrace.writeAlways("Resolved properties file: " + fileURL);
			InputStream stream = UESettings.class.getClassLoader().getResourceAsStream(fileName);
			properties.load(stream);
			// Log all properties into the trace
			Set<Object> propertiesKeys = properties.keySet();
			for (Object key : propertiesKeys) {
				ueTrace.writeAlways(key + "=" + properties.getProperty((String) key));
			}
		} catch (Exception e) {
			ueTrace.writeAlways("Error processing properties from file " + fileName + ", message: " + e.getMessage());
		}
		// Set the keys
		replaceCharacters = properties.getProperty("replaceCharacters", replaceCharacters);
		debug = Boolean.parseBoolean(properties.getProperty("debug", Boolean.toString(debug)));
	}

	// Load the character conversion map
	private void loadConversionMap() {

		String[] replaceArray = replaceCharacters.split(",");
		for (int i = 0; i < replaceArray.length; i++) {
			String[] replaceElement = replaceArray[i].split(":");
			if (replaceElement.length == 2) {
				ueTrace.writeAlways("Character " + replaceElement[0] + " (" + Utils.stringToHex(replaceElement[0])
						+ ") will be replaced by " + replaceElement[1] + " (" + Utils.stringToHex(replaceElement[1])
						+ ")");
				conversionMap.put(replaceElement[0], replaceElement[1]);
			} else
				ueTrace.writeAlways(
						"Invalid conversion element (" + replaceElement[i] + "), must be specified as <from>:<to>");
		}
		ueTrace.writeAlways("Number of elements in the conversion map: " + conversionMap.size());

	}

	// Clean up the settings so that they are reloaded next time
	public static synchronized void resetInstance() {
		instance = null;
	}

	// Main method, just for validating this class
	public static void main(String[] args) {
		new UESettings();
	}
}