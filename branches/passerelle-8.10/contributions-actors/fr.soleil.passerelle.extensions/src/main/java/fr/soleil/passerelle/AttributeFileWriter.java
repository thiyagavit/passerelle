/*
 * Created on 9 juin 2005
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.soleil.passerelle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.FileSaver;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author root
 * 
 *         TODO A retester
 * 
 */
@SuppressWarnings("serial")
public class AttributeFileWriter extends Sink {
    private final static Logger logger = LoggerFactory.getLogger(AttributeFileWriter.class);
    public Parameter filePathParam;
    private String filePath = "/home/expert";
    public Parameter fileNameParam;
    private String fileName = "test";
    public Parameter fileExtensionParam;
    private String fileExtension = "txt";
    FileSaver fileWriter;

    Map<Long, SequenceTrace> map = new HashMap<Long, SequenceTrace>();

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public AttributeFileWriter(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        fileNameParam = new StringParameter(this, "File Name");
        fileNameParam.setExpression(fileName);
        registerConfigurableParameter(fileNameParam);

        filePathParam = new StringParameter(this, "File Path");
        filePathParam.setExpression(filePath);
        registerConfigurableParameter(filePathParam);

        fileExtensionParam = new StringParameter(this, "File Extension");
        fileExtensionParam.setExpression(fileExtension);
        registerConfigurableParameter(fileExtensionParam);

        fileWriter = null;
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == fileNameParam) {
            fileName = ((StringToken) fileNameParam.getToken()).stringValue();
        } else if (arg0 == filePathParam) {
            filePath = ((StringToken) filePathParam.getToken()).stringValue();
        } else if (arg0 == fileExtensionParam) {
            fileExtension = ((StringToken) fileExtensionParam.getToken()).stringValue();
        } else {
            super.attributeChanged(arg0);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }

        super.doInitialize();
        map.clear();
        if (fileWriter != null) {
            fileWriter.endSave();
        }
        fileWriter = FileSaver.getInstance(filePath, fileName, fileExtension);
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.isencia.passerelle.actor.Sink#sendMessage(com.isencia.passerelle.message
     * .ManagedMessage)
     */
    @Override
    protected void sendMessage(final ManagedMessage outgoingMessage) throws ProcessingException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " sendMessage() - entry");
        }
        if (outgoingMessage.isPartOfSequence()) {
            // get sequence
            final Long seqID = outgoingMessage.getSequenceID();
            SequenceTrace trace = map.get(seqID);
            if (trace == null) {
                trace = new SequenceTrace(seqID);
                map.put(seqID, trace);
            }
            trace.addMessage(outgoingMessage);

            if (trace.isComplete()) {
                // write all seq to file
                final ManagedMessage[] messageArray = trace.getMessagesInSequence();
                for (final ManagedMessage message : messageArray) {
                    writeMessageInFile(message);
                }
                map.remove(seqID);
            }
        } else {
            writeMessageInFile(outgoingMessage);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " sendMessage() - exit");
        }
    }

    /**
     * @param outgoingMessage
     * @param message
     * @throws ProcessingException
     */
    private void writeMessageInFile(final ManagedMessage outgoingMessage) throws ProcessingException {
        // TODO: utiliser la conversion automatique
        TangoAttribute att = null;
        String str = null;
        boolean typeString = false;
        try {
            final Object message = outgoingMessage.getBodyContent();
            if (message instanceof TangoAttribute) {
                att = (TangoAttribute) message;
            } else {
                str = message.toString();
                typeString = true;
            }
        } catch (final MessageException e) {
            // e.printStackTrace();
            ExceptionUtil.throwProcessingException("Cannot get input message", this.getName(), e);
        }

        try {
            if (typeString == false) {
                // save a Tango Attribute to file
                if (logger.isTraceEnabled()) {
                    logger.trace(getName() + ": write attr " + att.getAttributeProxy().fullName() + " to file");
                }
                ExecutionTracerService.trace(this, "write attribute " + att.getAttributeProxy().fullName()
                        + " to file: " + fileWriter.getFullFileName());
                String attr = att.getAttributeProxy().fullName() + "\t";
                if (att.getAttributeProxy().get_info().data_format.equals(AttrDataFormat.IMAGE)) {
                    attr += "\n";
                }
                attr += att.readAsString("\t", "");
                fileWriter.save(attr);
            } else {
                // Save others objects to file
                logger.trace(getName() + ": write string to file");
                ExecutionTracerService.trace(this, "write data to file: " + fileWriter.getFullFileName());
                final String attr = "not a Tango attribute  \t";
                fileWriter.save(attr + str);
            }
        } catch (final IOException e1) {
            // e1.printStackTrace();
            ExceptionUtil.throwProcessingException("Cannot write to file", fileName, e1);

        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }
}
