package fr.soleil.passerelle.testUtils;

import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.rules.ExternalResource;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;

/**
 * This rule have to be used with the RuleClass annotation. Using this annotation instead of Rule
 * annotation allow to read only one time the moml file.
 * 
 * @author GRAMER
 * 
 */
// TODO remove inheritance
public class MomlRule extends ExternalResource {

    private Reader reader = null;
    private FlowManager flowMgr;
    private final String sequenceName;

    // TODO USE AN COPY OF TOPLEVEL TO BE ABLE TO USE CLASS RULE
    private Flow original;
    private Flow copy;

    public MomlRule(final String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public void before() throws Exception {
        if (reader == null) {
            reader = new InputStreamReader(getClass().getResourceAsStream(sequenceName));
            flowMgr = new FlowManager();
            original = FlowManager.readMoml(reader);

            final BasicDirector dir = new BasicDirector(original, "Dir");
            original.setDirector(dir);
        }
        copy = (Flow) original.clone(new Workspace());

    }

    @Override
    public void after() {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                fail("cant close file: " + e.getMessage());
            }
        }
    }

    public void executeBlockingErrorLocally(final Map<String, String> props) throws FlowAlreadyExecutingException,
            PasserelleException {

        flowMgr.executeBlockingErrorLocally(copy, props);

    }

    public ComponentEntity getEntity(final String actorName) {
        return copy.getEntity(actorName);
    }

    /**
     * this method allow to catch the messages which pass through by a port of an actor. @see
     * MessageListener
     * 
     * @param actorName the name of the actor in the sequence
     * @param portName the name of the port to spy
     * @param receiver the array which contains the caught messages. ArrayBlockingQueue guaranteed a
     *            FIFO order
     * @throws NullPointerException if the actor or the port can not be found
     */
    public void addMessageReceiver(final String actorName, final String portName,
            final ArrayBlockingQueue<String> receiver) {
        copy.getEntity(actorName).getPort(portName).addDebugListener(new MessageListener(receiver));
    }
}
