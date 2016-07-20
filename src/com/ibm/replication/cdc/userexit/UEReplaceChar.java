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

import java.util.ArrayList;
import java.util.Arrays;

import com.datamirror.ts.target.publication.userexit.*;

public class UEReplaceChar implements UserExitIF {

	private UETrace ueTrace = new UETrace();
	private UESettings ueSettings = UESettings.getInstance();

	boolean firstTime = true;

	private ArrayList<String> columnsToConvert = new ArrayList<String>();

	@Override
	public void init(ReplicationEventPublisherIF eventPublisher) throws UserExitException {
		// Set tracing level
		ueTrace.init(ueSettings.debug);

		// Parse the parameter that indicates the columns to be converted
		String parameter = eventPublisher.getParameter();
		if (parameter != null && !parameter.isEmpty()) {
			columnsToConvert = new ArrayList<String>(Arrays.asList(parameter.split(",")));
			ueTrace.write("Columns that will be converted: " + columnsToConvert);
			eventPublisher.logEvent("Columns that will be converted: " + columnsToConvert);
		}
		if (columnsToConvert.isEmpty())
			eventPublisher
					.logEvent("Warning: No columns to convert specified in the parameter, no conversion will be done.");

		// Ensure that the user exit is only called before insert, update,
		// delete
		eventPublisher.unsubscribeEvent(ReplicationEventTypes.ALL_EVENTS);
		eventPublisher.subscribeEvent(ReplicationEventTypes.BEFORE_INSERT_EVENT);
		eventPublisher.subscribeEvent(ReplicationEventTypes.BEFORE_UPDATE_EVENT);
		eventPublisher.subscribeEvent(ReplicationEventTypes.BEFORE_DELETE_EVENT);

	}

	@Override
	public boolean processReplicationEvent(ReplicationEventIF event) throws UserExitException {
		// Get event type (insert, update, delete) and the appropriate data
		// records
		int eventType = event.getEventType();
		DataRecordIF targetAfterImage = event.getData();
		DataRecordIF targetBeforeImage = event.getBeforeData();

		// Check if columns exist. If not, remove from list
		if (firstTime) {
			DataRecordIF image = targetAfterImage;
			if (image == null)
				image = targetBeforeImage;
			for (String column : columnsToConvert) {
				try {
					String nativeType = image.getColumnNativeType(column);
					ueTrace.write("Column " + column + " has type " + nativeType);
				} catch (Exception e) {
					columnsToConvert.remove(column);
					event.logEvent("Warning: Specified column " + column + " not found, will be ignored.");
				}
			}
			firstTime = false;
		}

		// Process all specified columns and convert the column in the before or
		// after image, dependent on the type of operation
		for (String column : columnsToConvert) {
			if (eventType == ReplicationEventTypes.BEFORE_DELETE_EVENT
					|| event.getEventType() == ReplicationEventTypes.BEFORE_UPDATE_EVENT) {
				convertColumn(targetBeforeImage, column);
			}
			if (eventType == ReplicationEventTypes.BEFORE_INSERT_EVENT
					|| event.getEventType() == ReplicationEventTypes.BEFORE_UPDATE_EVENT) {
				convertColumn(targetAfterImage, column);
			}
		}
		// Proceed with apply
		return true;
	}

	private void convertColumn(DataRecordIF dataRecord, String column) {
		try {
			String beforeContent = dataRecord.getString(column);
			String afterContent = Utils.convertString(beforeContent, ueSettings.conversionMap);
			// If debugging is on, report
			if (ueSettings.debug) {
				if (!afterContent.equals(beforeContent))
					ueTrace.write("Column " + column + " content " + beforeContent + " ("
							+ Utils.stringToHex(beforeContent) + ") " + " converted to " + afterContent + " ("
							+ Utils.stringToHex(afterContent) + ")");
			}
			dataRecord.setString(column, afterContent);
		} catch (DataTypeConversionException e) {
			ueTrace.writeAlways("Error while converting before-content of column " + column + ": " + e.getMessage());
		} catch (InvalidSetDataException e) {
			ueTrace.writeAlways(
					"Error while converting setting resulting string of column " + column + ": " + e.getMessage());
		}
	}

	@Override
	public void finish() throws UserExitException {
		// Ensure that settings are reloaded next time
		UESettings.resetInstance();
	}

}
