package fr.soleil.passerelle.actor.tango.control;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;

import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorManager;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class MotorInitReferencePositionV2Test {

    public static final DbDatum OTHER_INIT_TYPE = new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "LDWP");
    private static final String ACTOR_NAME = "MotorInitReferencePositionV2";
    private static final DbDatum NO_ENCODER = new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 0);
    private static final DbDatum INIT_POS_IS_0_5 = new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, 0.5);
    private static String CB_NAME = "test/cb/1";
    private static String MOTOR_NAME = "test/motor/1-1";
    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH
            + "MotorInitReferencePositionV2.moml");
    private DeviceProxy cbProxy;
    private DeviceProxy motorProxy;
    private DeviceData data;

    @BeforeMethod
    public void setUp() throws Exception {
        cbProxy = new DeviceProxy(CB_NAME);
        motorProxy = new DeviceProxy(MOTOR_NAME);

        data = new DeviceData();

        data.insert(0);
        cbProxy.command_inout("setInitEndStateEndState", data);

        data = new DeviceData();
        data.insert(false);
        motorProxy.command_inout("setInitEndStateToFault", data);

        DeviceProxy motorProxy = new DeviceProxy(MOTOR_NAME);
        motorProxy.put_property(new DbDatum[] { OTHER_INIT_TYPE, NO_ENCODER, INIT_POS_IS_0_5 });

        cbProxy.command_inout("Init");
        motorProxy.command_inout("Init");

        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test
    public void should_init_the_motor() throws PasserelleException {
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put(ACTOR_NAME + "." + MotorInitReferencePositionV2.DEVICE_NAME, MOTOR_NAME);

        ArrayBlockingQueue<String> outPortMsgs = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, MotorInitReferencePositionV2.OUTPUT_PORT_NAME,
                outPortMsgs);
        moml.executeBlockingErrorLocally(props);

        assertThat(outPortMsgs).hasSize(1);
        assertThat(outPortMsgs.poll()).isEqualTo("");
    }
}
