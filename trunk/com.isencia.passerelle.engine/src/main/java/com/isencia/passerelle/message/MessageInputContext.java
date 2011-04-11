/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.message;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * In the new Passerelle Actor API, the MessageInputContext is a generic container
 * for attributes etc that are related to one specific message input for an actor.
 * <p>
 * Since Passerelle v5.3.1, MessageInputContext can contain multiple input msgs received on the same input port.
 * Actors capable of handling this, should use the getMsgIterator() to read all msgs present in an input context.
 * The getMsg() just returns the 1st received message.
 * </p>
 *  
 * @author erwin
 */
public class MessageInputContext {
	private int portIndex;
	private String portName;
	private Queue<ManagedMessage> msgQ = new ConcurrentLinkedQueue<ManagedMessage>();
	// indicates whether this input has already been processed by an actor
	private boolean processed;

	/**
	 * 
	 * @param portIndex
	 * @param portName
	 * @param msg
	 */
	public MessageInputContext(int portIndex, String portName, ManagedMessage msg) {
		super();
		this.portIndex = portIndex;
		this.portName = portName;
		if(msg!=null)
			msgQ.offer(msg);
		
		// nothing to be done with empty messages!
		processed=(msg==null);
	}

	public ManagedMessage getMsg() {
		return msgQ.peek();
	}
	
	public Iterator<ManagedMessage> getMsgIterator() {
		return msgQ.iterator();
	}

	public int getPortIndex() {
		return portIndex;
	}

	public String getPortName() {
		return portName;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/**
	 * Add an extra message received from this context's input port.
	 * @param inputMsg
	 */
	public void addMsg(ManagedMessage inputMsg) {
		msgQ.offer(inputMsg);
	}
}
