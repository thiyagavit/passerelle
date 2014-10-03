package fr.soleil.passerelle.actor.acquisition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.tango.utils.DevFailedUtils;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.device.Device;
import fr.soleil.tangounit.junit.TangoUnitTest;

@RunWith(Parameterized.class)
public class CCDSequence extends TangoUnitTest {

    @Parameters
    public static List<Object[]> getParametres() {

        return Arrays.asList(new Object[][] { { "DirTango", false }, { "DirMock", true } });
    }

    private final String dirName;
    static String deviceName;
    private final boolean mockMode;
    private Flow topLevel;
    Map<String, String> parameters = new HashMap<String, String>();

    public CCDSequence(final String dirName, final boolean mockMode) {
        this.dirName = dirName;
        this.mockMode = mockMode;
    }

    @BeforeClass
    public static void setUp() {
        FlowHelperForTests.setProperties(CCDSequence.class);
        try {

            final Device d = tangounit.addDevice("Publisher");

            System.out.println("adding property on " + d.getName());
            tangounit.addDeviceProperties(d.getName(), "AttributesList",
                    "useROI;DEVBOOLEAN;SCALAR", "roi1xmin;DEVSHORT;SCALAR",
                    "roi1xmax;DEVSHORT;SCALAR", "roi1ymin;DEVSHORT;SCALAR",
                    "roi1ymax;DEVSHORT;SCALAR", "xbin;DEVSHORT;SCALAR", "ybin;DEVSHORT;SCALAR",
                    "frames;DEVSHORT;SCALAR", "trigger;DEVSHORT;SCALAR", "acqMode;DEVSHORT;SCALAR",
                    "exposure;DEVDOUBLE;SCALAR", "timing;DEVDOUBLE;SCALAR");

            tangounit.addDeviceProperties(d.getName(), "CommandsList", "Start;DevVoid;DevVoid",
                    "Stop;DevVoid;DevVoid", "SetAverageIntensity0;DevVoid;DevVoid");

            System.out.println("TEST - create");
            tangounit.create();
            System.out.println("TEST - starting");
            tangounit.start();
            deviceName = d.getProxy().get_name();
            System.out.println("TEST - init done for " + deviceName);

        } catch (final DevFailed e) {
            // e.printStackTrace();
            Except.print_exception(e);
            // Assert.fail(TangoUtil.getDevFailedString(e));
            Assert.fail(DevFailedUtils.toString(e));
        } catch (final TimeoutException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(timeout = 20000)
    public void testNormal() {
        topLevel = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + "ccd.moml");
        FlowHelperForTests.setBasicDirector(topLevel, dirName);
        parameters.put(dirName + ".Mock Mode", Boolean.toString(mockMode));
        parameters.put("ConfigurationAviex.Exposure Time (ms)", "250");
        parameters.put("ConfigurationPrincetonOde.Exposure Time (ms)", "600");
        parameters.put("ConfigurationPrincetonOde.Device Name", deviceName);
        parameters.put("ConfigurationAviex.Device Name", deviceName);
        parameters.put("AcquisitionAviexSwing.Device Name", deviceName);
        parameters.put("AcquisitionPrincetonOde.Device Name", deviceName);
        FlowHelperForTests.executeBlockingError(topLevel, parameters);
    }

    @Test(timeout = 20000)
    public void testStop() {
        topLevel = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + "ccd.moml");
        FlowHelperForTests.setBasicDirector(topLevel, dirName);
        parameters.put(dirName + ".Mock Mode", Boolean.toString(mockMode));
        parameters.put("ConfigurationAviex.Exposure Time (ms)", "250");
        parameters.put("ConfigurationPrincetonOde.Exposure Time (ms)", "600");
        parameters.put("ConfigurationPrincetonOde.Device Name", deviceName);
        parameters.put("ConfigurationAviex.Device Name", deviceName);
        parameters.put("AcquisitionAviexSwing.Device Name", deviceName);
        parameters.put("AcquisitionPrincetonOde.Device Name", deviceName);
        final FlowManager flowMgr = FlowHelperForTests.executeNonBlocking(topLevel, parameters);
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FlowHelperForTests.stopExecution(flowMgr, topLevel);
    }
}
