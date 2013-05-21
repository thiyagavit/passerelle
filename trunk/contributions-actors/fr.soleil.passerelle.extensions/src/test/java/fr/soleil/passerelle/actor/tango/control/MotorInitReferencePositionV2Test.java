package fr.soleil.passerelle.actor.tango.control;

import static fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5.ERROR_DEVICE_NAME_EMPTY;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.AXIS_ENCODER_TYPE_PROPERTY;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.AXIS_INIT_POSITION_PROPERTY;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.AXIS_INIT_TYPE_PROPERTY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class MotorInitReferencePositionV2Test {

    public static final DbDatum OTHER_INIT_TYPE = new DbDatum(AXIS_INIT_TYPE_PROPERTY, "LDWP");
    private static final String ACTOR_NAME = "MotorInitReferencePositionV2";
    private static final DbDatum NO_ENCODER = new DbDatum(AXIS_ENCODER_TYPE_PROPERTY, 0);
    private static final DbDatum INIT_POS_IS_0_5 = new DbDatum(AXIS_INIT_POSITION_PROPERTY, 0.5);
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

    @Deprecated
    @Test(expectedExceptions = IllegalActionException.class)
    // Test attributeChange 
    // Deprecated: should be tested by the ATangoDeviceActorV5 class
    public void when_deviceName_parameter_is_empty_before_start_then_an_excpetion_is_raised()
            throws IllegalActionException {
        MotorInitReferencePositionV2 actor = (MotorInitReferencePositionV2) moml
                .getEntity(ACTOR_NAME);

        actor.deviceNameParam.setToken("");
    }

    @Deprecated
    @Test()
    public void when_deviceName_parameter_is_empty_then_validateInitialization_raises_an_exception() {
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put(ACTOR_NAME + "." + MotorInitReferencePositionV2.DEVICE_NAME, "");

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        }
        catch (PasserelleException e) {
            assertThat(e).hasMessageContaining(ERROR_DEVICE_NAME_EMPTY);
        }
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
