package fr.soleil.passerelle.actor.tango.control.motor.actions;

import static fr.soleil.passerelle.actor.tango.control.motor.actions.MoveNumericAttribute.STATUS_PREFIX;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import org.tango.utils.DevFailedUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;

public class MoveNumericAttributeTest {

    public static String MOTOR_NAME = "test/motor/1-1";
    public static String POSITION_ATTR = "position";
    public DeviceProxy motor;
    public MoveNumericAttribute mover;

    @BeforeClass
    public void setUp() throws DevFailed {

        motor = new DeviceProxy(MOTOR_NAME);
        motor.command_inout("Init");

        mover = new MoveNumericAttribute();
        mover.setDeviceName(MOTOR_NAME);
        mover.setActionName(POSITION_ATTR);
        mover.init();
    }

    @Test
    public void should_throw_devFailed_when_desiredPosition_is_empty() throws DevFailed {
        mover.setDesiredPosition("");
        mover.move();
        mover.waitEndMouvement(); // move is non blocking operation
        assertThat(motor.read_attribute(POSITION_ATTR).extractDoubleArray()[1]).isNaN(); // write
                                                                                         // part
    }

    @Test
    public void should_throw_devFailed_when_desiredPosition_is_NaN() throws DevFailed {
        mover.setDesiredPosition("it's NaN");
        try {
            mover.move();
            mover.waitEndMouvement(); // move is non blocking operation
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e))
                    .contains(
                            "Could not convert given object with class 'java.lang.String' to object with type signature 'double'");
        }
    }

    @Test
    public void should_move_the_motor_to_the_desired_position() throws DevFailed {
        mover.setDesiredPosition("0.2");
        mover.move();
        mover.waitEndMouvement(); // move is non blocking operation

        assertThat(motor.read_attribute(POSITION_ATTR).extractDouble()).isEqualTo(0.2);
        assertThat(mover.getStatus()).isEqualTo(STATUS_PREFIX + "0.2");
    }

    @Test
    public void when_cancelWaitEnd_is_called_then_main_thread_unblocking() throws DevFailed {
        // assume that takes 10 sec to the motor to go to the desired posistion
        mover.setDesiredPosition("1");
        mover.move();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    // nope
                }
                mover.cancelWaitEnd();
            }
        }.start();
        mover.waitEndMouvement(); // move is non blocking operation

        assertThat(mover.getStatus()).doesNotMatch("the motor is at (0\\.0*|1\\.0*)");
        // here the motor still move
    }

    // TODO test stop
    // TODO Test current STATUS
    // TODO test Error write
    // TODO manage sleep between steps
}
