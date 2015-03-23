package fr.soleil.passerelle.actor.tango.basic;

import java.net.URL;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TriggeredSource;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

/**
 * Set the Tango client timeout for all Tango devices implied in this sequence.
 * 
 * @author ABEILLE
 * 
 */
@SuppressWarnings("serial")
public class SetTimeout extends TriggeredSource {

    private static final String TIMEOUT = "Timeout";

    /**
     * The timeout in ms
     */
    @ParameterName(name = TIMEOUT)
    public Parameter timeoutParam;
    private int timeout = 3000;
    private boolean messageSent = false;

    public SetTimeout(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {
        super(arg0, arg1);
        timeoutParam = new StringParameter(this, TIMEOUT);
        timeoutParam.setExpression("3000");

        final URL url = this.getClass().getResource("/fr/soleil/tango/tango.jpg");
        _attachText("_iconDescription", "<svg>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url + "\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"-20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"-20\" x2=\"20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"-19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"18\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "</svg>\n");

    }

    @Override
    protected void doInitialize() throws InitializationException {
        messageSent = false;
        super.doInitialize();
    }

    @Override
    protected ManagedMessage getMessage() throws ProcessingException {

        if (messageSent && !isTriggerConnected()) {
            return null;
        }

        messageSent = true;
        try {
            ExecutionTracerService.trace(this, "Setting timeout to " + timeout);
            ProxyFactory.getInstance().setTimout(timeout);
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }

        return PasserelleUtil.createTriggerMessage();
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == timeoutParam) {
            timeout = Integer.valueOf(((StringToken) timeoutParam.getToken()).stringValue());
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean mustWaitForTrigger() {
        return true;
    }

}
