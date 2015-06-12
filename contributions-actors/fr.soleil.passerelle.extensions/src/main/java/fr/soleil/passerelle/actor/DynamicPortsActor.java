package fr.soleil.passerelle.actor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.MessageInputContext;

@SuppressWarnings("serial")
public abstract class DynamicPortsActor extends com.isencia.passerelle.actor.dynaport.DynamicPortsActor {

    public enum DynamicPortType {
        EQUAL_NUMBER_INPUT_OUTPUT, DIFF_NUMBER_INPUT_OUTPUT, ONLY_INPUTS, ONLY_OUTPUTS;
    }

    private static Logger logger = LoggerFactory.getLogger(DynamicPortsActor.class);

    private String inputPortPrefix = "input";
    private String outputPortPrefix = "output";
    private int idxOffsetPort = 0;

    private Class<?> expectedContentType;

    public DynamicPortsActor(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        switch (getPortConfiguration()) {
            case EQUAL_NUMBER_INPUT_OUTPUT:
                // remove separate parameter for nr of output ports
                if (numberOfOutputs != null) {
                    numberOfOutputs.setContainer(null);
                    numberOfOutputs = null;
                }
                if (numberOfInputs != null) {
                    numberOfInputs.setName("Nr of ports");
                }
                break;
            case ONLY_INPUTS:
                if (numberOfOutputs != null) {
                    numberOfOutputs.setContainer(null);
                    numberOfOutputs = null;
                }
                break;
            case ONLY_OUTPUTS:
                if (numberOfInputs != null) {
                    numberOfInputs.setContainer(null);
                    numberOfInputs = null;
                }
                break;
            default:
                // do nothing for DIFF_NUMBER_INPUT_OUTPUT
                break;
        }

    }

