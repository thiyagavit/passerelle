package fr.soleil.passerelle.actor.basic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.acquisition.CCDSequence;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.client.TangoUnitClient;
import fr.soleil.tangounit.client.TangoUnitFactory;
import fr.soleil.tangounit.client.TangoUnitFactory.MODE;
import fr.soleil.tangounit.device.Device;

public class CommandSequence {
    private static TangoUnitClient client;
    private static String deviceName;
    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;

    private Flow topLevel;

    @BeforeClass
    public static void setUp() throws DevFailed, TimeoutException {
        FlowHelperForTests.setProperties(CCDSequence.class);

        client = TangoUnitFactory.instance().createTangoUnitClient(MODE.remote);

        final Device d = client.addDevice("TangoTest");
        System.out.println("create");
        client.create();
        System.out.println("starting");
        client.start();
        deviceName = d.getProxy().get_name();
        System.out.println("init done for " + deviceName);
    }

    @Test(expectedExceptions = PasserelleException.class)
    public void testError() throws Exception {

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "command.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        // props.put("DirBasic.Error Control", "retry");
        props.put("Constant.value", "toto");
        props.put("CommandInOut.Device Name", deviceName);
        props.put("CommandInOut.Command Name", "DevLong");
        System.out.println("Command DevLong");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

    }

    @Test
    public void testRealAllTypes() throws Exception {

        final DeviceProxy dev = new DeviceProxy(deviceName);
        final CommandInfo[] cmds = dev.command_list_query();

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "command.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        for (final CommandInfo cmd : cmds) {
            final Map<String, String> props = new HashMap<String, String>();
            props.put("DirBasic.Mock Mode", "false");
            props.put("Constant.value", "1");
            props.put("CommandInOut.Device Name", deviceName);
            props.put("CommandInOut.Command Name", cmd.cmd_name);
            System.out.println("Command " + cmd.cmd_name);
            try {
                flowMgr.executeBlockingErrorLocally(topLevel, props);
            } catch (final Exception e) {

            }
        }
    }
}
