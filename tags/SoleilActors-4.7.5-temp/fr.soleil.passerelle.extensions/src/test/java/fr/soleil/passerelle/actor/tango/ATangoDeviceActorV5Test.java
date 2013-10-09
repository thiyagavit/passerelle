package fr.soleil.passerelle.actor.tango;

import static fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5.DEVICE_NAME;
import static fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5.ERROR_DEVICE_NAME_EMPTY;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

public class ATangoDeviceActorV5Test {
    private Flow flow;
    private FlowManager flowMgr;
    private DummyTangoDeviceActorV5 actor;

    public class DummyTangoDeviceActorV5 extends ATangoDeviceActorV5 {
        public DummyTangoDeviceActorV5(CompositeEntity container, String name)
                throws NameDuplicationException, IllegalActionException {
            super(container, name);
        }

        @Override
        protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
                throws ProcessingException {
            // no need
        }
    }

    @BeforeMethod
    public void setUp() throws IllegalActionException, NameDuplicationException {
        flow = new Flow("unit test", null);
        flow.setDirector(new Director(flow, "director"));
        flowMgr = new FlowManager();

        actor = new DummyTangoDeviceActorV5(flow, "DummyActor");
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void when_deviceName_parameter_is_empty_before_start_then_an_excpetion_is_raised()
            throws IllegalActionException, NameDuplicationException {

        actor.deviceNameParam.setToken("");
    }

    @Test(expectedExceptions = PasserelleException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + ERROR_DEVICE_NAME_EMPTY + ".*")
    public void when_deviceName_parameter_is_empty_then_validateInitialization_raises_an_exception()
            throws PasserelleException {

        Map<String, String> props = new HashMap<String, String>();
        props.put("DummyActor." + DEVICE_NAME, "");
        flowMgr.executeBlockingErrorLocally(flow, props);

    }
}
