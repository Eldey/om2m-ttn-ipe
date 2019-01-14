package org.eclipse.om2m.ttn.ipe.model;

import org.eclipse.om2m.commons.constants.Constants;

public class TtnApplication {
	
	private String name;
	private String key;
	private int maxMessages = Constants.MAX_NBR_OF_INSTANCES.intValue();
	
	public TtnApplication(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public int getMaxMessages() {
		return maxMessages;
	}

	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}
	
}
