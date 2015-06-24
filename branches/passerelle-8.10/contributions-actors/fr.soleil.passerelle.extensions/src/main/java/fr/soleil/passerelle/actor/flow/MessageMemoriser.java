package fr.soleil.passerelle.actor.flow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class MessageMemoriser extends Transformer {

    private static Map<String, ManagedMessage> memorizedMessages = new ConcurrentHashMap<String, ManagedMessage>();
    public Parameter messageNameParam;
    private String messageName = "myvalue";

    public Parameter doMemorizationParam;
    private boolean doMemorization = true;

    public MessageMemoriser(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        messageNameParam = new StringParameter(this, "Message Name");
        messageNameParam.setExpression(messageName);

        doMemorizationParam = new StringParameter(this, "Memorization");
        doMemorizationParam.addChoice("do memorization");
        doMemorizationParam.addChoice("output value");
        doMemorizationParam.setExpression("do memorization");
    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {
        if (doMemorization) {
            ExecutionTracerService.trace(this, "Memorizing value in " + messageName);
            memorizedMessages.put(messageName, message);
            // System.out.println(memorizedMessages);
            // just output a copy of the input
            sendOutputMsg(output, PasserelleUtil.createCopyMessage(this, message));
        } else { // output previoulsy saved message
            ExecutionTracerService.trace(this, "Outputting value " + messageName);
            final ManagedMessage memorizedMessage = memorizedMessages.get(messageName);
            if (memorizedMessage == null) { // message not found
                ExceptionUtil.throwProcessingException("memorized message not found", messageName);
            }
            sendOutputMsg(output, PasserelleUtil.createCopyMessage(this, memorizedMessage));
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == messageNameParam) {
            messageName = ((StringToken) messageNameParam.getToken()).stringValue();
            if (messageName.isEmpty()) {
                throw new IllegalActionException("Message Name can not be empty");
            }
        } else if (attribute == doMemorizationParam) {
            final String doMemorizationS = ((StringToken) doMemorizationParam.getToken())
                    .stringValue();
            if (doMemorizationS.equals("do memorization")) {
                doMemorization = true;
            } else {
                doMemorization = false;
            }
        }
        super.attributeChanged(attribute);
    }

}
