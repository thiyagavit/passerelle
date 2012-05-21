package fr.soleil.passerelle.actor.flow;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

import com.isencia.passerelle.model.Flow;

@RunWith(Suite.class)
@SuiteClasses({ KeyValueListMemorizerLinkedFunctionTest.class,
        KeyValueListMemorizeStandAloneFunctionTest.class })
public class KeyValueListMemorizerSuiteCase {

    private static final Pattern PATTERN = Pattern.compile("<Body>.*</Body>");

    /**
     * A message is in XML format and contains lot of informations. This function extract the body
     * content (ie key or value )of the message
     * 
     * @param fullMessage Passerelle message formated in xml
     * @return the key or the value contain in message
     */
    public static String extractBodyContent(final String fullMessage) {
        final Matcher matcher = PATTERN.matcher(fullMessage);
        String content = "";
        if (matcher.find()) {
            content = matcher.group();
            content = content.subSequence(6, content.length() - 7).toString();
        }

        return content;
    }

    /**
     * add two MessageListener on key and value port of actor of type KeyValueListMemorizer.
     * 
     * @param actorName the name of actor
     * @param keyMsgQueue the ArrayBlockingQueue which will collect all message from key port
     * @param valueMsgQueue the ArrayBlockingQueue which will collect all message from value port
     */
    public static void addKeyValueMessageListenerToActor(final Flow topLevel,
            final String actorName, final ArrayBlockingQueue<String> keyMsgQueue,
            final ArrayBlockingQueue<String> valueMsgQueue) {

        final KeyValueListMemorizer actor = (KeyValueListMemorizer) topLevel.getEntity(actorName);
        actor.keyPort.addDebugListener(new MessageListener(keyMsgQueue));
        actor.valuePort.addDebugListener(new MessageListener(valueMsgQueue));

    }

    /**
     * add a MessageListener on outputValue port of actor of type KeyValueListMemorizer.
     * 
     * @param actorName actorName the name of actor
     * @param valueQueue the ArrayBlockingQueue which will collect all message from outputValue port
     */
    public static void addOutputValueMessageListenerToActor(final Flow topLevel,
            final String actorName, final ArrayBlockingQueue<String> valueQueue) {

        final KeyValueListMemorizer actor = (KeyValueListMemorizer) topLevel.getEntity(actorName);
        actor.outputValuePort.addDebugListener(new MessageListener(valueQueue));

    }

    /**
     * this inner class is use to collect message which transit on actor port. In this case the
     * messages which are emit by key and value ports. </br></br>
     * 
     * Message are in a {code ArrayBlockingQueue} which is guaranteed a FIFO order
     * 
     * @author GRAMER
     * 
     */
    public static class MessageListener implements DebugListener {

        private final ArrayBlockingQueue<String> msgQueue;

        /**
         * 
         * @param msgQueue the {@code ArrayBlockingQueue} which contains the messages
         */
        public MessageListener(final ArrayBlockingQueue<String> msgQueue) {
            this.msgQueue = msgQueue;
        }

        @Override
        public void event(final DebugEvent event) {
            // no need
        }

        /**
         * add message to the queue, this method is called by Passerelle system
         * 
         * @throws IllegalStateException {@inheritDoc} - if this queue is full
         */
        @Override
        public void message(final String message) {
            msgQueue.add(message);
        }

    }

}
