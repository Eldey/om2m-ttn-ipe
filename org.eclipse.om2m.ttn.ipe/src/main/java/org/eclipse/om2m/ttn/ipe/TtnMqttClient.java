package org.eclipse.om2m.ttn.ipe;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.ttn.ipe.model.TtnApplication;
import org.eclipse.om2m.ttn.ipe.model.TtnBroker;
import org.eclipse.om2m.ttn.ipe.util.RequestSender;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TtnMqttClient implements MqttCallback {

	private static final Log LOGGER = LogFactory.getLog(TtnMqttClient.class);

	private TtnApplication application;
	private TtnBroker broker;
	private int maxMessage = Constants.MAX_NBR_OF_INSTANCES.intValue();
	private MqttClient client;
	private RequestSender requestSender;
	private JSONParser parser = new JSONParser();

	private String csePrefix = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;

	public TtnMqttClient(TtnApplication application, TtnBroker broker, RequestSender requestSender) {
		this.application = application;
		this.broker = broker;
		this.requestSender = requestSender;
	}

	public TtnApplication getApplication() {
		return application;
	}

	public TtnBroker getBroker() {
		return broker;
	}

	public int getMaxMessage() {
		return maxMessage;
	}

	public void setMaxMessage(int maxMessage) {
		this.maxMessage = maxMessage;
	}

	public void initAndConnect() {
		// Check if the AE already exists
		ResponsePrimitive response = requestSender.getRequest(this.csePrefix + "/" + "TTN_" + application.getName());
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			// Create the AE if it is not present
			AE ae = new AE();
			ae.setName("TTN_" + application.getName());
			ae.setRequestReachability(false);
			ae.setAppID("TTN_" + application.getName());
			requestSender.createAE(ae);
		}

		try {
			client = new MqttClient(broker.getServerURI(), "oneM2M-" + application.getName());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(application.getName());
			options.setPassword(application.getKey().toCharArray());
			client.connect(options);

			client.setCallback(this);

			// Subscribe to uplink topic
			client.subscribe(this.broker.getUplinkTopic());

		} catch (MqttException e) {
			throw new RuntimeException("Error on MQTT execution.", e);
		}
	}

	@Override
	public void connectionLost(Throwable exception) {
		LOGGER.error("Connection lost.", exception);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Empty
	}

	private void checkAndCreateContainers(String deviceId) {
		String aePrefix = this.csePrefix + "/" + "TTN_" + application.getName();
		ResponsePrimitive response = requestSender
				.getRequest(aePrefix + "/" + deviceId);
		if(!response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			Container deviceContainer = new Container();
			deviceContainer.setName(deviceId);
			requestSender.createContainer(aePrefix, deviceContainer);
			
			Container uplinkContainer = new Container();
			uplinkContainer.setName("uplink");
			uplinkContainer.setMaxNrOfInstances(BigInteger.valueOf(this.application.getMaxMessages()));
			requestSender.createContainer(aePrefix + "/" + deviceId, uplinkContainer);
			
			Container downlinkContainer = new Container();
			downlinkContainer.setName("downlink");
			downlinkContainer.setMaxNrOfInstances(BigInteger.valueOf(this.application.getMaxMessages()));
			requestSender.createContainer(aePrefix + "/" + deviceId, downlinkContainer);
			
			// TODO Create subscription to downlink container
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String payload = new String(message.getPayload());
		JSONObject jsonPayload = (JSONObject) parser.parse(payload);
		String deviceId = (String) jsonPayload.get("dev_id");
		checkAndCreateContainers(deviceId);
		
		ContentInstance contentInstance = new ContentInstance();
		contentInstance.setContent(payload);
		
		String aePrefix = this.csePrefix + "/" + "TTN_" + application.getName();
		String uplinkPrefix = aePrefix + "/" + deviceId + "/uplink";
		requestSender.createContentInstance(uplinkPrefix, contentInstance);
	}

}
