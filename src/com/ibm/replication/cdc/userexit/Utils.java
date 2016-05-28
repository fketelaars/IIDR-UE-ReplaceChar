package com.ibm.replication.cdc.userexit;

public class Utils {

	static String stringToHex(String string) {
		StringBuilder buf = new StringBuilder(200);
		for (char ch : string.toCharArray()) {
			if (buf.length() > 0)
				buf.append(' ');
			buf.append(String.format("%02x", (int) ch));
		}
		return buf.toString();
	}

}
