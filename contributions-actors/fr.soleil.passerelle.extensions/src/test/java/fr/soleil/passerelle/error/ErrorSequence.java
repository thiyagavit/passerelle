package fr.soleil.passerelle.error;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.testUtils.Constants;

public class ErrorSequence {

    private Director dir;
    private FlowManager flowMgr;
    private Reader in;
    private Flow topLevel;

    @Test(expectedExceptions = PasserelleException.class)
    public void testFireFatal() throws Exception {

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "error.moml"));
        flowMgr = new FlowManager();
        try {
            topLevel = FlowManager.readMoml(in);
        } catch (final Exception e) {
            AssertJUnit.fail(e.getMessage());
            e.printStackTrace();
        }
        dir = new BasicDirector(topLevel, "DirFireFatal");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirFireFatal.Mock Mode", "true");
        props.put("ErrorGenerator.error type", "fire");
        props.put("ErrorGenerator.severity", "FATAL");
        flowMgr.executeBlockingErrorLocally(topLevel, props);
    }

    @Test(expectedExceptions = PasserelleException.class)
    public void testFireNonFatal() throws Exception {

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "error.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirFireNonFatal");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirFireNonFatal.Mock Mode", "true");
        props.put("DirFireNonFatal.Error Control", "retry");
        props.put("ErrorGenerator.error type", "fire");
        props.put("ErrorGenerator.severity", "NON_FATAL");
        flowMgr.executeBlockingErrorLocally(topLevel, props);
    }

    @Test(expectedExceptions = PasserelleException.class)
    public void testInitFatal() throws FlowAlreadyExecutingException, PasserelleException,
            IllegalActionException, NameDuplicationException {

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "error.moml"));
        flowMgr = new FlowManager();
        try {
            topLevel = FlowManager.readMoml(in);
        } catch (final Exception e) {
            AssertJUnit.fail(e.getMessage());
            e.printStackTrace();
        }
        dir = new BasicDirector(topLevel, "DirInitFatal");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirInitFatal.Mock Mode", "true");
        props.put("ErrorGenerator.error type", "init");
        props.put("ErrorGenerator.severity", "FATAL");
        try {
            flowMgr.executeBlockingErrorLocally(topLevel, props);
        } catch (final Throwable t) {

        }
    }

    @Test(expectedExceptions = PasserelleException.class)
    public void testInitNonFatal() throws Exception {

        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "error.moml"));
        flowMgr = new FlowManager();
        try {
            topLevel = FlowManager.readMoml(in);
        } catch (final Exception e) {
            AssertJUnit.fail(e.getMessage());
            e.printStackTrace();
        }
        dir = new BasicDirector(topLevel, "DirInitNonFatal");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirInitNonFatal.Mock Mode", "true");
        props.put("ErrorGenerator.error type", "init");
        props.put("ErrorGenerator.severity", "NON_FATAL");
        flowMgr.executeBlockingErrorLocally(topLevel, props);
    }
}
