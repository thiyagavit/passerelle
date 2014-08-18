package fr.soleil.passerelle.actor.tango.basic;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ErrorCode;

import fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public abstract class DeviceProperty extends ATangoDeviceActorV5 {

    public static final String PROPERTY_NAME = "property name";
    public static final String ERROR_PROPERTY_NAME_EMPTY = PROPERTY_NAME + " parameter can not be empty";
    public Parameter propertyNameParam;
    protected String propertyName;

    public DeviceProperty(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        checkIsAliveAtValidateInit = false;

        propertyNameParam = new StringParameter(this, PROPERTY_NAME);
        propertyNameParam.setExpression("prop");

        recordDataParam.setVisibility(Settable.EXPERT);
    }

    @Override
    protected void validateInitialization() throws ValidationException {


        if (!isMockMode()) {
            try {
                propertyName = extractPropertyName();
            } catch (IllegalActionException e) {
                throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e); // TODO
            }

        }
        super.validateInitialization();
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == propertyNameParam) {
            propertyName = extractPropertyName();
        } else {
            super.attributeChanged(arg0);
        }
    }

    private String extractPropertyName() throws IllegalActionException {
        String dname = PasserelleUtil.getParameterValue(propertyNameParam);
        if (dname.isEmpty()) {
            throw new IllegalActionException(ERROR_PROPERTY_NAME_EMPTY);
        }
        return dname;
    }
}
