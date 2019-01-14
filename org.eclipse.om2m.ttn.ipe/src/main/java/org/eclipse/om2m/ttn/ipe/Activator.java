package org.eclipse.om2m.ttn.ipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	private static final Log LOGGER = LogFactory.getLog(Activator.class);
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private Monitor monitor;

	private ServiceTracker<CseService, CseService> cseServiceTracker;
	private ServiceTrackerCustomizer<CseService, CseService> cseServiceTrackerCustomizer = new ServiceTrackerCustomizer<CseService, CseService>() {

		@Override
		public void removedService(ServiceReference<CseService> reference, CseService service) {
			monitor.setCse(null);
		}

		@Override
		public void modifiedService(ServiceReference<CseService> reference, CseService service) {
			monitor.setCse(service);
		}

		@Override
		public CseService addingService(ServiceReference<CseService> reference) {
			monitor.setCse(context.getService(reference));
			// Starts the monitor when the CSE service is available
			monitor.start();
			return context.getService(reference);
		}
	};

	public void start(BundleContext bundleContext) throws Exception {
		try {
			Activator.context = bundleContext;
			LOGGER.info("Starting TTN IPE");
			monitor = new Monitor();
			cseServiceTracker = new ServiceTracker<>(bundleContext, CseService.class, cseServiceTrackerCustomizer);
			cseServiceTracker.open();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		cseServiceTracker.close();
		cseServiceTracker = null;
		monitor.stop();
	}

}
