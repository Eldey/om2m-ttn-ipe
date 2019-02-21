package org.eclipse.om2m.ttn.ipe.util;


import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.Notification;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.datamapping.service.DataMapperService;
import org.eclipse.om2m.interworking.service.InterworkingService;

public class Controller implements InterworkingService {

	public static String APOCPath = "IPETTN";

	private DataMapperService dms;
	
	public Controller() {
		System.out.println("Controller created.");
	}
	
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		try {
			System.out.println("Yeaaaaah!");
			System.out.println(request.toString());
			ResponsePrimitive response = new ResponsePrimitive(request);
			if(request.getContent() != null && request.getContent() instanceof String && request.getRequestContentType().equals(MimeMediaType.JSON)) {
				Notification notification = (Notification) dms.stringToObj((String)request.getContent());
				
				if(notification.isVerificationRequest() != null && notification.isVerificationRequest()) {
					System.out.println("Verification request, returning OK.");
					response.setResponseStatusCode(ResponseStatusCode.OK);
					return response; 
				}
				
				Resource res = notification.getNotificationEvent().getRepresentation().getResource();
				System.out.println("After retrieving the resource from notification event and representation");
				if (res instanceof ContentInstance) {
					ContentInstance cin = (ContentInstance) res;
					System.out.println("Content: " + cin.getContent());
				} 
//			String[] parts = request.getTo().split("/");
//			String appId = parts[3];
			}
			response.setResponseStatusCode(ResponseStatusCode.OK);
			return response;			
		} catch (Exception e) {
			throw new RuntimeException("Error in TTN IPE Controller", e);
		}
	}

	@Override
	public String getAPOCPath() {
		return APOCPath;
	}

	public void setDataMapperService(DataMapperService service) {
		this.dms = service;
	}

}
