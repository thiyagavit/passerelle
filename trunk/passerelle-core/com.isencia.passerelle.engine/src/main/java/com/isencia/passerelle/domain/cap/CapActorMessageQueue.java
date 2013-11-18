package com.isencia.passerelle.domain.cap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageQueue;

/**
 * Instances of this class can be used to manage the actor's internal buffer to handle messages from PUSH input ports. 
 * It mixes queuing with the locking behaviour of Ptolemy's PNQueueReceiver. 
 * It can not be a Ptolemy receiver itself, as it's not contained in a Port, but in an Actor...
 * 
 * @author erwin
 */
public class CapActorMessageQueue implements MessageQueue {
  /**
   * Used to indicate that the size of the queue or the history queue is infinite.
   */
  public static final int INFINITE_CAPACITY = -1;

  private DummyReceiver myDummyReceiver = new DummyReceiver();
  private Actor actor;
  private CapDirector director;
  private BlockingQueue<MessageInputContext> messages;
  private int capacity;
  private boolean terminate;

  /** Reference to a thread that is read blocked on this receiver. */
  private Thread _readPending = null;

  /** Reference to a thread that is write blocked on this receiver. */
  private Thread _writePending = null;

  CapActorMessageQueue(Actor actor, int capacity) throws InitializationException {
    this.actor = actor;
    this.capacity = capacity;
    try {
      this.director = (CapDirector) actor.getDirector();
      messages = new LinkedBlockingQueue<MessageInputContext>(capacity);
    } catch (ClassCastException e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Can not create a CapActorMessageQueue when no CapDirector is used", actor, e);
    }
  }

  public int getCapacity() {
    return capacity;
  }
  
  @Override
  public int size() {
    return messages.size();
  }
  
  @Override
  public boolean isEmpty() {
    return messages.isEmpty();
  }

