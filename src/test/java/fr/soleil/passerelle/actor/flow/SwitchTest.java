package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Rule;
import org.junit.Test;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.MomlRule;

public class SwitchTest {
    @Rule
    public MomlRule moml = new MomlRule("/sequences/Switch.moml");

    private static final String ACTOR_NAME = "Switch1";

    // TODO USE PARAMETRIZED TEST
    @Test(expected = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_a_string() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("azerty");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expected = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_negative() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("-1");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expected = IllegalActionException.class)
    public void should_throw_exception_if_nbOutput_is_zero() throws IllegalActionException {
        final Switch actor = (Switch) moml.getEntity("Switch1");
        actor.numberOfOutputs.setToken("0");
        actor.attributeChanged(actor.numberOfOutputs);
    }

    @Test(expected = IllegalActionException.class)
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

    // TODO PARAMETERIZED THESE TESTS
    @Test()
    public void should_select_the_first_output_if_selectPort_is_a_string()
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "truc");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0.poll()).isEqualTo("azerty");
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2).isEmpty();
    }

    @Test()
    public void should_select_the_first_output_if_selectPort_is_an_empty_string()
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0.poll()).isEqualTo("azerty");
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2).isEmpty();
    }

    @Test()
    public void should_select_the_first_output_if_selectPort_is_negative()
            throws PasserelleException {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "-1");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0.poll()).isEqualTo("azerty");
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2).isEmpty();
    }

    @Test()
    public void should_select_the_last_output_if_selectPort_is_too_hight()
            throws PasserelleException {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "4");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0).isEmpty();
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2.poll()).isEqualTo("azerty");
    }

    @Test()
    public void should_select_the_first_output_if_selectPort_is_equal_to_0()
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "0");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0.poll()).isEqualTo("azerty");
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2).isEmpty();
    }

    @Test()
    public void should_select_the_second_output_if_selectPort_is_equal_to_1()
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "1");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0).isEmpty();
        assertThat(outputReceiver1.poll()).isEqualTo("azerty");
        assertThat(outputReceiver2).isEmpty();
    }

    @Test()
    public void should_select_the_third_output_if_selectPort_is_equal_to_2()
            throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "azerty");
        props.put("Constant_2.value", "2");
        props.put(ACTOR_NAME + ".count", "3");

        final ArrayBlockingQueue<String> outputReceiver0 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> outputReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, "output 0", outputReceiver0);
        moml.addMessageReceiver(ACTOR_NAME, "output 1", outputReceiver1);
        moml.addMessageReceiver(ACTOR_NAME, "output 2", outputReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver0).isEmpty();
        assertThat(outputReceiver1).isEmpty();
        assertThat(outputReceiver2.poll()).isEqualTo("azerty");
    }
}