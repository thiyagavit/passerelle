package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Rule;
import org.junit.Test;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.MomlRule;

public class MessageAssemblerTest {
    private static final String ACTOR_NAME = "MessageAssembler1";
    @Rule
    public MomlRule moml = new MomlRule("/sequences/MessageAssembler.moml");

    @Test
    public void should_assemble_message() throws PasserelleException {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("ForLoopMemorize1.Start Value", "0");
        props.put("ForLoopMemorize1.End Value", "2");
        props.put("ValuesGenerator.Values List (sep by commas)", "a,b,c");

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(3);
        moml.addMessageReceiver(ACTOR_NAME, "output", outputReceiver);

        moml.executeBlockingErrorLocally(props);

        assertThat(outputReceiver.poll()).isEqualTo("a");
        assertThat(outputReceiver.poll()).isEqualTo("a,b");
        assertThat(outputReceiver.poll()).isEqualTo("a,b,c");

    }
}
