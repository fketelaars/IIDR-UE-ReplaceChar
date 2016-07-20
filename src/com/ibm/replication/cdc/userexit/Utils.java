package com.ibm.replication.cdc.userexit;

import java.util.HashMap;

public class Utils {

	/**
	 * Converts a text input string to its hexadecimal representation. Used for
	 * debugging.
	 * 
	 * @param inputString
	 *            The string to be converted to a hexadecimal representation
	 * @return The hexadecimal representation of the input string
	 */
	static String stringToHex(String inputString) {
		StringBuilder buf = new StringBuilder(200);
		for (char ch : inputString.toCharArray()) {
			if (buf.length() > 0)
				buf.append(' ');
			buf.append(String.format("%02x", (int) ch));
		}
		return buf.toString();
	}

	/**
	 *
	 * Scans the string for the specified characters to be replaced and returns
	 * the string with the characters replaced.
	 * 
	 * @param beforeContent
	 *            The string to be searched for replacement characters.
	 * @param conversionMap
	 *            The conversion map containing all characters to be replaced,
	 *            with their replacement values.
	 * @return The converted string.
	 */
	static String convertString(String beforeContent, HashMap<String, String> conversionMap) {
		String afterContent = beforeContent;
		if (beforeContent != null) {
			for (String searchChar : conversionMap.keySet()) {
				String replaceChar = conversionMap.get(searchChar);
				afterContent = afterContent.replace(searchChar, replaceChar);
			}
		}
		return afterContent;
	}

}
