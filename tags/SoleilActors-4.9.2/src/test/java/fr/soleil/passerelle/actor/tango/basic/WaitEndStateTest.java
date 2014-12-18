package fr.soleil.passerelle.actor.tango.basic;

import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

/**
 * Created with IntelliJ IDEA. User: GRAMER Date: 13/03/13 Time: 14:38 To change this template use
 * File | Settings | File Templates.
 */
public class WaitEndStateTest {

    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "WaitEndState.moml");

    @BeforeMethod
    public void setUp() throws Exception {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test
    public void test() throws PasserelleException {

        final HashMap<String, String> props = new HashMap<String, String>();
        // props.put("WaitEndState" + "." + WaitEndState.STATE_TO_WAIT, "MOVING");
        // execute sequence

        moml.executeBlockingErrorLocally(props);
    }
}
