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

import com.datamirror.ts.derivedexpressionmanager.DEUserExitIF;
import com.datamirror.ts.derivedexpressionmanager.UserExitInvalidArgumentException;
import com.datamirror.ts.derivedexpressionmanager.UserExitInvokeException;

public class UEReplaceCharDE implements DEUserExitIF {

	private UETrace ueTrace = new UETrace();
	private UESettings ueSettings = UESettings.getInstance();

	boolean firstTime = true;

	public UEReplaceCharDE() {
		// Set tracing level
		ueTrace.init(ueSettings.debug);

	}

	@Override
	public Object invoke(Object[] aObjList) throws UserExitInvalidArgumentException, UserExitInvokeException {
		// If first time, check if correct number of parameters is specified
		if (firstTime) {
			if (aObjList.length == 0) {
				throw new UserExitInvalidArgumentException(
						this.getClass().getSimpleName() + ": Incorrect number of parameters, 1 parameter expected");
			}
		}
		// Now convert the contents
		String beforeContent = (String) aObjList[0];
		String afterContent = Utils.convertString(beforeContent, ueSettings.conversionMap);
		// If debugging is on, report
		if (ueSettings.debug) {
			if (!afterContent.equals(beforeContent))
				ueTrace.write("String " + beforeContent + " (" + Utils.stringToHex(beforeContent) + ") "
						+ " converted to " + afterContent + " (" + Utils.stringToHex(afterContent) + ")");
		}
		return (Object) afterContent;
	}

}
