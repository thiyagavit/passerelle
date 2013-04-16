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
 * MultiMessageFlowElement represents a container for a message navigation inside an actor.
 * It maintains a list of pairs (input port, input message) corresponding to different data input ports and the received messages thereon.
 * This is an extension on the simple MessageFlowElement, that only models message navigation for 1 input msg on 1 port.
 * 
 * 
 * It allows to set one or more output specifications, 
 * i.e. pairs of messages and output port numbers via which the corresponding
 * messages should be sent out.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class MultiMessageFlowElement {
	
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
	
	// a list of MessageAndPort pairs
	private MessageAndPort[] inputs;
	private List<MessageAndPort> outputs;
	private int nrOfOutputPorts;

	/**
	 * 
	 * @param inputs
	 * @param nrOfOutputPorts
	 */
	public MultiMessageFlowElement(MessageAndPort[] inputs, int nrOfOutputPorts) {
		// Initialize the properties
		this.inputs= inputs;
		this.outputs = new ArrayList<MessageAndPort>();
		this.nrOfOutputPorts = nrOfOutputPorts;
	}

	/**
	 * A constructor that just defines the nr of inputs and outputs.
	 * Input specs need to be added one by one afterwards...
	 * 
	 * @param inputs
	 * @param nrOfOutputPorts
	 */
	public MultiMessageFlowElement(int nrOfInputPorts, int nrOfOutputPorts) {
		// Initialize the properties
		this.inputs= new MessageAndPort[nrOfInputPorts];
		this.outputs = new ArrayList<MessageAndPort>();
		this.nrOfOutputPorts = nrOfOutputPorts;
	}

	public int getNrInputs() {
		return inputs.length;
	}
	
	/**
	 * @return Returns the message.
	 */
	public ManagedMessage getInputMessage(int portNr) throws IndexOutOfBoundsException {
		return inputs[portNr].message;
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
	 * Returns the message of the first defined output spec.
	 * If no output spec has been defined, the default is returned,
	 * i.e. the unmodified input message for the port 0.
	 * 
	 * @return Returns the message for first defined output spec.
	 */
	public ManagedMessage getOutputMessage() {
		if(outputs.isEmpty()) {
			return getInputMessage(0);
		} else {
			return ((MessageAndPort)outputs.get(0)).message;
		}
	}

	public int getNrOutputSpecs() {
		return outputs.size();
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
			return new MessageAndPort[]{new MessageAndPort(0,getInputMessage(0))};
		} else {
			return (MessageAndPort[])outputs.toArray(new MessageAndPort[outputs.size()]);
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
		if (outputSpec.portNr < 0 || outputSpec.portNr >= nrOfOutputPorts) {
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
	 * Add a chosen input spec.
	 * 
	 * @param inputPortNr
	 *            Sets the number of the output port to use.
	 */
	public void addInputSpec(int inputPortNr, ManagedMessage message) throws IllegalArgumentException {
		addInputSpec(new MessageAndPort(inputPortNr, message));
	}
	/**
	 * Add a chosen output spec.
	 * 
	 * @param outputPortNr
	 *            Sets the number of the output port to use.
	 */
	public void addInputSpec(MessageAndPort inputSpec) throws IllegalArgumentException {
		// Check the arguments
		if(inputSpec == null) {
			throw new IllegalArgumentException("Null specs are not allowed");
		}
		if(inputSpec.message == null) {
			throw new IllegalArgumentException("Null messages are not allowed");
		}
		// Set the properties.
		try {
			this.inputs[inputSpec.portNr] = inputSpec ;
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("An impossible inPortNr, port " + inputSpec.portNr + " doesn't exist.");
		}
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
		if (outputSpec.portNr < 0 || outputSpec.portNr >= nrOfOutputPorts) {
			throw new IllegalArgumentException("An impossible outputPortNr, port " + outputSpec.portNr + " doesn't exist.");
		}
		if(outputSpec.message == null) {
			throw new IllegalArgumentException("Null messages are not allowed");
		}
		// Set the properties.
		this.outputs.add(outputSpec);
	}
}