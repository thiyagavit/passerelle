package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import static fr.esrf.Tango.DevState.ALARM;
import static fr.esrf.Tango.DevState.DISABLE;
import static fr.esrf.Tango.DevState.FAULT;
import static fr.esrf.Tango.DevState.MOVING;
import static fr.esrf.Tango.DevState.OFF;
import static fr.esrf.Tango.DevState.ON;
import static fr.esrf.Tango.DevState.RUNNING;
import static fr.esrf.Tango.DevState.STANDBY;
import static fr.esrf.Tango.DevState.UNKNOWN;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.InitCommand.Init_ERROR_MSG;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.MicroCodeCommand.MICRO_CODE_ERROR_MSG;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.initDevices.OnCommand.ON_ERROR_MSG;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;

/**
 * The goal of this suite test is to test the initialization sequence of the ControlBox and
 * GalilAxis devices?
 */

public class MotorConfigurationV2InitMotorTest {

    private static String CB_NAME = "test/cb/1";
    private static String MOTOR_NAME = "test/motor/1-1";
    private MotorConfigurationV2 config;
    private DeviceProxy cbProxy;
    private DeviceProxy motorProxy;
    private DeviceData data;

    @BeforeMethod
    public void setUp() throws DevFailed {
        cbProxy = new DeviceProxy(CB_NAME);
        motorProxy = new DeviceProxy(MOTOR_NAME);

        data = new DeviceData();

        data.insert(0);
        cbProxy.command_inout("setInitEndStateEndState", data);
        // cbProxy.command_inout("setMicroCodeEndState", data);

        data = new DeviceData();
        data.insert(false);
        motorProxy.command_inout("setInitEndStateToFault", data);
        //
        // data.insert(0);
        // motorProxy.command_inout("setOnEndState", data);

        cbProxy.command_inout("Init");
        motorProxy.command_inout("Init");
        config = new MotorConfigurationV2(MOTOR_NAME);
    }

    @Test
    public void shoudl_init_devices() throws DevFailed, ProcessingException {
        config.retrieveFullConfig();
        config.initDevice(null);

        assertThat(cbProxy.state()).isEqualTo(ON);
        assertThat(motorProxy.state()).isIn(STANDBY, ON);
        assertThat(config.isSwitchToOffAfterInit()).isFalse();
    }

    @DataProvider(name = "rescueCommandProvider")
    public Object[][] rescueCommandProvider() {
        return new Object[][] { //
        new Object[] { FAULT }, //
                new Object[] { UNKNOWN },//
                new Object[] { ALARM },//
        };
    }

    @Test(dataProvider = "rescueCommandProvider")
    public void when_CB_is_fault_or_unknown_or_alarm_and_rescue_command_works_then_cb_is_init(
            DevState initialCbState) throws DevFailed, ProcessingException {
        data.insert(initialCbState.value());
        cbProxy.command_inout("switchState", data);
        config.retrieveFullConfig();
        config.initDevice(null);

        assertThat(cbProxy.state()).isEqualTo(DevState.ON);
        assertThat(config.isSwitchToOffAfterInit()).isFalse();
    }

    @DataProvider(name = "rescueCommandFailedProvider")
    public Object[][] rescueCommandFailedProvider() {
        return new Object[][] { //
        new Object[] { FAULT, "setInitEndStateEndState", Init_ERROR_MSG }, //
                new Object[] { UNKNOWN, "setInitEndStateEndState", Init_ERROR_MSG },//
                new Object[] { ALARM, "setMicroCodeEndState", MICRO_CODE_ERROR_MSG },//
        };
    }

    @Test(dataProvider = "rescueCommandFailedProvider")
    public void when_CB_is_fault_or_unknown_or_alarm_and_rescue_command_NOT_works_then_cb_is_init(
            DevState initialCbState, String cmdToSimulateError, String expectedErrorMsg)
            throws DevFailed {
        data.insert(initialCbState.value());
        cbProxy.command_inout("switchState", data);

        data.insert(initialCbState.value());
        cbProxy.command_inout(cmdToSimulateError, data);

        try {
            config.retrieveFullConfig();
            config.initDevice(null);
            failBecauseExceptionWasNotThrown(ProcessingException.class);
        }
        catch (ProcessingException e) {
            assertThat(e).hasMessageContaining(expectedErrorMsg);
            assertThat(cbProxy.state()).isEqualTo(initialCbState);
            assertThat(config.isSwitchToOffAfterInit()).isFalse();
        }
    }

    @Test
    public void should_throw_an_exception_when_galil_is_Moving_during_init() throws DevFailed {
        data.insert(MOVING.value());
        motorProxy.command_inout("switchState", data);
        try {
            config.retrieveFullConfig();
            config.initDevice(null);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        }
        catch (ProcessingException e) {
            assertThat(cbProxy.state()).isEqualTo(DevState.ON);
            assertThat(motorProxy.state()).isEqualTo(MOVING);
            assertThat(e).hasMessageContaining(
                    "Error device " + MOTOR_NAME + " is in  " + MOVING + " state");
            assertThat(config.isSwitchToOffAfterInit()).isFalse();
        }
    }

    @Test
    public void should_throw_an_exception_when_galil_is_Disable() throws DevFailed {
        data.insert(DISABLE.value());
        motorProxy.command_inout("switchState", data);
        try {
            config.retrieveFullConfig();
            config.initDevice(null);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        }
        catch (ProcessingException e) {
            assertThat(cbProxy.state()).isEqualTo(DevState.ON);
            assertThat(motorProxy.state()).isEqualTo(DISABLE);
            assertThat(e).hasMessageContaining(
                    "Error device " + MOTOR_NAME + " is in  " + DISABLE + " state");
            assertThat(config.isSwitchToOffAfterInit()).isFalse();
        }
    }

    @Test
    public void when_motor_is_off_and_on_command_work_then_devices_should_be_init()
            throws DevFailed, ProcessingException {
        data.insert(OFF.value());
        motorProxy.command_inout("switchState", data);

        config.retrieveFullConfig();
        config.initDevice(null);

        assertThat(cbProxy.state()).isEqualTo(ON);
        assertThat(motorProxy.state()).isEqualTo(ON);
        assertThat(config.isSwitchToOffAfterInit()).isTrue();

    }

    @Test
    public void when_motor_is_off_and_on_command_NOT_work_then_devices_should_not_be_init()
            throws DevFailed {
        data.insert(OFF.value());
        motorProxy.command_inout("switchState", data);
        motorProxy.command_inout("setOnEndState", data);
        try {
            config.retrieveFullConfig();
            config.initDevice(null);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        }
        catch (ProcessingException e) {
            assertThat(cbProxy.state()).isEqualTo(ON);
            assertThat(motorProxy.state()).isEqualTo(OFF);
            assertThat(e).hasMessageContaining(ON_ERROR_MSG);
        }
    }

    @Test
    public void should_throw_exception_when_motor_is_not_StandBy_or_On_at_end_of_init()
            throws DevFailed {
        data.insert(RUNNING.value());
        motorProxy.command_inout("switchState", data);
        try {
            config.retrieveFullConfig();
            config.initDevice(null);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        }
        catch (ProcessingException e) {
            assertThat(cbProxy.state()).isEqualTo(ON);
            assertThat(motorProxy.state()).isEqualTo(RUNNING);
            assertThat(e).hasMessageContaining("Motor is " + RUNNING + " insteadof  StandBy or On");
        }
    }
}
