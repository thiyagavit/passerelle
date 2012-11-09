package fr.soleil.passerelle.actor.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tango.utils.DevFailedUtils;

import com.isencia.passerelle.model.Flow;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.device.Device;
import fr.soleil.tangounit.junit.TangoUnitTest;

public class ReadSequence extends TangoUnitTest {

    private Flow topLevel;
    static String deviceName;

    @BeforeClass
    public static void setUp() throws DevFailed, TimeoutException {
        FlowHelperForTests.setProperties(ReadSequence.class);
        final Device d = tangounit.addDevice("TangoTest");
        System.out.println("TEST - create");
        tangounit.create();
        System.out.println("TEST - starting");
        tangounit.start();
        deviceName = d.getProxy().get_name();
        System.out.println("TEST - init done for " + deviceName);
    }

    @Test
    public void testRealAllTypes() {

        DeviceProxy dev = null;
        String[] attrList = null;
        try {
            dev = new DeviceProxy(deviceName);
            attrList = dev.get_attribute_list();
        } catch (final DevFailed e) {
            Assert.fail(DevFailedUtils.toString(e));
        }
        topLevel = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + " reader.moml");

        for (final String element : attrList) {
            if (!element.equals("no_value") && !element.equals("throw_exception")) {
                final Map<String, String> props = new HashMap<String, String>();
                props.put("AttributeReader.Attribute Name", deviceName + "/" + element);
                FlowHelperForTests.setBasicDirector(topLevel, element);
                props.put(element + ".Mock Mode", "false");
                FlowHelperForTests.executeBlockingError(topLevel, props);
            }
        }
    }
}
