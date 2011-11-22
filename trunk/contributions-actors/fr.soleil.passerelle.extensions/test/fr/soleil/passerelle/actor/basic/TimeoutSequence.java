package fr.soleil.passerelle.actor.basic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;

import be.isencia.passerelle.core.PasserelleException;
import be.isencia.passerelle.model.Flow;
import be.isencia.passerelle.model.FlowManager;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

public class TimeoutSequence {

    @AfterClass
    public static void setStandardTimeout() throws DevFailed {
        ProxyFactory.getInstance().setTimout(3000);
    }

    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;

    private Flow topLevel;

    @Test(expected = PasserelleException.class)
    public void testError() throws Exception {

        // String deviceName = "tango/tangotest/1";
        final String deviceName = "sys/database/dbds1";
        // sys/database/dbds1/DbGetDeviceExportedList
        final DeviceProxy dev = new DeviceProxy(deviceName);

        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/timeout.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", "*");
        props.put("SetTimeout.Timeout", "1");
        props.put("CommandInOut.Device Name", deviceName);
        props.put("CommandInOut.Command Name", "DbGetDeviceExportedList");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

    }
}