  public void setCapacity(int capacity) throws InitializationException, IllegalArgumentException {
    if ((capacity < 0) && (capacity != INFINITE_CAPACITY)) {
      throw new IllegalArgumentException("Cannot set queue capacity to " + capacity);
    }
    if ((capacity != INFINITE_CAPACITY) && (messages.size() > capacity)) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Queue contains more elements than the proposed capacity.", actor, null);
    }
    this.capacity = capacity;
  }

  public void put(MessageInputContext ctxt) throws InterruptedException {
    synchronized (director) {
      while (!terminate) {
        if (messages.remainingCapacity() > 0) {
          messages.put(ctxt);
          // If any thread is blocked on a get(), then it will become
          // unblocked. Notify the director now so that there isn't a
          // spurious deadlock detection.
          if (_readPending != null) {
            director.threadUnblocked(_readPending, myDummyReceiver, PNDirector.READ_BLOCKED);
            _readPending = null;
          }

          // Normally, the _writePending reference will have
          // been cleared by the read that unblocked this write.
          // However, it might be that the director increased the
          // buffer size, which would also have the affect of unblocking
          // this write. Hence, we clear it here if it is set.
          if (_writePending != null) {
            director.threadUnblocked(_writePending, myDummyReceiver, PNDirector.WRITE_BLOCKED);
            _writePending = null;
          }
          break;
        }
        // Wait to try again.
        try {
          _writePending = Thread.currentThread();
          director.threadBlocked(_writePending, myDummyReceiver, PNDirector.WRITE_BLOCKED);

          Workspace workspace = director.workspace();
          workspace.wait(director);
        } catch (InterruptedException e) {
          terminate = true;
        }
      }
      if (terminate) {
        throw new TerminateProcessException("Process terminated.");
      }
    }
  }

  public MessageInputContext take() throws InterruptedException, TerminateProcessException {
    MessageInputContext result = null;

    // NOTE: This method used to be synchronized on this
    // receiver, but since it calls synchronized methods in
    // the director, that can cause deadlock.
    synchronized (director) {
      if (!messages.isEmpty()) {
        result = messages.take();

        // Need to mark any thread that is write blocked on
        // this receiver unblocked now, before any notification,
        // or we will detect deadlock and increase the buffer sizes.
        // Note that there is no need to clear the _readPending
        // reference because that will have been cleared by the write.
        if (_writePending != null) {
          director.threadUnblocked(_writePending, myDummyReceiver, PNDirector.WRITE_BLOCKED);
          _writePending = null;
        }
      } else {
        while (!terminate) {
          // Wait to try again.
          try {
            _readPending = Thread.currentThread();
            director.threadBlocked(Thread.currentThread(), myDummyReceiver, PNDirector.READ_BLOCKED);

            Workspace workspace = director.workspace();
            workspace.wait(director);
          } catch (InterruptedException e) {
            terminate = true;
          }
          if (!messages.isEmpty()) {
            result = messages.take();

            // Need to mark any thread that is write blocked on
            // this receiver unblocked now, before any notification,
            // or we will detect deadlock and increase the buffer sizes.
            // Note that there is no need to clear the _readPending
            // reference because that will have been cleared by the write.
            if (_writePending != null) {
              director.threadUnblocked(_writePending, myDummyReceiver, PNDirector.WRITE_BLOCKED);
              _writePending = null;
            }
            break;
          }
        }
      }
      if (terminate && result == null) {
        throw new TerminateProcessException("");
      }
    }
    return result;
  }

  /**
   * Clear the state variables for this queue.
   */
  public void clear() {
    if (_readPending != null) {
      director.threadUnblocked(_readPending, myDummyReceiver, PNDirector.READ_BLOCKED);
    }
    if (_writePending != null) {
      director.threadUnblocked(_writePending, myDummyReceiver, PNDirector.WRITE_BLOCKED);
    }
    _readPending = null;
    _writePending = null;
    messages.clear();
  }

  private static class DummyReceiver implements ProcessReceiver {
    @Override
    public void clear() throws IllegalActionException {
    }

    @Override
    public Token get() throws NoTokenException {
      throw new NoTokenException("dummy receiver");
    }

    @Override
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
      throw new NoTokenException("dummy receiver");
    }

    @Override
    public IOPort getContainer() {
      return null;
    }

    @Override
    public boolean hasRoom() {
      return true;
    }

    @Override
    public boolean hasRoom(int numberOfTokens) {
      return true;
    }

    @Override
    public boolean hasToken() {
      return true;
    }

    @Override
    public boolean hasToken(int numberOfTokens) {
      return true;
    }

    @Override
    public boolean isKnown() {
      return false;
    }

    @Override
    public void put(Token token) throws NoRoomException, IllegalActionException {
      throw new IllegalActionException("dummy receiver");
    }

    @Override
    public void putArray(Token[] tokenArray, int numberOfTokens) throws NoRoomException, IllegalActionException {
    }

    @Override
    public void putArrayToAll(Token[] tokens, int numberOfTokens, Receiver[] receivers) throws NoRoomException, IllegalActionException {
      throw new IllegalActionException("dummy receiver");
    }

    @Override
    public void putToAll(Token token, Receiver[] receivers) throws NoRoomException, IllegalActionException {
      throw new IllegalActionException("dummy receiver");
    }

    @Override
    public void setContainer(IOPort port) throws IllegalActionException {
      throw new IllegalActionException("dummy receiver");
    }

    @Override
    public boolean isConnectedToBoundary() {
      return false;
    }

    @Override
    public boolean isConnectedToBoundaryInside() {
      return false;
    }

    @Override
    public boolean isConnectedToBoundaryOutside() {
      return false;
    }

    @Override
    public boolean isConsumerReceiver() {
      return false;
    }

    @Override
    public boolean isInsideBoundary() {
      return false;
    }

    @Override
    public boolean isOutsideBoundary() {
      return false;
    }

    @Override
    public boolean isProducerReceiver() {
      return false;
    }

    @Override
    public boolean isReadBlocked() {
      return false;
    }

    @Override
    public boolean isWriteBlocked() {
      return false;
    }

    @Override
    public void requestFinish() {
    }

    @Override
    public void reset() {
    }
  }
}
