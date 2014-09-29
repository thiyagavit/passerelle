package fr.soleil.passerelle.actor.basic;

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.isencia.passerelle.model.Flow;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.junit.TangoUnitTest;

public class WriteSequence extends TangoUnitTest {

    static String deviceName = "tango/tangotest/1";

    @BeforeClass
    public static void setUp() {
        FlowHelperForTests.setProperties(WriteSequence.class);
        // try {
        //
        // // final Device d = tangounit.addDevice("TangoTest");
        // //
        // // System.out.println("TEST - create");
        // // tangounit.create();
        // // System.out.println("TEST - starting");
        // // tangounit.start();
        // // deviceName = d.getProxy().get_name();
        // // System.out.println("TEST - init done for " + deviceName);
        //
        // } catch (final DevFailed e) {
        // // e.printStackTrace();
        // Except.print_exception(e);
        // Assert.fail(DevFailedUtils.toString(e));
        // } catch (final TimeoutException e) {
        // e.printStackTrace();
        // Assert.fail();
        // }
    }

    private Flow topLevel;

    @Test
    public void testRealAllTypes() {
        try {

            final DeviceProxy dev = new DeviceProxy(deviceName);

            final String[] attrList = dev.get_attribute_list();
            topLevel = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                    + "writer.moml");

            FlowHelperForTests.setBasicDirector(topLevel, "DirBasic");

            // in = new InputStreamReader(getClass().getResourceAsStream(
            // "/fr/soleil/passerelle/resources/writer.moml"));
            // flowMgr = new FlowManager();
            // topLevel = FlowManager.readMoml(in);
            // dir = new BasicDirector(topLevel, "DirBasic");
            // topLevel.setDirector(dir);

            for (final String element : attrList) {
                final AttrWriteType writeType = dev.get_attribute_info(element).writable;
                final AttrDataFormat format = dev.get_attribute_info(element).data_format;
                // image format is not supported by actor
                if (format != AttrDataFormat.IMAGE
                        && (writeType == AttrWriteType.READ_WRITE || writeType == AttrWriteType.WRITE)) {
                    // Ajouter un cas boolean
                    final Map<String, String> props = new HashMap<String, String>();
                    props.put("DirBasic.Mock Mode", "false");
                    props.put("Constant.value", "1");
                    // bug on device tango test :write on uchar_spectrum never
                    // works
                    // if (element.compareTo("uchar_scalar") != 0
                    // && element.compareTo("uchar_spectrum") != 0
                    // && element.compareTo("string_spectrum") != 0) {
                    try {
                        props.put("AttributeWriter.Attribute Name", deviceName + "/" + element);
                        FlowHelperForTests.executeBlockingError(topLevel, props);
                    } catch (final Exception e) {
                        AssertJUnit.fail("error"/* DevFailedUtils.toString(e) */);
                    }
                    // }

                }
            }
        } catch (final DevFailed e) {
            // Assert.fail(DevFailedUtils.toString(e));
        }
    }
}
