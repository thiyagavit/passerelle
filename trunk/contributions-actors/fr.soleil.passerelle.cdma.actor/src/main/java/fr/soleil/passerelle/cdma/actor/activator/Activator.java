package fr.soleil.passerelle.cdma.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import fr.soleil.passerelle.cdma.actor.CDMAArrayFileWriter;
import fr.soleil.passerelle.cdma.actor.CDMAArrayValueDumper;
import fr.soleil.passerelle.cdma.actor.CDMAArrayValueModifier;
import fr.soleil.passerelle.cdma.actor.CDMADataItemSelector;
import fr.soleil.passerelle.cdma.actor.CDMADataSetReader;
import fr.soleil.passerelle.cdma.actor.CDMADataSetSlicer;
import fr.soleil.passerelle.cdma.actor.CDMAFactoryLister;
import fr.soleil.passerelle.cdma.actor.CDMAShapeFilter;
import fr.soleil.passerelle.cdma.actor.CDMASqlQueryActor;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private ServiceRegistration<ModelElementClassProvider> svcReg;
  private BundleActivator testFragmentActivator;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		svcReg = (ServiceRegistration<ModelElementClassProvider>) bundleContext.registerService(ModelElementClassProvider.class, 
		    new DefaultModelElementClassProvider(
		         CDMADataSetReader.class,
		         CDMASqlQueryActor.class,
		         CDMADataItemSelector.class,
		         CDMADataSetSlicer.class,
		         CDMAArrayValueModifier.class,
		         CDMAArrayValueDumper.class,
		         CDMAArrayFileWriter.class,
		         CDMAFactoryLister.class,
		         CDMAShapeFilter.class
		         ), 
		    null);

    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("fr.soleil.passerelle.cdma.actor.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
	}

	public void stop(BundleContext bundleContext) throws Exception {
    if (testFragmentActivator != null) {
      testFragmentActivator.stop(context);
    }
    svcReg.unregister();
		Activator.context = null;
	}
}
