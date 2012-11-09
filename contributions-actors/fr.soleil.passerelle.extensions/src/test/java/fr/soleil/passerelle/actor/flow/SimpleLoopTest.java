package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class SimpleLoopTest {

    private static final String ACTOR_NAME = "SimpleLoop";
    private static final String VALUES_LIST = ACTOR_NAME + ".Values List (separated by commas)";
    private static final String VALUES_LIST_CHOICE = ACTOR_NAME + ".Use Values List";
    private static final String NUMBER_OF_LOOPS = ACTOR_NAME + ".Number of Loops";

    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "simpleLoop.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @DataProvider(name = "provider")
    public static Object[][] getParams() {
        return new Object[][] { //
        // 0- test without value list
                { 4, "false", "", "true,true,true,true" }, //

                // 1 - test with value list
                { 4, "true", "1,2,3,4", "1.0,2.0,3.0,4.0" },//

                // 2- with value list < nbItt
                { 6, "true", "1,2,3,4", "1.0,2.0,3.0,4.0,1.0,2.0" },//

                // 3 -test with only one element in value list
                { 1, "true", "1", "1.0" } //
        };
    }

    // timeout to avoid infinity loop
    @Test(timeOut = 3000, dataProvider = "provider")
    public void normalCase(final int nbLoop, final String valueListChoice, final String valueList,
            final String expectedResult) throws Exception {

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

    // timeout to avoid infinity loop
    @Test(timeOut = 3000, enabled = false)
    public void should_stop_sequence_when_exception_is_thrown() {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(NUMBER_OF_LOOPS, "4");
        props.put(VALUES_LIST_CHOICE, "false");
        props.put(VALUES_LIST, "1,2,3,4");
        props.put("generate_error.value", "1");

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.OUTPUT_PORT_NAME, outputReceiver);
        moml.addMessageReceiver(ACTOR_NAME, SimpleLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        } catch (final FlowAlreadyExecutingException e) {
            fail("can not execute the sequence " + e.getMessage());
        } catch (final PasserelleException e) {
            // TODO implement it
            assertThat(outputReceiver).isEmpty();
        }
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_a_string() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("a string");
        actor.attributeChanged(actor.loopNumberParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_empty() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("");
        actor.attributeChanged(actor.loopNumberParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_nb_loop_is_a_double() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.loopNumberParam.setToken("3.5");
        actor.attributeChanged(actor.loopNumberParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_value_list_is_a_string() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.valuesListParam.setToken(" a string");
        actor.attributeChanged(actor.valuesListParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_value_list_is_empty() throws Exception {
        final SimpleLoop actor = (SimpleLoop) moml.getEntity(ACTOR_NAME);
        actor.valuesListParam.setToken("");
        actor.attributeChanged(actor.valuesListParam);

    }

}
