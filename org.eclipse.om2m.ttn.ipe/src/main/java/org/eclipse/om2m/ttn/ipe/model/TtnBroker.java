package org.eclipse.om2m.ttn.ipe.model;

public class TtnBroker {

	private String host = "eu.thethings.network";
	private int port = 1883;
	private String uplinkTopic = "+/devices/+/up";
	private String downlinkTopic = "+/devices/down";
	private String eventsTopic = "+/devices/+/events/+";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUplinkTopic() {
		return uplinkTopic;
	}

	public void setUplinkTopic(String uplinkTopic) {
		this.uplinkTopic = uplinkTopic;
	}

	public void setEventsTopic(String eventsTopic) {
		this.eventsTopic = eventsTopic;
	}

	public String getEventsTopic() {
		return eventsTopic;
	}

	public String getDownlinkTopic() {
		return downlinkTopic;
	}

	public void setDownlinkTopic(String downlinkTopic) {
		this.downlinkTopic = downlinkTopic;
	}

	public String getServerURI() {
		return "tcp://" + this.host + ":" + this.port;
	}

}
