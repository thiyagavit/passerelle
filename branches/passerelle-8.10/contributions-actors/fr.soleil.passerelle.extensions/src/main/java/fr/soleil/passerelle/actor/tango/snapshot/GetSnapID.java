package fr.soleil.passerelle.actor.tango.snapshot;

import java.net.URL;
import java.util.Arrays;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class GetSnapID extends Transformer {

    // TODO: set parameters for seach critieras that can return only one snapID
    private SnapExtractorProxy extractor;
    public Parameter contextIDParam;
    private String contextID;

    /**
     * Syntax for filter: <br>
     * "id_snap > | < | = | <= | >= nbr",<br>
     * "time < | > | >= | <=  yyyy-mm-dd hh:mm:ss | dd-mm-yyyy hh:mm:ss",<br>
     * "comment starts | ends | contains string", first | last<br>
     */
    public Parameter searchfilterParam;
    private String searchfilter;

    public GetSnapID(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        contextIDParam = new StringParameter(this, "Context ID");
        contextIDParam.setExpression("1");

        searchfilterParam = new StringParameter(this, "Search Filter");
        searchfilterParam.setExpression("id_snap=1|time<2030-03-09 17:45:30| comment contains test");

        final URL url = this.getClass().getResource(
                "/org/tango-project/tango-icon-theme/32x32/devices/camera-photo.png");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:orange;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + " <image x=\"-15\" y=\"-15\" width =\"32\" height=\"32\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                extractor = new SnapExtractorProxy();
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - snap ID found " + 1);
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, 1));
        } else {
            try {
                final String[] snapIDs = extractor.getSnapIDs(contextID, searchfilter);
                if (snapIDs.length >= 1) {
                    ExecutionTracerService.trace(this, "snap ID found " + Arrays.toString(snapIDs)
                            + " - using " + snapIDs[0]);
                    sendOutputMsg(output, PasserelleUtil.createContentMessage(this, snapIDs[0]));
                } else {
                    ExceptionUtil.throwProcessingException("snap id not found", searchfilter);
                }
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException(this, e);
            }
        }
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == contextIDParam) {
            contextID = PasserelleUtil.getParameterValue(contextIDParam);
        } else if (attribute == searchfilterParam) {
            searchfilter = PasserelleUtil.getParameterValue(searchfilterParam);
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
