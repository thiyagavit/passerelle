package fr.soleil.passerelle.actor.basic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.acquisition.CCDSequence;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.device.Device;
import fr.soleil.tangounit.junit.TangoUnitTest;

public class GroupSequence extends TangoUnitTest {

    private static String deviceName;

    @BeforeClass
    public static void setUp() throws DevFailed, TimeoutException {
        FlowHelperForTests.setProperties(CCDSequence.class);

        final Device d = tangounit.addDevice("TangoTest");
        System.out.println("create");
        tangounit.create();
        System.out.println("starting");
        tangounit.start();
        deviceName = d.getProxy().get_name();
        System.out.println("init done for " + deviceName);

    }

    private BasicDirector dir;
    private FlowManager flowMgr;

    private Reader in;

    private Flow topLevel;

    @Test
    public void test() throws Exception {

        // String deviceName = "tango/tangotest/1";
        // DeviceProxy dev = new DeviceProxy(deviceName);

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "groups.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("GroupedAttributeWriter.Attribute Names", deviceName + "/long_scalar,"
                + deviceName + "/double_scalar");
        props.put("GroupedCommand.Device List", deviceName);
        props.put("GroupedCommand.Command Name", "SwitchStates");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

    }
}
