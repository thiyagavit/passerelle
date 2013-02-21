package fr.soleil.passerelle.testUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

/**
 * this inner class is use to collect message which transit on actor port. Only
 * the body content of the message is stored </br></br>
 * 
 * Message are in a {code ArrayBlockingQueue} which is guaranteed a FIFO order
 * 
 * @author GRAMER
 * 
 */
public class MessageListener implements DebugListener {
    private final ArrayBlockingQueue<String> msgQueue;
    private static final Pattern PATTERN = Pattern.compile("<Body>.*</Body>");

    /**
     * 
     * @param msgQueue
     *            the {@code ArrayBlockingQueue} which contains the messages
     */
    public MessageListener(final ArrayBlockingQueue<String> msgQueue) {
        this.msgQueue = msgQueue;
    }

    @Override
    public void event(final DebugEvent event) {
        // no need
    }

    /**
     * add the body content of the message to the queue, this method is called
     * by Passerelle system
     * 
     * @throws IllegalStateException
     *             {@inheritDoc} - if this queue is full
     */
    @Override
    public void message(final String message) {
        msgQueue.add(extractBodyContent(message));
    }

    /**
     * A message is in XML format and contains lot of informations. This
     * function extract the body content (ie key or value )of the message
     * 
     * @param fullMessage
     *            Passerelle message formated in xml
     * @return the key or the value contain in message
     */
    private String extractBodyContent(final String fullMessage) {
        final Matcher matcher = PATTERN.matcher(fullMessage);
        String content = "";
        if (matcher.find()) {
            content = matcher.group();
            content = content.subSequence(6, content.length() - 7).toString();
        }

        return content;
    }

}