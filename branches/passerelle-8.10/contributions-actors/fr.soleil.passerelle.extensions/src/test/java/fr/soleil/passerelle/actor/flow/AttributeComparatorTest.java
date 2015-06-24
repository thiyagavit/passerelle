package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;
import fr.soleil.passerelle.testUtils.dataProviders.ComparisonProvider;

public class AttributeComparatorTest {

    private static final String ACTOR_NAME = "Comparator1";

    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "AttributeComparator.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test(dataProviderClass = ComparisonProvider.class, dataProvider = ComparisonProvider.NAME)
    public void nominalTest(final String leftValue, final String comparison,
            final String rightValue, final String tolerence, final boolean expectedMessage)
            throws Exception {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", leftValue);
        props.put("Constant2.value", rightValue);
        props.put(ACTOR_NAME + ".comparison", comparison);
        props.put(ACTOR_NAME + ".tolerance", tolerence);

        // add receiver to collect the messages which pass through the output
        // ports
        final ArrayBlockingQueue<String> trueTriggerReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> falseTriggerReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, "trueOutput (Trigger)", trueTriggerReceiver);
        moml.addMessageReceiver(ACTOR_NAME, "falseOutput (Trigger)", falseTriggerReceiver);

        moml.executeBlockingErrorLocally(props);

        if (expectedMessage) {
            assertThat(trueTriggerReceiver).hasSize(1);
            assertThat(falseTriggerReceiver).hasSize(0);
            // TODO message is always the same need to test it ?
            assertThat(trueTriggerReceiver.poll()).isEqualTo("true");
        } else {
            assertThat(trueTriggerReceiver).hasSize(0);
            assertThat(falseTriggerReceiver).hasSize(1);
            assertThat(falseTriggerReceiver.poll()).isEqualTo("true");
        }
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_if_tolerance_is_not_a_number() throws IllegalActionException {
        final AttributeComparator actor = (AttributeComparator) moml.getEntity(ACTOR_NAME);

        actor.toleranceParam.setToken("azerty");
        actor.attributeChanged(actor.toleranceParam);
    }

}
