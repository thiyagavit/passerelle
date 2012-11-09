package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import static org.fest.assertions.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class SwitchTest {
    private static final String ACTOR_NAME = "Switch1";
    public MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "Switch.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_a_string() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("azerty");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_negative() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("-1");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_zero() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("0");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_double() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("1.5");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test()
    public void should_add_output_port() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");

        // get the number of port and add 1
        final int oldPortNumber = ((IntToken) actor.numberOfOutputs.getToken()).intValue();
        final int newPortNumber = oldPortNumber + 1;

        // change the number of port
        actor.numberOfOutputs.setToken(String.valueOf(newPortNumber));
        actor.attributeChanged(actor.numberOfOutputs);

        // build the expected result
        final Object[] expectedOutputPortNames = new Object[newPortNumber];
        for (int i = 0; i < newPortNumber; i++) {
            expectedOutputPortNames[i] = "output " + i;
        }

        assertThat(extractProperty("name").from(actor.portList()))
                .contains(expectedOutputPortNames);
    }

    @DataProvider(name = "Input provider")
    public static Object[][] getParameters() {
        return new Object[][] {
                // nominal case
                { "0", 0 },//
                { "1", 1 },//
                { "2", 2 },//

                // select the last output if selectPort is too hight
                { "4", 2 },//

                // select the first output if selectPort is negative
                { "-1", 0 },//

                // select the first output if selectPort is empty string
                { "", 0 },//

                // select the first output if selectPort is string
                { "a String", 0 },//

        };
    }

    @Test(dataProvider = "Input provider")
    public void should_select_the_right_output(final String input, final int output)
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", input);
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        switch (output) {
        case 0:
            assertThat(outputReceiver0.poll()).isEqualTo("azerty");
            assertThat(outputReceiver1).isEmpty();
            assertThat(outputReceiver2).isEmpty();
            break;
        case 1:
            assertThat(outputReceiver0).isEmpty();
            assertThat(outputReceiver1.poll()).isEqualTo("azerty");
            assertThat(outputReceiver2).isEmpty();
            break;
        case 2:
            assertThat(outputReceiver0).isEmpty();
            assertThat(outputReceiver1).isEmpty();
            assertThat(outputReceiver2.poll()).isEqualTo("azerty");
            break;
        default:
            fail("test not implemented for " + output);
        }
    }
}