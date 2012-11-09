package fr.soleil.passerelle.actor.tango.control;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.device.Device;
import fr.soleil.tangounit.junit.TangoUnitTest;

/**
 * @author ABEILLE
 * 
 */
@RunWith(Parameterized.class)
public class MotorSequence extends TangoUnitTest {

    @Parameters
    public static List<Object[]> getParametres() {

        return Arrays.asList(new Object[][] { { "DirTango", false }, { "DirMock", true } });
    }

    private static Flow topLevel;
    private final String dirName;
    private final boolean mockMode;
    static String deviceName;

    public MotorSequence(final String dirName, final boolean mockMode) {
        this.dirName = dirName;
        this.mockMode = mockMode;
    }

    @BeforeClass
    public static void setUp() throws DevFailed, TimeoutException {
        FlowHelperForTests.setProperties(MotorSequence.class);

        final Device d = tangounit.addDevice("Publisher");

        System.out.println("adding property on " + d.getName());
        tangounit.addDeviceProperties(d.getName(), "AttributesList", "position;DEVDOUBLE;SCALAR");

        System.out.println("TEST - create");
        tangounit.create();
        System.out.println("TEST - starting");
        tangounit.start();
        deviceName = d.getProxy().get_name();
        System.out.println("TEST - init done for " + deviceName);

    }

    @Test
    public void testBasic() throws Exception {
        topLevel = FlowHelperForTests.loadMoml(MotorSequence.class, Constants.SEQUENCES_PATH
                + " motor.moml");
        FlowHelperForTests.setBasicDirector(topLevel, dirName);

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("DirBasic.Mock Mode", Boolean.toString(mockMode));
        parameters.put("Constant.value", "10");
        parameters.put("MotorMover.Device Name", deviceName);
        FlowHelperForTests.executeBlockingError(topLevel, parameters);
    }

    @Test
    public void testRealStop() throws Exception {
        topLevel = FlowHelperForTests.loadMoml(MotorSequence.class, Constants.SEQUENCES_PATH
                + " motor.moml");
        FlowHelperForTests.setBasicDirector(topLevel, dirName);

        // init pos
        Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", Boolean.toString(mockMode));
        props.put("Constant.value", "0");
        props.put("MotorMover.Device Name", deviceName);
        FlowHelperForTests.executeBlockingError(topLevel, props);

        // test stop
        props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", Boolean.toString(mockMode));
        props.put("Constant.value", "1000");
        props.put("MotorMover.Device Name", deviceName);
        final FlowManager manager = FlowHelperForTests.executeNonBlocking(topLevel, props);
        // Thread.sleep(10);
        System.out.println("=== STOP motor ====");
        FlowHelperForTests.stopExecution(manager, topLevel);
        final DeviceProxy dev = new DeviceProxy(deviceName);
        Assert.assertNotSame(DevState.MOVING, dev.state());
    }
}
