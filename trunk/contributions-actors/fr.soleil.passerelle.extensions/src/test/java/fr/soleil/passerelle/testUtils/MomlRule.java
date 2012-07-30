package fr.soleil.passerelle.testUtils;

import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.rules.ExternalResource;

import ptolemy.kernel.ComponentEntity;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;

/**
 * This rule have to be used with the RuleClass annotation. Using this
 * annotation instead of Rule annotation allow to read only one time the moml
 * file.
 * 
 * @author GRAMER
 * 
 */
public class MomlRule extends ExternalResource {

    private Reader reader;
    private FlowManager flowMgr;
    private final String sequenceName;

    // TODO USE AN COPY OF TOPLEVEL TO BE ABLE TO USE CLASS RULE
    private Flow original;
    private Flow copy;
    public static final Pattern PATTERN = Pattern.compile("<Body>.*</Body>");

    public MomlRule(final String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    protected void before() throws Throwable {
        reader = new InputStreamReader(getClass().getResourceAsStream(sequenceName));
        flowMgr = new FlowManager();
        original = FlowManager.readMoml(reader);

        final BasicDirector dir = new BasicDirector(original, "Dir");
        original.setDirector(dir);
        // copy = (Flow) original.clone();
    }

    @Override
    protected void after() {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                fail("cant close file: " + e.getMessage());
            }
        }
    }

    public void executeBlockingErrorLocally(final Map<String, String> props)
            throws FlowAlreadyExecutingException, PasserelleException {

        // TODO change this to flowMgr.executeBlockingErrorLocally(copy, props);
        flowMgr.executeBlockingErrorLocally(original, props);
        // TODO uncomment this line
        // reset the copy
        // copy = (Flow) original.clone();
    }

    public ComponentEntity getEntity(final String actorName) {
        // change this to copy.getEntity(actorName);
        return original.getEntity(actorName);
    }

    /**
     * this method allow to catch the messages which pass through by a port of
     * an actor. @see MessageListener
     * 
     * @param actorName
     *            the name of the actor in the sequence
     * @param portName
     *            the name of the port to spy
     * @param receiver
     *            the array which contains the caught messages.
     *            ArrayBlockingQueue guaranteed a FIFO order
     * @throws NullPointerException
     *             if the actor or the port can not be found
     */
    public void addMessageReceiver(final String actorName, final String portName,
            final ArrayBlockingQueue<String> receiver) {

        // change this to copy.getEntity(actorName);
        original.getEntity(actorName).getPort(portName)
                .addDebugListener(new MessageListener(receiver));
    }

    /**
     * A message is in XML format and contains lot of informations. This
     * function extract the body content (ie key or value )of the message
     * 
     * @param fullMessage
     *            Passerelle message formated in xml
     * @return the key or the value contain in message
     */
    public static String extractBodyContent(final String fullMessage) {
        final Matcher matcher = MomlRule.PATTERN.matcher(fullMessage);
        String content = "";
        if (matcher.find()) {
            content = matcher.group();
            content = content.subSequence(6, content.length() - 7).toString();
        }

        return content;
    }
}
