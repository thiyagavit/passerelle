package fr.soleil.passerelle.actor.flow;

import javax.swing.JOptionPane;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;

import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.util.PasserelleUtil;

public class UserMessage extends TransformerV5 {

    /**
     * 
     */
    private static final long serialVersionUID = -6434067921345274404L;
    /**
     * 
     */

    public static final String MESSAGE_LABEL = "Message";
    public static final String TITLE_LABEL = "Title";
    
    /**
     * The message that must be shown to the user
     */
    @ParameterName(name = MESSAGE_LABEL)
    public Parameter messageParam;
    private String messageValue = "your question";
    
    /**
     * The title of the window
     */
    @ParameterName(name = TITLE_LABEL)
    public Parameter titleParam;
    private String titleValue = "";

    public Port cancelOutput;
    public Port noOutput;

    public UserMessage(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        messageParam = new StringParameter(this, MESSAGE_LABEL);
        messageParam.setExpression(messageValue);
        
        titleParam = new StringParameter(this, TITLE_LABEL);
        titleParam.setExpression(titleValue);

        output.setName("Yes");        
        noOutput = PortFactory.getInstance().createOutputPort(this, "No");
        cancelOutput = PortFactory.getInstance().createOutputPort(this, "Cancel");
    }

    @Override
    protected void validateInitialization() throws ValidationException {
        super.validateInitialization();
        try {
            messageValue = PasserelleUtil.getParameterValue(messageParam);
            if (messageValue.isEmpty()) {
                throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR,
                        MESSAGE_LABEL + " can not be empty", this, null);
            }            
            titleValue = PasserelleUtil.getParameterValue(titleParam);
        } catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, MESSAGE_LABEL + " or " +  TITLE_LABEL + " is not good", this, e);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        int result = JOptionPane.YES_OPTION;
        result = JOptionPane.showConfirmDialog(null, messageValue, titleValue, JOptionPane.YES_NO_CANCEL_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this,"yes"));
        } else if (result == JOptionPane.NO_OPTION){
            response.addOutputMessage(noOutput, PasserelleUtil.createContentMessage(this, "no"));
        }else {
            response.addOutputMessage(cancelOutput, PasserelleUtil.createContentMessage(this, "cancel"));
        }
    }
}
