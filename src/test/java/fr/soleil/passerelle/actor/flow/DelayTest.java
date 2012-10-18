package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Rule;
import org.junit.Test;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.MomlRule;

public class DelayTest {
    @Rule
    public MomlRule moml = new MomlRule("/sequences/Delay.moml");

    private static final String ACTOR_NAME = "Delay1";

    @Test(expected = NumberFormatException.class)
    public void should_throw_expection_if_time_is_a_string() throws IllegalActionException {

        final Delay actor = (Delay) moml.getEntity(ACTOR_NAME);
        actor.timeParameter.setToken("yop");
        actor.attributeChanged(actor.timeParameter);

    }

    @Test(timeout = 5000)
    public void parameterSleepTest() throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", "azerty");
        props.put(ACTOR_NAME + ".time(s)", "3");

        // add receiver to collect the messages which pass through the output
        // ports
        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, "output", outputReceiver);

        final long start = System.currentTimeMillis();
        moml.executeBlockingErrorLocally(props);
        final long end = System.currentTimeMillis();

        assertThat(end - start).isGreaterThanOrEqualTo(3000).isLessThan(4000);
        assertThat(outputReceiver.poll()).isEqualTo("azerty");

    }

    @Test(timeout = 5000)
    public void portSleepTest() throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", "2.0");
        props.put(ACTOR_NAME + ".time(s)", "4.0");
        props.put(ACTOR_NAME + ".take delay from port", "true");

        // add receiver to collect the messages which pass through the output
        // ports
        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, "output", outputReceiver);

        final long start = System.currentTimeMillis();
        moml.executeBlockingErrorLocally(props);
        final long end = System.currentTimeMillis();

        assertThat(end - start).isGreaterThanOrEqualTo(2000).isLessThan(3000);
        assertThat(outputReceiver.poll()).isEqualTo("2.0");
    }

    @Test
    public void should_throw_expection_if_input_is_string_and_porDelay_is_true() {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", "azerty");
        props.put(ACTOR_NAME + ".time(s)", "4.0");
        props.put(ACTOR_NAME + ".take delay from port", "true");

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        } catch (final PasserelleException e) {
            assertThat(e).hasMessageContaining("azerty");
        }

    }
}
