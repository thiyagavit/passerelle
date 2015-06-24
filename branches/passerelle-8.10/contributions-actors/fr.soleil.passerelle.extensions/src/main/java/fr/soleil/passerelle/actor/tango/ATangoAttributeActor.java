package fr.soleil.passerelle.actor.tango;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public abstract class ATangoAttributeActor extends ATangoActor {

    private static final String ATTRIBUTE_NAME = "Attribute Name";
    private final static Logger logger = LoggerFactory.getLogger(ATangoAttributeActor.class);

    /**
     * The name of the attribute to write
     */
    @ParameterName(name = ATTRIBUTE_NAME)
    public Parameter attributeNameParam;
    private String attributeName = "tango/tangotest/1/short_scalar";

    private TangoAttribute tangoAttribute;

    public ATangoAttributeActor(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        attributeNameParam = new StringParameter(this, ATTRIBUTE_NAME);
        attributeNameParam.setExpression(attributeName);
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == attributeNameParam) {
            attributeName = PasserelleUtil.getParameterValue(attributeNameParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    /**
     * When not in mock mode, create the attribute proxy with the attribute name
     * 
     * @throws InitializationException
     */

    @Override
    protected void validateInitialization() throws ValidationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - entry");
        }

        if (!isMockMode()) {
            try {
                tangoAttribute = new TangoAttribute(attributeName);
            } catch (final DevFailed e) {
                ExceptionUtil.throwValidationException(this, e);
            }
        }
        super.validateInitialization();

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - exit");
        }
    }

    /**
     * 
     * @return The attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * 
     * @return A TangoAttribute that is initialized in {@link ATangoAttributeActor#doInitialize()}
     * @throws PasserelleException
     */
    public TangoAttribute getTangoAttribute() throws PasserelleException {
        if (tangoAttribute == null) {
            ExceptionUtil.throwPasserelleException("field not initialized", attributeName);
        }
        return tangoAttribute;
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final ATangoAttributeActor copy = (ATangoAttributeActor) super.clone(workspace);
        copy.tangoAttribute = null;
        return copy;
    }

}
