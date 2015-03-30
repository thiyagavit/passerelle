package fr.soleil.passerelle.actor.tango.control.motor.configuration;

import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.ABSOLUTE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.INCREMENTAL;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.EncoderType.NONE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.DP;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.InitType.OTHER;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.DEFINE_POS_CANT_BE_APPLY_WITH_OTHER_STRATEGIE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.INIT_REF_CANT_BE_APPLY_WITH_DP_STATEGIE;
import static fr.soleil.passerelle.actor.tango.control.motor.configuration.MotorConfigurationV2.NO_CONTROL_BOX_ATTACHED_TO;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import org.tango.utils.DevFailedUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceProxy;

/**
 * The goal of this suite test is to test the "recovery" of the motor configuration (ie the encoder,
 * the init strategy...)
 */
public class MotorConfigurationV2InitConfigTest {

    public static final String MOTOR_2_2 = "test/motor/2-2";
    public static final String MOTOR_1_1 = "test/motor/1-1";
    public static final DbDatum DP_INIT_TYPE = new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "DP");
    public static final DbDatum OTHER_INIT_TYPE = new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "LDWP");

    public static final DbDatum NO_ENCODER = new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, NONE.ordinal());
    public static final DbDatum ABSOLUTE_ENCODER = new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY,
            ABSOLUTE.ordinal());

    public static final DbDatum INIT_POS_IS_2 = new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, 2);

    private static void putProperties(String deviceName, DbDatum... properties) throws DevFailed {
        DeviceProxy motorProxy = new DeviceProxy(deviceName);
        motorProxy.put_property(properties);
    }

    @BeforeClass
    public void setUp() throws DevFailed {
    }

    @AfterClass
    public void clean() throws DevFailed {
        putProperties(MOTOR_2_2, NO_ENCODER, DP_INIT_TYPE, INIT_POS_IS_2);
    }

    @Test
    public void should_retrieve_the_controlBox_2_for_the_motor_2_2() throws DevFailed {
        MotorConfigurationV2 config = new MotorConfigurationV2(MOTOR_2_2);
        config.retrieveMyControlBox();
        assertThat(config.getControlBoxName()).isEqualTo("test/cb/2");
    }

    @Test
    public void should_retrieve_the_controlBox_1_for_the_motor_1_1() throws DevFailed {
        MotorConfigurationV2 config = new MotorConfigurationV2(MOTOR_1_1);
        config.retrieveMyControlBox();
        assertThat(config.getControlBoxName()).isEqualTo("test/cb/1");
    }

    @Test
    public void when_deviceName_is_not_a_motor_then_retrieveMyControlBox_throw_DevFailed() {
        MotorConfigurationV2 config = null;
        final String deviceName = "test/gs/1";
        try {
            // TODO change device Name
            config = new MotorConfigurationV2(deviceName);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        try {
            config.retrieveMyControlBox();
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e))
                    .contains(NO_CONTROL_BOX_ATTACHED_TO + deviceName);
        }
    }

    @DataProvider(name = "noIntEncoderProvider")
    public Object[][] noIntEncoderProvider() {
        return new Object[][] { //
        new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, "") }, //
                new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, "not a number") }, //
                new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 0.3) } //
        };
    }

    @Test(dataProvider = "noIntEncoderProvider")
    public void when_AxisEncoderType_property_is_not_a_int_then_throw_devFailed(DbDatum encoderType) {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, encoderType, DP_INIT_TYPE, INIT_POS_IS_2);
            config = new MotorConfigurationV2(MOTOR_2_2);

        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        try {
            config.retrieveProperties();
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(MotorManager.AXIS_ENCODER_TYPE_PROPERTY_IS_NOT_INT);
        }
    }

    @DataProvider(name = "validEncoderProvider")
    public Object[][] validEncoderProvider() {
        return new Object[][] { //
        new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 0), NONE }, //
                new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 1), INCREMENTAL }, //
                new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 2), ABSOLUTE }, //
        };
    }

    @Test(dataProvider = "validEncoderProvider")
    public void should_return_the_correct_encoder(DbDatum encoderType, EncoderType expected)
            throws DevFailed {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, encoderType, DP_INIT_TYPE, INIT_POS_IS_2);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.retrieveProperties();
        assertThat(config.getEncoder()).isEqualTo(expected);
    }

    @DataProvider(name = "invalidEncoderProvider")
    public Object[][] encoderErrorProvider() {
        return new Object[][] { //
        new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, -1), -1 },//
                new Object[] { new DbDatum(MotorManager.AXIS_ENCODER_TYPE_PROPERTY, 3), 3 }, //
        };
    }

    @Test(dataProvider = "invalidEncoderProvider")
    public void should_throw_exception_when_encoder_is_unknown(DbDatum encoderType, int number) {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, encoderType, DP_INIT_TYPE, INIT_POS_IS_2);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        try {
            config.retrieveProperties();
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(
                    "Encoder type: " + number + " is unknown");
        }

    }

    @DataProvider(name = "strategyProvider")
    public Object[][] strategyProvider() {
        return new Object[][] { //
        new Object[] { new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, ""), DP }, //
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "DP"), DP }, //
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "dP"), DP }, //
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_TYPE_PROPERTY, "LDPD"), OTHER }, //
        };
    }

    @Test(dataProvider = "strategyProvider")
    public void should_return_the_correct_strategy(DbDatum strategy, InitType expected)
            throws DevFailed {
        MotorConfigurationV2 config = null;
        try {
            putProperties(MOTOR_2_2, strategy, NO_ENCODER, INIT_POS_IS_2);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.retrieveProperties();
        assertThat(config.getInitStrategy()).isEqualTo(expected);
    }

    @DataProvider(name = "invalidInitPosProvider")
    public Object[][] invalidInitPosProvider() {
        return new Object[][] { //
        new Object[] { new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, "") },//
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, "not a number") }, //
        };
    }

    @Test(dataProvider = "invalidInitPosProvider")
    public void when_strategy_is_other_and_initPos_is_invalid_then_an_exception_is_raised(
            DbDatum initPosition) {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, initPosition, OTHER_INIT_TYPE, NO_ENCODER);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        try {
            config.retrieveProperties();
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(MotorManager.AXIS_INIT_POSITION_PROPERTY_IS_NAN);
        }
    }

    @Test(dataProvider = "invalidInitPosProvider")
    public void when_strategy_is_DP_and_initPos_is_invalid_Then_no_error(DbDatum initPosition)
            throws DevFailed {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, initPosition, DP_INIT_TYPE, NO_ENCODER);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }
        // if exeception is raised the test failed
        config.retrieveProperties();
    }

    @DataProvider(name = "validInitPosProvider")
    public Object[][] validInitPosProvider() {
        return new Object[][] { //
        new Object[] { INIT_POS_IS_2 },//
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, -2) },//
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, 3.5) }, //
                new Object[] { new DbDatum(MotorManager.AXIS_INIT_POSITION_PROPERTY, -3.5) }, //
        };
    }

    @Test(dataProvider = "validInitPosProvider")
    public void when_strategy_is_other_and_initPos_is_valid_then_no_error(DbDatum initPosition)
            throws DevFailed {
        MotorConfigurationV2 config = null;

        try {
            putProperties(MOTOR_2_2, initPosition, OTHER_INIT_TYPE, NO_ENCODER);
            config = new MotorConfigurationV2(MOTOR_2_2);
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }
        // if exception is raised the test failed
        config.retrieveProperties();
    }

    @Test(expectedExceptions = MotorConfigurationException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + INIT_REF_CANT_BE_APPLY_WITH_DP_STATEGIE + ".*")
    public void when_init_strategy_is_DP_then_initRefPosition_cant_be_apply() throws Exception {
        MotorConfigurationV2 config = null;
        try {
            putProperties(MOTOR_1_1, INIT_POS_IS_2, NO_ENCODER, DP_INIT_TYPE);
            config = new MotorConfigurationV2(MOTOR_1_1);
            config.retrieveProperties();
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.assertInitRefPosBeApplyOnMotor();
    }

    @Test(expectedExceptions = MotorConfigurationException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER + ".*")
    public void when_encoder_is_absolute_then_initRefPosition_cant_be_apply() throws Exception {
        MotorConfigurationV2 config = null;
        try {
            putProperties(MOTOR_1_1, INIT_POS_IS_2, ABSOLUTE_ENCODER, OTHER_INIT_TYPE);
            config = new MotorConfigurationV2(MOTOR_1_1);
            config.retrieveProperties();
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.assertInitRefPosBeApplyOnMotor();
    }

    @Test(expectedExceptions = MotorConfigurationException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + DEFINE_POS_CANT_BE_APPLY_WITH_OTHER_STRATEGIE + ".*")
    public void when_init_strategy_is_OTHER_then_DefinePosition_cant_be_apply() throws Exception {
        MotorConfigurationV2 config = null;
        try {
            putProperties(MOTOR_1_1, INIT_POS_IS_2, NO_ENCODER, OTHER_INIT_TYPE);
            config = new MotorConfigurationV2(MOTOR_1_1);
            config.retrieveProperties();
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.assertDefinePositionCanBeApplyOnMotor();
    }

    @Test
    public void when_encoder_is_not_absolute_and_init_strategie_is_not_DP_then_initRef_can_be_apply()
            throws Exception {

        putProperties(MOTOR_1_1, INIT_POS_IS_2, NO_ENCODER, OTHER_INIT_TYPE);
        MotorConfigurationV2 config = new MotorConfigurationV2(MOTOR_1_1);
        config.retrieveProperties();

        // if exception is raised the test failed
        config.assertInitRefPosBeApplyOnMotor();
    }

    @Test(expectedExceptions = MotorConfigurationException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + INIT_NOT_POSSIBLE_WITH_ABSOLUTE_ENCODER + ".*")
    public void when_encoder_is_absolute_then_definePostion_cant_be_apply() throws Exception {
        MotorConfigurationV2 config = null;
        try {
            putProperties(MOTOR_1_1, INIT_POS_IS_2, ABSOLUTE_ENCODER, DP_INIT_TYPE);
            config = new MotorConfigurationV2(MOTOR_1_1);
            config.retrieveProperties();
        }
        catch (DevFailed devFailed) {
            fail("Can not create configuration");
        }

        config.assertInitRefPosBeApplyOnMotor();
    }

    @Test
    public void when_encoder_is_not_absolute_and_init_strategie_is_not_OTHER_then_DefinePosition_can_be_apply()
            throws Exception {

        putProperties(MOTOR_1_1, INIT_POS_IS_2, NO_ENCODER, DP_INIT_TYPE);
        MotorConfigurationV2 config = new MotorConfigurationV2(MOTOR_1_1);
        config.retrieveProperties();

        // if exception is raised the test failed
        config.assertDefinePositionCanBeApplyOnMotor();
    }
}
