/*
 * Synchrotron Soleil
 * 
 * File : Monochromator.java
 * 
 * Project : soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 4 d�c. 2006
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: Monochromator.java,v
 */
/*
 * Created on 4 d�c. 2006
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.control.conf;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.tango.ATangoDeviceActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class ConfigureMonochromator extends ATangoDeviceActor {

    public Parameter confParam;
    protected String confValue = "";

    private TangoCommand configureCmd;

    private String enumDeviceName;

    public ConfigureMonochromator(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);
        recordDataParam.setVisibility(Settable.EXPERT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
     * )
     */
    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == confParam) {
            confValue = ((StringToken) confParam.getToken()).stringValue();
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                // do not use the monochromator device
                // using the device initially installed for GlobalScreen to be
                // able
                // to manage with all types of monochromators
                configureCmd = new TangoCommand(getDeviceName() + enumDeviceName, confValue);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        final ManagedMessage message = request.getMessage(input);

        try {
            if (isMockMode()) {
                ExecutionTracerService.trace(this, "MOCK - Changing monochromator conf to " + confValue);
            } else {
                configureCmd.execute();
                ExecutionTracerService.trace(this, "Changing monochromator conf to " + confValue);
            }
            response.addOutputMessage(0, output, PasserelleUtil.createCopyMessage(this, message));

        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEnumDeviceName() {
        return enumDeviceName;
    }

    public void setEnumDeviceName(final String enumDeviceName) {
        this.enumDeviceName = enumDeviceName;
    }
}
