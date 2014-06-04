package net.izot.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

public class LedControllerDataParser {
	private static final List<String> keys = new ArrayList<>();

	public static void buildKeys(String keyString) {
		keys.clear();
		StringTokenizer tokenizer = new StringTokenizer(keyString, ",");
		while (tokenizer.hasMoreTokens()) {
			keys.add(tokenizer.nextToken().trim());
		}
	}

	public static JSONObject toJson(String ledData) {
		if (keys.isEmpty()) {
			throw new RuntimeException("No LED controller data keys available");
		}
		StringTokenizer tokenizer = new StringTokenizer(ledData, ",");

		JSONObject jsonObj = new JSONObject();
		int index = 0;
		while (tokenizer.hasMoreTokens() && (index < keys.size())) {
			String key = keys.get(index++);
			String value = tokenizer.nextToken().trim();
	        try {
				jsonObj.put(key, value);
			} catch (JSONException e) {
				throw new RuntimeException("Malformed LED data", e);
			}
		}
		return jsonObj;
	}
}
