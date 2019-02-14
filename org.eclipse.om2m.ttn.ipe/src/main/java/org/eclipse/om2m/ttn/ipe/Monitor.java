package org.eclipse.om2m.ttn.ipe;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.ttn.ipe.model.TtnApplication;
import org.eclipse.om2m.ttn.ipe.model.TtnBroker;
import org.eclipse.om2m.ttn.ipe.util.RequestSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Monitor {

	private static final Log LOGGER = LogFactory.getLog(Activator.class);

	private CseService cse;
	private RequestSender requestSender = new RequestSender();

	private TtnBroker broker = new TtnBroker();
	private Map<String, TtnMqttClient> clientMap = new HashMap<>();

	public void setCse(CseService cse) {
		this.cse = cse;
		this.requestSender.setCseService(this.cse);
	}

	private void loadConfiguration() {
		String configFilePath = System.getProperty("org.eclipse.om2m.ipe.ttn.config", "ttn_config.json");
		JSONParser parser = new JSONParser();

		try {
			JSONObject configObject = (JSONObject) parser.parse(new FileReader(configFilePath));
			if (configObject.containsKey("broker")) {
				JSONObject broker = (JSONObject) configObject.get("broker");
				if (broker.containsKey("host"))
					this.broker.setHost((String) broker.get("host"));
				if (broker.containsKey("port"))
					this.broker.setPort(((Long) broker.get("port")).intValue());
				if (broker.containsKey("uplink"))
					this.broker.setUplinkTopic((String) broker.get("uplink"));
				if (broker.containsKey("downlink"))
					this.broker.setDownlinkTopic((String) broker.get("downlink"));
				if (broker.containsKey("events"))
					this.broker.setDownlinkTopic((String) broker.get("events"));
			}

			if (configObject.containsKey("applications")) {
				JSONArray applications = (JSONArray) configObject.get("applications");
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> appIterator = applications.iterator();
				while (appIterator.hasNext()) {
					JSONObject applicationObject = appIterator.next();
					TtnApplication application = new TtnApplication((String) applicationObject.get("name"),
							(String) applicationObject.get("key"));
					if (applicationObject.containsKey("max_message")) {
						application.setMaxMessages((Integer) applicationObject.get("max_message"));
					}

					TtnMqttClient ttnClient = new TtnMqttClient(application, this.broker, requestSender);
					clientMap.put(application.getName(), ttnClient);
				}
			}

		} catch (FileNotFoundException e) {
			LOGGER.error("No configuration file for TTN IPE at: " + configFilePath, e);
			throw new IllegalArgumentException("No configuration file for TTN IPE at: " + configFilePath, e);
		} catch (ParseException | IOException e) {
			LOGGER.error("Error on parsing configuration file.", e);
			throw new RuntimeException("Error on parsing configuration file.", e);
		}
	}

	public void start() {
		loadConfiguration();

		// Start a MQTT client for each TTN application
		for (Entry<String, TtnMqttClient> entries : clientMap.entrySet()) {
			entries.getValue().initAndConnect();
		}
	}

	public void stop() {
		// TODO Stop the mqtt clients
	}

}
