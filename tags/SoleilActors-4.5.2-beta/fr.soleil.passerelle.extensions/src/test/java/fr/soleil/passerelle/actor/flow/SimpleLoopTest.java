package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.soleil.passerelle.testUtils.MomlRule;

@RunWith(Parameterized.class)
public class SimpleLoopTest {

    private static final String ACTOR_NAME = "SimpleLoop";
    private static final String VALUES_LIST = ACTOR_NAME + ".Values List (separated by commas)";
    private static final String VALUES_LIST_CHOICE = ACTOR_NAME + ".Use Values List";
    private static final String NUMBER_OF_LOOPS = ACTOR_NAME + ".Number of Loops";

    @Rule
    public final MomlRule moml = new MomlRule("/sequences/simpleLoop.moml");

    private final int nbLoop;
    private final String valueListChoice;
    private final String valueList;
    private final String expectedResult;

    public SimpleLoopTest(final int nbLoop, final String valueListChoice, final String valueList,
            final String expectedResult) {
        this.nbLoop = nbLoop;
        this.valueListChoice = valueListChoice;
        this.valueList = valueList;
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static List<Object[]> getParams() {
        return Arrays.asList((new Object[][] { //
                // 0- test without value list
                        { 4, "false", "", "true,true,true,true" }, //

                        // 1 - test with value list
                        { 4, "true", "1,2,3,4", "1.0,2.0,3.0,4.0" },//

                        // 2- with value list < nbItt
                        { 6, "true", "1,2,3,4", "1.0,2.0,3.0,4.0,1.0,2.0" },//

                        // 3 -test with only one element in value list
                        { 1, "true", "1", "1.0" } //
                }));
    }

    // timeout to avoid infinity loop
    @Test(timeout = 3000)
    public void normalCase() throws Exception {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(NUMBER_OF_LOOPS, String.valueOf(nbLoop));
        props.put(VALUES_LIST_CHOICE, valueListChoice);
        props.put(VALUES_LIST, valueList);
        props.put("generate_error.value", "0");

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(nbLoop);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.OUTPUT_PORT_NAME, outputReceiver);
        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        moml.executeBlockingErrorLocally(props);

        // check number of messages and content
        final String[] expectedValues = expectedResult.split(",");
        assertThat(outputReceiver).hasSize(nbLoop);
        for (int i = 0; i < outputReceiver.size(); i++) {
            assertThat(outputReceiver.poll()).isEqualTo(expectedValues[i]);
        }
        assertThat(endLoopReceiver).hasSize(1);
    }

    // FIXME: the sequence is never stop and the exception is not forwarded to
    // moml.executeBlockingErrorLocally
    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    // timeout to avoid infinity loop
    @Ignore
    @Test(timeout = 3000)
    public void should_stop_sequence_when_exception_is_thrown() {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(NUMBER_OF_LOOPS, "4");
        props.put(VALUES_LIST_CHOICE, "false");
        props.put(VALUES_LIST, "");
        props.put("generate_error.value", "1");

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.OUTPUT_PORT_NAME, outputReceiver);
        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        try {
            moml.executeBlockingErrorLocally(props);
            fail("exception was not throw");
        } catch (final FlowAlreadyExecutingException e) {
            fail("can not execute the sequence " + e.getMessage());
        } catch (final PasserelleException e) {
            e.printStackTrace();
        }
        // check number of messages and content
        final String[] expectedValues = expectedResult.split(",");
        assertThat(outputReceiver).hasSize(1);
        assertThat(outputReceiver.poll()).isEqualTo("true");

    }

    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_a_string() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("a string");
        actor.attributeChanged(actor.loopNumberParam);

    }

    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_empty() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("");
        actor.attributeChanged(actor.loopNumberParam);

    }

    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_a_double() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("3.5");
        actor.attributeChanged(actor.loopNumberParam);

    }

    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_value_list_is_a_string() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.valuesListParam.setToken(" a string");
        actor.attributeChanged(actor.valuesListParam);

    }

    // TODO: use dataProvider (final TestNg or JunitParam lib) to avoid to
    // execute this test 30 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_value_list_is_empty() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.valuesListParam.setToken("");
        actor.attributeChanged(actor.valuesListParam);

    }

}
