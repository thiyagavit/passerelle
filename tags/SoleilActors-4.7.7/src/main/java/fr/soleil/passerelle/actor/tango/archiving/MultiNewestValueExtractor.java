package fr.soleil.passerelle.actor.tango.archiving;

import java.text.SimpleDateFormat;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.DevFailedProcessingException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public class MultiNewestValueExtractor extends AArchivingExtractor {

  public Parameter depthParam;
  private int depth;

  private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy H:m:s");

  public MultiNewestValueExtractor(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    commandNameParam.setExpression("ExtractBetweenDates");
    depthParam = new StringParameter(this, "depth (secs)");
    depthParam.setExpression("10");
  }

  @Override
  protected void process(final ActorContext ctx, final ProcessRequest request, final ProcessResponse response) throws ProcessingException {
    try {
      final String[] attributeNames = getAttributeNames();

      final TangoCommand command = getTangoCommand();
      for (final String attributeName : attributeNames) {

//        if (isMockMode()) {
//          for (int i = 0; i < 10; i++) {
//            final Date date = new Date(i * 1000);
//            final Event evt = new ResultItemImpl(attributeName, Integer.toString(i), null, date);
//            ExecutionTracerService.trace(this, attributeName + " extraction is : [timestamp= " + DateUtils.format(date, "yyyy/MM/dd HH:mm:ss") + ",value " + i
//                + "]");
//            final ManagedMessage message = createMessage(evt, ManagedMessage.objectContentType);
//            response.addOutputMessage(0, output, message);
//          }
//        } else {
//          final long now = System.currentTimeMillis();
//          final Date endDate = new Date(now);
//          final Date startDate = new Date(now - depth * 1000);
//          System.out.println(startDate);
//          System.out.println(endDate);
//          command.execute(attributeName, format.format(startDate), format.format(endDate));
//          final double[] timestamps = command.getNumDoubleMixArrayArgout();
//          final String[] values = command.getStringMixArrayArgout();
//          System.out.println(Arrays.toString(values));
//          for (int i = 0; i < values.length; i++) {
//            final Date date = new Date((long) timestamps[i]);
//            final Event evt = new ResultItemImpl(attributeName, values[i], null, date);
//            ExecutionTracerService.trace(this, attributeName + " extraction is : [timestamp= " + DateUtils.format(date, "yyyy/MM/dd HH:mm:ss") + ",value "
//                + values[i] + "]");
//            final ManagedMessage message = createMessage(evt, ManagedMessage.objectContentType);
//            response.addOutputMessage(0, output, message);
//          }
//        }
      }
//    } catch (final DevFailed e) {
//      throw new DevFailedProcessingException(e, this);
    } catch (final PasserelleException e) {
      throw new ProcessingException(e.getMessage(), getDeviceName(), e);
    }
  }

  @Override
  public void attributeChanged(final Attribute attr) throws IllegalActionException {
    if (attr == depthParam) {
      depth = PasserelleUtil.getParameterIntValue(depthParam);
    } else {
      super.attributeChanged(attr);
    }
  }
}
