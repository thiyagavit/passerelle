package fr.soleil.passerelle.actor.tango.archiving;

import java.util.Date;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedProcessingException;

@SuppressWarnings("serial")
public class NewestValueExtractor extends AArchivingExtractor {

  public NewestValueExtractor(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    commandNameParam.setExpression("GetNewestValue");
  }

  @Override
  protected void process(final ActorContext ctx, final ProcessRequest request, final ProcessResponse response) throws ProcessingException {

    try {
      final String[] attributeNames = getAttributeNames();
      for (final String attributeName : attributeNames) {
        final String result;
        if (isMockMode()) {
          result = getTangoCommand().execute(String.class, "21534854;10");
        } else {
          result = getTangoCommand().execute(String.class, attributeName);
        }
        final String[] spitted = result.split(";");
        if (spitted.length != 2) {
          throw new ProcessingException("extraction must contains a ; " + result, getDeviceName(), null);
        }
        final Date timestamp = new Date(Long.parseLong(spitted[0]));
        final String value = spitted[1];
//        final Event evt = new ResultItemImpl(attributeName, value, null, timestamp);
//        ExecutionTracerService.trace(this, attributeName + " extraction is : [timestamp= " + DateUtils.format(timestamp, "yyyy/MM/dd HH:mm:ss") + ",value "
//            + value + "]");
//        final ManagedMessage message = createMessage(evt, ManagedMessage.objectContentType);
//        response.addOutputMessage(0, output, message);
      }
    } catch (final DevFailed e) {
      throw new DevFailedProcessingException(e, this);
    } catch (final PasserelleException e) {
      throw new ProcessingException(e.getMessage(), getDeviceName(), e);
    }

  }
}
