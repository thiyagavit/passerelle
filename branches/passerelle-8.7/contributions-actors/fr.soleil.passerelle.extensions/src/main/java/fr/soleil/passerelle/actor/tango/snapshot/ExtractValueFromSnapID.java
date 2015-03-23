package fr.soleil.passerelle.actor.tango.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class ExtractValueFromSnapID extends ASnapExtractor {

    private final static Logger logger = LoggerFactory.getLogger(ExtractValueFromSnapID.class);

    public ExtractValueFromSnapID(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        input.setExpectedMessageContentType(Double.class);
        input.setName("SnapID");
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        final Double snapID = (Double) PasserelleUtil.getInputValue(arg0);
        final Long tmp = snapID.longValue();
        final String convertedSnapID = tmp.toString();
        logger.debug(convertedSnapID);
        try {
            super.setSnapID(convertedSnapID);
            getAndSendValues();
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return null;
    }

}
