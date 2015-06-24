package fr.soleil.passerelle.actor.tango.snapshot;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ExtractValueFromLastSnap extends ASnapExtractor {
    public Parameter contextIDParam;
    private String contextID;

    /**
     * 
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ExtractValueFromLastSnap(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        contextIDParam = new StringParameter(this, "Context ID");
        contextIDParam.setExpression("1");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        try {
            setSnapIDFromContext(contextID);
            getAndSendValues();
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == contextIDParam) {
            contextID = PasserelleUtil.getParameterValue(contextIDParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return null;
    }

}
