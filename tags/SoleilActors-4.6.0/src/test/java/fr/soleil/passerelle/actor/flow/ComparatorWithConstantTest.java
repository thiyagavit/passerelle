package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ptolemy.kernel.util.IllegalActionException;
import fr.soleil.passerelle.testUtils.MomlRule;

@RunWith(Parameterized.class)
public class ComparatorWithConstantTest {
    @Rule
    public final MomlRule moml = new MomlRule("/sequences/ComparatorWithConst.moml");

    private static final String ACTOR_NAME = "Comparator1";
    private final String constant;
    private final String comparison;
    private final String rValue;
    private final String tolerence;
    private final boolean expectedMessage;

    // TODO put this in another TestCase
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_if_tolerance_is_not_a_number() throws IllegalActionException {
        final ComparatorWithConstant actor = (ComparatorWithConstant) moml.getEntity(ACTOR_NAME);

        actor.toleranceParam.setToken("azerty");
        actor.attributeChanged(actor.toleranceParam);
    }

    @Parameters
    public static List<Object[]> getParametres() {
        return Arrays.asList(new Object[][] { //

                        // TEST ON STRING
                        { "aaaa", ">", "bbb", "0.0", false },// 0
                        { "a", ">", "aa", "0.0", false },// 1
                        { "ccc", ">", "ccc", "0.0", false }, // 2
                        { "aA", ">", "aa", "0.0", false },// 3 ignore case
                        { "b", ">", "a", "0.0", true },// 4

                        { "aaaa", ">=", "bbb", "0.0", false },// 5
                        { "a", ">=", "aa", "0.0", false },// 6
                        { "ccc", ">=", "ccc", "0.0", true }, // 7
                        { "aA", ">=", "aa", "0.0", true },// 8
                        { "b", ">", "a", "0.0", true },// 9

                        { "aaaa", "<", "bbb", "0.0", true },// 10
                        { "a", "<", "aa", "0.0", true },// 11
                        { "ccc", "<", "ccc", "0.0", false }, // 12
                        { "aA", "<", "aa", "0.0", false },// 13 ignore case
                        { "b", "<", "a", "0.0", false },// 14

                        { "aaaa", "<=", "bbb", "0.0", true },// 15
                        { "a", "<=", "aa", "0.0", true },// 16
                        { "ccc", "<=", "ccc", "0.0", true }, // 17
                        { "aA", "<=", "aa", "0.0", true },// 18 ignore case
                        { "b", "<", "a", "0.0", false },// 19

                        { "aaaa", "==", "bbb", "0.0", false },// 20
                        { "a", "==", "aa", "0.0", false },// 21
                        { "ccc", "==", "ccc", "0.0", true }, // 22
                        { "aA", "==", "aa", "0.0", true },// 23 ignore case

                        { "aaaa", "!=", "bbb", "0.0", true },// 24
                        { "a", "!=", "aa", "0.0", true },// 25
                        { "ccc", "!=", "ccc", "0.0", false }, // 26
                        { "aA", "!=", "aa", "0.0", false },// 27 ignore case

                        // TEST ON DOUBLE
                        { "0.1", ">", "1.2", "0.0", false },// 28
                        { "0.1", ">", "1.2", "0.1", false },// 29
                        { "0.1", ">", "0.1", "0.0", false },// 30
                        { "1.2", ">", "0.1", "0.0", true }, // 31
                        { "0.1", ">", "1.2", "1.1", true },// 32
                        { "0.1", ">", "0.1", "0.1", true },// 33

                        { "0.1", ">=", "1.2", "0.0", false },// 34
                        { "0.1", ">=", "1.2", "0.1", false },// 35
                        { "0.1", ">=", "0.1", "0.0", true },// 36
                        { "1.2", ">=", "0.1", "0.0", true }, // 37
                        { "0.1", ">=", "1.2", "1.1", true },// 38
                        { "0.1", ">=", "0.1", "0.1", true },// 39

                        { "0.1", "<", "1.2", "0.0", true },// 40
                        { "0.1", "<", "1.2", "0.1", true },// 41
                        { "0.1", "<", "0.1", "0.0", false },// 42
                        { "1.2", "<", "0.1", "0.0", false }, // 43
                        { "0.1", "<", "1.2", "1.1", true },// 44
                        { "0.1", "<", "0.1", "0.1", true },// 45

                        { "0.1", "<=", "1.2", "0.0", true },// 46
                        { "0.1", "<=", "1.2", "0.1", true },// 47
                        { "0.1", "<=", "0.1", "0.0", true },// 48
                        { "1.2", "<=", "0.1", "0.0", false }, // 49
                        { "0.1", "<=", "1.2", "1.1", true },// 50
                        { "0.1", "<=", "0.1", "0.1", true },// 51

                        // test DOUBLE --STRING and STRING --DOUBLE
                        { "1.2", ">", "bbb", "100.0", false },// 51
                        { "bbb", ">", "1.2", "100.0", true },// 52

                        { "1.2", ">=", "bbb", "100.0", false },// 53
                        { "bbb", ">=", "1.2", "100.0", true },// 54

                        { "1.2", "<", "bbb", "100.0", true },// 55
                        { "bbb", "<", "1.2", "100.0", false },// 56

                        { "1.2", "<=", "bbb", "100.0", true },// 57
                        { "bbb", "<=", "1.2", "100.0", false },// 58

                        { "1.2", "!=", "bbb", "100.0", true },// 59
                        { "bbb", "!=", "1.2", "100.0", true },// 60

                });

    }

    public ComparatorWithConstantTest(final String constant, final String comparison,
            final String rValue, final String tolerance, final boolean expectedMessage) {
        this.constant = constant;
        this.rValue = rValue;
        this.comparison = comparison;
        this.tolerence = tolerance;
        this.expectedMessage = expectedMessage;
    }

    @Test
    public void nominalTest() throws Exception {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", constant);
        props.put(ACTOR_NAME + ".comparison", comparison);
        props.put(ACTOR_NAME + ".right value", rValue);
        props.put(ACTOR_NAME + ".tolerance", tolerence);

        // add receiver to collect the messages which pass through the output
        // ports
        final ArrayBlockingQueue<String> trueTriggerReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> falseTriggerReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, "true trigger", trueTriggerReceiver);
        moml.addMessageReceiver(ACTOR_NAME, "false trigger", falseTriggerReceiver);

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

}