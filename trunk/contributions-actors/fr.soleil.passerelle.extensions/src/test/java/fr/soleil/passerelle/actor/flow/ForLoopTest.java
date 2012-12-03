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

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class ForLoopTest {

    private static final String ACTOR_NAME = "ForLoop";

    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "forLoopActor.moml");

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
        return new Object[][] {//
        // check positives
                { "0", "3", "1", 4 },// 0 check step =1
                { "2", "3", "1", 2 },// 1 check with start >0
                { "0", "3", "2", 2 },// 2 check with step !=1
                { "10.0", "11.4", "0.2", 8 },// 0 check step =1

                // check negatives
                { "0", "-3", "1", 4 },// 3 check step =1
                { "-3", "-6", "1", 4 },// 4 check with start <0
                { "-3", "3", "1", 7 },// 5 negative to positive
                { "0", "-3", "2", 2 },// 6 check with step !=1

        };
    }

    // to avoid infinity loop
    @Test(timeOut = 5000, dataProvider = "provider")
    public void normalCase(final String start, final String end, final String step,
            final int nbItteration) throws Exception {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(ACTOR_NAME + "." + ForLoop.START_VALUE_PARAM_NAME, start);
        props.put(ACTOR_NAME + "." + ForLoop.END_VALUE_PARAM_NAME, end);
        props.put(ACTOR_NAME + "." + ForLoop.STEP_WIDTH_PARAM_NAME, step);

        props.put("generate_error.value", "0");

        final ArrayBlockingQueue<String> outpoutReceiver = new ArrayBlockingQueue<String>(
                nbItteration);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.OUTPUT_PORT_NAME, outpoutReceiver);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        moml.executeBlockingErrorLocally(props);

        assertThat(outpoutReceiver).hasSize(nbItteration);
        assertThat(endLoopReceiver).hasSize(1);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_start_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.startValueParam.setToken("a string");
        actor.attributeChanged(actor.startValueParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_start_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.startValueParam.setToken("");
        actor.attributeChanged(actor.startValueParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_end_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.endValueParam.setToken("a string");
        actor.attributeChanged(actor.endValueParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_end_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.endValueParam.setToken("");
        actor.attributeChanged(actor.endValueParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_step_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("a string");
        actor.attributeChanged(actor.stepWidthParam);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void should_throw_exception_when_step_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("");
        actor.attributeChanged(actor.stepWidthParam);

    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_throw_exception_when_step_is_less_than_one() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("0");
        actor.attributeChanged(actor.stepWidthParam);
    }

    // FIXME: the sequence is never stop: the test is blocked at
    // moml.executeBlockingErrorLocally(props), an exception is traced
    @Test(timeOut = 3000, enabled = false)
    public void should_stop_loop_and_sequence_when_exception_is_thrown() {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(ACTOR_NAME + "." + ForLoop.START_VALUE_PARAM_NAME, "0");
        props.put(ACTOR_NAME + "." + ForLoop.END_VALUE_PARAM_NAME, "5");
        props.put(ACTOR_NAME + "." + ForLoop.STEP_WIDTH_PARAM_NAME, "1");

        props.put("generate_error.value", "1");

        final ArrayBlockingQueue<String> outpoutReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.OUTPUT_PORT_NAME, outpoutReceiver);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        } catch (final FlowAlreadyExecutingException e) {
            fail("this expcetion should not be thown : " + e.getMessage());
        } catch (final PasserelleException e) {
            // TODO assert is the exception thrown by errorGenerator
        }
    }
}
