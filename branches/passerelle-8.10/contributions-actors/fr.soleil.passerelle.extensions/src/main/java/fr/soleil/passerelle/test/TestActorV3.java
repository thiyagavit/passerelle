package fr.soleil.passerelle.test;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;

import fr.soleil.passerelle.actor.ActorV3;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class TestActorV3 extends ActorV3 {

    public Port input;
    public Port output;

    public TestActorV3(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        input = PortFactory.getInstance().createInputPort(this, "input", PortMode.PULL, null);
        output = PortFactory.getInstance().createOutputPort(this, "output");
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        System.out.println("process");
        response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());
    }

}
