/*
 * (c) Copyright 2004-2005, iSencia NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia NV, Belgium.  
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.script;

import java.util.ArrayList;
import java.util.List;
import com.isencia.passerelle.message.ManagedMessage;


/**
 * 
 * MessageFlowElement represents a container for a message navigation inside an actor.
 * It maintains the input port on which the input message arrived.
 * It allows to set one or more output specifications, 
 * i.e. pairs of messages and output port numbers via which the corresponding
 * messages should be sent out.
 * 
 * @author erwin.de.ley@isencia.be
 * @deprecated use {@link ProcessRequest} and {@link ProcessResponse} instead
 */
public class MessageFlowElement {
	
	public static class MessageAndPort {
		public ManagedMessage message = null;
		public int portNr = 0;
		
		public MessageAndPort() {
		}
		
		public MessageAndPort(int portNr, ManagedMessage message) {
			this.portNr = portNr;
			this.message = message;
		}
	}
	
	private MessageAndPort input;
	private List outputs;
	private int amountOfOutputPorts;

	/**
	 * 
	 * @param input
	 * @param amountOfOutputPorts
	 * @throws IllegalArgumentException If the input spec is invalid, i.e. null or with a null message
	 */
	public MessageFlowElement(MessageAndPort input, int amountOfOutputPorts) throws IllegalArgumentException {
		// Check the arguments
		if (input == null || input.message == null) {
			throw new IllegalArgumentException("Message input data are invalid");
		}

		// Initialize the properties
		this.input= input;
		this.outputs = new ArrayList();
		this.amountOfOutputPorts = amountOfOutputPorts;
	}

	/**
	 * @return Returns the input port number.
	 */
	public int getInputPortNr() {
		return input.portNr;
	}

	/**
	 * @return Returns the message.
	 */
	public ManagedMessage getInputMessage() {
		return input.message;
	}

	/**
	 * Returns the number of the first defined output spec.
	 * If no output spec has been defined, the default (0)
	 * is returned.
	 * 
	 * @return Returns the number of first defined output spec.
	 */
	public int getOutputPortNr() {
		if(outputs.isEmpty()) {
			return 0;
		} else {
			return ((MessageAndPort)outputs.get(0)).portNr;
		}
	}

	/**
	 * Returns the number of the first defined output spec.
	 * If no output spec has been defined, the default is returned,
	 * i.e. the unmodified input message.
	 * 
	 * @return Returns the message for first defined output spec.
	 */
	public ManagedMessage getOutputMessage() {
		if(outputs.isEmpty()) {
			return getInputMessage();
		} else {
			return ((MessageAndPort)outputs.get(0)).message;
		}
	}

	/**
	 * Returns all defined output specs.
	 * If no output has been defined, the default is returned,
	 * i.e. an array with a single spec for port 0 containing 
	 * the unmodified input message.
	 * 
	 * @return Returns the number of first chosen outputPort.
	 */
	public MessageAndPort[] getAllOutputSpecs() {
		if(outputs.isEmpty()) {
			return new MessageAndPort[]{new MessageAndPort(0,getInputMessage())};
		} else {
			return (MessageAndPort[])outputs.toArray(new MessageAndPort[0]);
		}
	}

	/**
	 * Set a single chosen output spec.
	 * If any output specs were set prior to calling this method,
	 * they are all removed.
	 * 
	 * @param outputPortNr
	 *            Sets the number of the output port to use.
	 * @param message
	 * 			  Sets the message to be sent via that port.
	 * 
	 * @throws IllegalArgumentException if the portNr is invalid, i.e. < 0 or > the actor's nr of data output ports,
	 * 			  or if the message is null.
	 */
	public void setOutputSpec(int outputPortNr, ManagedMessage message) throws IllegalArgumentException {
		setOutputSpec(new MessageAndPort(outputPortNr,message));
	}
	/**
	 * Set a single chosen output spec.
	 * If any output specs were set prior to calling this method,
	 * they are all removed.
	 * 
	 * @param outputSpec
	 *            Sets the number of the output port to use and the message to send.
	 * 
	 * @throws IllegalArgumentException if the portNr is invalid, i.e. < 0 or > the actor's nr of data output ports,
	 * 			  or if the message is null.
	 */
	public void setOutputSpec(MessageAndPort outputSpec) throws IllegalArgumentException {
		// Check the arguments
		if(outputSpec == null) {
			throw new IllegalArgumentException("Null specs are not allowed");
		}
		// Check the arguments
		if (outputSpec.portNr < 0 || outputSpec.portNr >= amountOfOutputPorts) {
			throw new IllegalArgumentException("An impossible outputPortNr, port " + outputSpec.portNr + " doesn't exist.");
		}
		if(outputSpec.message == null) {
			throw new IllegalArgumentException("Null messages are not allowed");
		}
		// Set the properties.
		this.outputs.clear();
		this.outputs.add(outputSpec);
	}

	/**
	 * Add a chosen output spec.
	 * If more than one output spec is defined, the specified message
	 * will be sent out by the actor on each corresponding specified data output port.
	 * 
	 * @param outputPortNr
	 *            Sets the number of the output port to use.
	 */
	public void addOutputSpec(int outputPortNr, ManagedMessage message) throws IllegalArgumentException {
		addOutputSpec(new MessageAndPort(outputPortNr, message));
	}
	/**
	 * Add a chosen output spec.
	 * If more than one output spec is defined, the specified message
	 * will be sent out by the actor on each corresponding specified data output port.
	 * 
	 * @param outputPortNr
	 *            Sets the number of the output port to use.
	 */
	public void addOutputSpec(MessageAndPort outputSpec) throws IllegalArgumentException {
		// Check the arguments
		if(outputSpec == null) {
			throw new IllegalArgumentException("Null specs are not allowed");
		}
		// Check the arguments
		if (outputSpec.portNr < 0 || outputSpec.portNr >= amountOfOutputPorts) {
			throw new IllegalArgumentException("An impossible outputPortNr, port " + outputSpec.portNr + " doesn't exist.");
		}
		if(outputSpec.message == null) {
			throw new IllegalArgumentException("Null messages are not allowed");
		}
		// Set the properties.
		this.outputs.add(outputSpec);
	}
}