    /**
     * 
     * @param newPortCount
     *            The amount of ports needed.
     * @param currPortCount
     *            The current nr of ports of the requested type
     * @param portType
     *            PortType.INPUT or PortType.OUTPUT, this parameter is used to
     *            set default values for a port and to choose a default name.
     * @throws IllegalActionException
     * @throws IllegalArgumentException
     */
    @Override
    protected void changeNumberOfPorts(final int newPortCount, final int currPortCount, final PortType portType)
            throws IllegalActionException, IllegalArgumentException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " changeNumberOfPorts() - entry - portType : " + portType + " / new nrOfPorts : "
                    + newPortCount);
        }
        switch (getPortConfiguration()) {
            case EQUAL_NUMBER_INPUT_OUTPUT:
                // Since we only enabled the input port count cfg parameter,
                // each change in the nr of input ports will pass here once.
                // And then the code below ensures that the output port count
                // remains
                // the same as the input port count.
                this.createDeletePorts(newPortCount, currPortCount, PortType.INPUT);
                this.createDeletePorts(newPortCount, currPortCount, PortType.OUTPUT);
                nrOutputPorts = newPortCount;
                nrInputPorts = newPortCount;
                break;
            case ONLY_INPUTS:
                this.createDeletePorts(newPortCount, currPortCount, PortType.INPUT);
                nrInputPorts = newPortCount;
                break;
            case ONLY_OUTPUTS:
                this.createDeletePorts(newPortCount, currPortCount, PortType.OUTPUT);
                nrOutputPorts = newPortCount;
                break;
        }

    }

    private void createDeletePorts(final int newPortCount, final int currPortCount, final PortType portType)
            throws IllegalActionException, IllegalArgumentException {
        // Set port to input or output
        // Remark: input is never multiport, output is always multiport
        boolean isInput = false, isOutput = true;
        String namePrefix;
        if (portType == PortType.INPUT) {
            isInput = true;
            isOutput = false;
            namePrefix = getInputPortPrefix();
        } else if (portType == PortType.OUTPUT) {
            isInput = false;
            isOutput = true;
            namePrefix = getOutputPortPrefix();
        } else {
            throw new IllegalArgumentException("Unknown PortType: " + portType);
        }

        // if we want lesser ports, remove some
        if (newPortCount < currPortCount) {
            for (int i = currPortCount - 1; i >= 0 && i >= newPortCount; i--) {
                final String portName = namePrefix + (i + idxOffsetPort);
                deletePort(portName);
            }
        }
        // if we want more ports, reuse old ones + add new ones
        else if (newPortCount > currPortCount) {
            for (int i = currPortCount; i < newPortCount; i++) {
                createPort(namePrefix, i, isInput, isOutput);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " changeNumberOfPorts() - exit");
        }
    }

    protected void deletePort(final String portName) throws IllegalActionException {
        // remove the port
        try {
            // System.out.println("removing " + portName);
            this.getPort(portName).setContainer(null);
        } catch (final Exception e) {
            throw new IllegalActionException(this, e, "failed to remove port " + portName);
        }
    }

    /**
     * 
     * @param name
     * @param isInput
     * @param isOutput
     * @param typeOfData
     * @return
     * @throws IllegalActionException
     */
    @Override
    protected Port createPort(final String namePrefix, final int index, final boolean isInput, final boolean isOutput)
            throws IllegalActionException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " createPort() - entry - name : " + namePrefix + " index : "
                    + (index + idxOffsetPort));
        }
        Port aPort = null;

        final String portName = namePrefix + (index + idxOffsetPort);

        try {
            aPort = (Port) getPort(portName);

            if (aPort == null) {
                // System.out.println("creating  " + portName);
                logger.debug(getName() + " createPort() - port " + portName + " will be constructed");
                if (isInput) {
                    aPort = PortFactory.getInstance().createInputPort(this, portName,
                            getPortModeForNewInputPort(portName), null);
                    if (expectedContentType != null) {
                        aPort.setExpectedMessageContentType(expectedContentType);
                    }
                } else {
                    aPort = PortFactory.getInstance().createOutputPort(this, portName);
                }
                aPort.setMultiport(!isInput);
            } else {
                logger.debug(getName() + " createPort() - port " + portName + " already exists");
                // ensure it has the right characteristics
                aPort.setInput(isInput);
                aPort.setOutput(isOutput);
                aPort.setMode(getPortModeForNewInputPort(portName));
                if (isInput && expectedContentType != null) {
                    aPort.setExpectedMessageContentType(expectedContentType);
                }
            }
        } catch (final Exception e) {
            throw new IllegalActionException(this, e, "failed to create port " + portName);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " createPort() - exit - port : " + aPort);
        }
        return aPort;
    }

    /**
     * @return Returns the inputPorts.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Port> getInputPorts() {
        // in order to avoid cloning issues
        // when we would maintain the list of dynamically cfg-ed
        // input ports in an instance variable,
        // we build this list dynamically here from
        // Ptolemy's internal port list
        final List<Port> inpPorts = new ArrayList<Port>();
        final List<Port> inputPortList = inputPortList();
        for (final Port inP : inputPortList) {
            if (inP.getName().startsWith(getInputPortPrefix(), 0)) {
                inpPorts.add(inP);
            }
        }
        return inpPorts;
    }

    /**
     * Overridable method to decide whether <br/>
     * - there should be 1 or 2 parameters to set the nr of input/output ports.
     * If an equal nr of input & output ports is OK (default case), only 1
     * parameter is shown.<br/>
     * - there are only dynamic input ports - there are only dynamic output
     * ports <br/>
     * 
     * 
     * @return flag indicating whether this Synch... implementation uses an
     *         equal nr of input and output ports.<br/>
     *         Remark that this method is invoked in the constructor, so its
     *         implementation should not depend on the instance being completely
     *         constructed!<br/>
     *         It should just contain a hard-coded true or false!!
     */
    protected DynamicPortType getPortConfiguration() {
        return DynamicPortType.EQUAL_NUMBER_INPUT_OUTPUT;
    }

    public String getInputPortPrefix() {
        return inputPortPrefix;
    }

    public void setInputPortPrefix(final String inputPortPrefix) {
        this.inputPortPrefix = inputPortPrefix;
    }

    public String getOutputPortPrefix() {
        return outputPortPrefix;
    }

    public void setOutputPortPrefix(final String outputPortPrefix) {
        this.outputPortPrefix = outputPortPrefix;
    }

    public int getIdxOffsetPort() {
        return idxOffsetPort;
    }

    /**
     * 
     * @param idxOffsetPort
     *            The index at which the port naming will start.
     */
    public void setIdxOffsetPort(final int idxOffsetPort) {
        this.idxOffsetPort = idxOffsetPort;
    }

    public Class<?> getExpectedContentType() {
        return expectedContentType;
    }

    public void setExpectedContentType(final Class<?> expectedContentType) {
        this.expectedContentType = expectedContentType;
    }

    protected int getPortIndex(final MessageInputContext messageInputContext) {
        String portName = messageInputContext.getPortName();
        String portIndexStr = portName.substring(getInputPortPrefix().length());
        int portIndex = Integer.parseInt(portIndexStr);
        return portIndex;
    }

}
