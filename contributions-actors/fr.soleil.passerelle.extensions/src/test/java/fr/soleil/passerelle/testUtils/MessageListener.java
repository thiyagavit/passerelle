package fr.soleil.passerelle.testUtils;

import java.util.concurrent.ArrayBlockingQueue;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

/**
 * this inner class is use to collect message which transit on actor port. In
 * this case the messages which are emit by key and value ports. </br></br>
 * 
 * Message are in a {code ArrayBlockingQueue} which is guaranteed a FIFO order
 * 
 * @author GRAMER
 * 
 */
public class MessageListener implements DebugListener {
    private final ArrayBlockingQueue<String> msgQueue;

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
     * add message to the queue, this method is called by Passerelle system
     * 
     * @throws IllegalStateException
     *             {@inheritDoc} - if this queue is full
     */
    @Override
    public void message(final String message) {
        msgQueue.add(message);
    }

}