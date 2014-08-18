package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class MessageMemoriserTest {

    private static final String MEMORISER_1 = "Memoriser_1";
    private static final String RESTORE_1 = "Restore_1";
    private static final String MEMORISER_2 = "Memoriser_2";
    private static final String RESTORE_2 = "Restore_2";

    public MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "MessageMemoriser.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void should_trhow_exception_if_message_name_is_empty() throws IllegalActionException {
        final MessageMemoriser actor = (MessageMemoriser) moml.getEntity(MEMORISER_1);
        actor.messageNameParam.setToken("");
        actor.attributeChanged(actor.messageNameParam);
    }

    @Test
    public void should_memorize_message_and_restaure_it() throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "my test");
        props.put("Constant_2.value", "my test2");

        props.put(MEMORISER_1 + ".Message Name", "message");
        props.put(MEMORISER_1 + ".Memorization", "do memorization");
        props.put(RESTORE_1 + ".Message Name", "message");
        props.put(RESTORE_1 + ".Memorization", "output value");

        props.put(MEMORISER_2 + ".Message Name", "message2");
        props.put(MEMORISER_2 + ".Memorization", "do memorization");
        props.put(RESTORE_2 + ".Message Name", "message2");
        props.put(RESTORE_2 + ".Memorization", "output value");

        final ArrayBlockingQueue<String> memorizeReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> memorizeReceiver2 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> restoreReceiver1 = new ArrayBlockingQueue<String>(1);
        final ArrayBlockingQueue<String> restoreReceiver2 = new ArrayBlockingQueue<String>(1);

        moml.addMessageReceiver(MEMORISER_1, "output", memorizeReceiver1);
        moml.addMessageReceiver(MEMORISER_2, "output", memorizeReceiver2);
        moml.addMessageReceiver(RESTORE_1, "output", restoreReceiver1);
        moml.addMessageReceiver(RESTORE_2, "output", restoreReceiver2);

        moml.executeBlockingErrorLocally(props);

        assertThat(memorizeReceiver1.poll()).isEqualTo("my test");
        assertThat(restoreReceiver1.poll()).isEqualTo("my test");

        assertThat(memorizeReceiver2.poll()).isEqualTo("my test2");
        assertThat(restoreReceiver2.poll()).isEqualTo("my test2");
    }

    @Test
    public void should_throw_exception_if_message_can_not_be_found() {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "my test");
        props.put("Constant_2.value", "my test2");

        props.put(MEMORISER_1 + ".Message Name", "message");
        props.put(MEMORISER_1 + ".Memorization", "do memorization");
        props.put(RESTORE_1 + ".Message Name", "unknow_message");
        props.put(RESTORE_1 + ".Memorization", "output value");

        props.put(MEMORISER_2 + ".Message Name", "message2");
        props.put(MEMORISER_2 + ".Memorization", "do memorization");
        props.put(RESTORE_2 + ".Message Name", "message2");
        props.put(RESTORE_2 + ".Memorization", "output value");

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);

        } catch (final PasserelleException e) {
            assertThat(e).hasMessageContaining("memorized message not found");
        }
    }
}
