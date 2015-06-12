package fr.soleil.passerelle.actor;

import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.v3.Actor;
import com.isencia.passerelle.core.Port;

@SuppressWarnings("serial")
public abstract class ActorV3 extends Actor{

    public ActorV3(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();
    }
    
    @Override
    public List<Port> outputPortList() {
        return super.outputPortList();
    }

}
