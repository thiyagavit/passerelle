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

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.soleil.passerelle.testUtils.MomlRule;

@RunWith(Parameterized.class)
public class ForLoopTest {

    private static final String ACTOR_NAME = "ForLoop";
    // TODO test Exception in loop
    @Rule
    public final MomlRule moml = new MomlRule("/sequences/forLoopActor.moml");

    private final String start;
    private final String end;
    private final String step;
    private final int nbItteration;

    public ForLoopTest(final String start, final String end, final String step,
            final int nbItteration) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.nbItteration = nbItteration;
    }

    @Parameters
    public static List<Object[]> getParams() {
        return Arrays.asList(new Object[][] {//
                // check positives
                        { "0", "3", "1", 4 },// 0 check step =1
                        { "2", "3", "1", 2 },// 1 check with start >0
                        { "0", "3", "2", 2 },// 2 check with step !=1

                        // check negatives
                        { "0", "-3", "1", 4 },// 3 check step =1
                        { "-3", "-6", "1", 4 },// 4 check with start <0
                        { "-3", "3", "1", 7 },// 5 negative to positive
                        { "0", "-3", "2", 2 },// 6 check with step !=1

                });

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_start_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.startValueParam.setToken("a string");
        actor.attributeChanged(actor.startValueParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_start_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.startValueParam.setToken("");
        actor.attributeChanged(actor.startValueParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_end_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.endValueParam.setToken("a string");
        actor.attributeChanged(actor.endValueParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_end_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.endValueParam.setToken("");
        actor.attributeChanged(actor.endValueParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_step_is_a_string() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("a string");
        actor.attributeChanged(actor.stepWidthParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = NumberFormatException.class)
    public void should_throw_exception_when_step_is_empty() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("");
        actor.attributeChanged(actor.stepWidthParam);

    }

    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(expected = IllegalActionException.class)
    public void should_throw_exception_when_step_is_less_than_one() throws Exception {
        final ForLoop actor = (ForLoop) moml.getEntity(ACTOR_NAME);
        actor.stepWidthParam.setToken("0");
        actor.attributeChanged(actor.stepWidthParam);
    }

    // FIXME: the sequence is never stop: the test is blocked at
    // moml.executeBlockingErrorLocally(props), an exception is traced
    // TODO: use dataProvider (TestNg or JunitParam lib) to avoid to execute
    // this test 7 times
    @Ignore
    @Test(timeout = 3000)
    public void should_stop_loop_and_sequence_when_exception_is_thrown() {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(ACTOR_NAME + "." + ForLoop.START_VALUE_PARAM_NAME, start);
        props.put(ACTOR_NAME + "." + ForLoop.END_VALUE_PARAM_NAME, end);
        props.put(ACTOR_NAME + "." + ForLoop.STEP_WIDTH_PARAM_NAME, step);

        props.put("generate_error.value", "1");

        final ArrayBlockingQueue<String> outpoutReceiver = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> endLoopReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.OUTPUT_PORT_NAME, outpoutReceiver);
        moml.addMessageReceiver(ACTOR_NAME, ForLoop.END_LOOP_PORT_NAME, endLoopReceiver);

        try {
            moml.executeBlockingErrorLocally(props);
            fail("missign exception");
        } catch (final FlowAlreadyExecutingException e) {
            fail("this expcetion should not be thown : " + e.getMessage());
        } catch (final PasserelleException e) {
            // TODO assert is the exception thrown by errorGenerator
        }
        //
        // // check number of messages
        // assertThat(continuingReceiver).hasSize(itteration);
        // assertThat(outputReceiver).hasSize(1);

    }

    // to avoid infinity loop
    @Test(timeout = 3000)
    public void normalCase() throws Exception {

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
}
