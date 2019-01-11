package org.eclipse.om2m.ttn.ipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.ipe.sample.sdt.Activator;
import org.eclipse.om2m.ipe.sample.sdt.controller.LifeCycleManager;
import org.osgi.framework.BundleContext;
public class Activator implements BundleActivator {
	
	private static BundleContext context;
	

	/** Logger */
    private static Log logger = LogFactory.getLog(Activator.class);

    static BundleContext getContext() {
		return context;
	}
 
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		System.out.println("Starting Sample Ipe");
		new ServiceTracker<Object, Object>(bundleContext, CseService.class.getName(), null){
 
			@Override
			public Object addingService(ServiceReference<Object> reference) {
				final CseService cse = (CseService) this.context.getService(reference);
				if(cse != null){
					RequestSender.CSE = cse;
					new Thread(){
						public void run() {
							System.out.println("Test");
						};
					}.start();
				}
				return cse;	
			}
 
			public void removedService(org.osgi.framework.ServiceReference<Object> reference, Object service) {
					
			};
 
		}.open();
 
		bundleContext.registerService(InterworkingService.class.getName(), new Controller(), null);
 
	}
 
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		System.out.println("Stopping Sample Ipe");
	}
 
}
