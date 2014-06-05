package net.izot.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONObject;

public class MessageRouter {
	private final ConcurrentMap<String, IzotConnection> registeredConnections = new ConcurrentHashMap<String, IzotConnection>();

	public void register(String address, IzotConnection connection) {
		registeredConnections.put(address, connection);
	}

	public void unregister(IzotConnection connection) {
		registeredConnections.remove(connection);
	}

	public void routeMessage(JSONObject message) {
		String messageStr = message.toString();
		List<String> closedConnections = new ArrayList<String>();
		for (Entry<String, IzotConnection> pair : registeredConnections
				.entrySet()) {
			if (pair.getValue().isClosed()) {
				closedConnections.add(pair.getKey());
			} else {
				pair.getValue().sendMessage(messageStr);
			}
		}
		for (String address : closedConnections) {
			registeredConnections.remove(address);
		}
	}
}
