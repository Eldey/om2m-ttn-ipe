package org.eclipse.om2m.ttn.ipe.util;

import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.interworking.service.InterworkingService;

public class Controller implements InterworkingService {

	private static String APOCPath = "IPETTN";

	public Controller() {
		System.out.println("Controller created.");
	}
	
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		System.out.println("Yeaaaaah!");
		System.out.println(request.toString());
		String[] parts = request.getTo().split("/");
		String appId = parts[3];
		ResponsePrimitive response = new ResponsePrimitive(request);
		return response;
	}

	@Override
	public String getAPOCPath() {
		return this.APOCPath;
	}

}
