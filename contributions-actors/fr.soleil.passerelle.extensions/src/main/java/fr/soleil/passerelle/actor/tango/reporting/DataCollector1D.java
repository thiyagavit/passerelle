package fr.soleil.passerelle.actor.tango.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.tango.ATangoAttributeActor;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * Collect data and assemble it to publish it on a Tango device of class
 * Publisher. <br>
 * For scalar data, the result will be a spectrum . <br>
 * For spectrum data, it is not yet available <br>
 * For image data, it is not yet available
 * 
 * @author ABEILLE
 * 
 */
@SuppressWarnings("serial")
public class DataCollector1D extends ATangoAttributeActor implements IActorFinalizer {

    private final static Logger logger = LoggerFactory.getLogger(DataCollector1D.class);

    private static class ListCollectorFlyWeight {
        private static final ListCollectorFlyWeight instance = new ListCollectorFlyWeight();
        private static final Map<String, List<String>> createdLists = new HashMap<String, List<String>>();
        private static boolean hasBeenCleared = false;

        private ListCollectorFlyWeight() {
        }

        public static ListCollectorFlyWeight getInstance() {
            return instance;
        }

        public synchronized List<String> createList(final String attributeName) {
            hasBeenCleared = false;
            if (createdLists.containsKey(attributeName)) {
                return createdLists.get(attributeName);
            } else {
                final List<String> newList = Collections.synchronizedList(new ArrayList<String>());
                // add attribute name to command argin
                newList.add(0, attributeName);
                createdLists.put(attributeName, newList);
                return newList;
            }
        }

        public synchronized void clearAll() {
            if (!hasBeenCleared) {
                for (final Map.Entry<String, List<String>> list : createdLists.entrySet()) {
                    list.getValue().clear();
                    // add attribute name to command argin
                    list.getValue().add(0, list.getKey());
                    logger.debug("clear " + list.getKey());
                }
            }
            hasBeenCleared = true;
        }
    }

    private List<String> values;
    // #18764
    private TangoAttribute publishSpectrumAttr;

    public DataCollector1D(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        input.setExpectedMessageContentType(String.class);
        recordDataParam.setVisibility(Settable.EXPERT);
    }

    @Override
    protected void doInitialize() throws InitializationException {

        super.doInitialize();
        final Director dir = getDirector();
        if (dir instanceof BasicDirector) {
            ((BasicDirector) dir).registerFinalizer(this);
        }
        try {
            final String attrName = getTangoAttribute().getAttributeProxy().name();
            values = ListCollectorFlyWeight.getInstance().createList(attrName);
            publishSpectrumAttr = getTangoAttribute();

        } catch (final PasserelleException e) {
            ExceptionUtil.throwInitializationException(e.getMessage(), this, e);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        final ManagedMessage message = request.getMessage(input);
        final String value = (String) PasserelleUtil.getInputValue(message);
        values.add(value);
        try {
            // publish data with a tango command
            List<String> newVal = new ArrayList<String>();
            newVal.addAll(values);
            // Attribute Name suppression
            newVal.remove(0);
            Object[] argin = newVal.toArray(new String[] {});
            publishSpectrumAttr.writeSpectrum(argin);
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

    @Override
    public void doFinalAction() {
        // clear list in case of executing the containing model several times
        ListCollectorFlyWeight.getInstance().clearAll();

    }

}
