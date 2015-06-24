package fr.soleil.passerelle.testUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import junit.framework.Assert;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.FlowNotExecutingException;

import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.domain.RecordingDirector;

public class FlowHelperForTests {

    public static FlowManager executeBlockingError(final Flow flow, final Map<String, String> props) {
        final FlowManager flowMgr = new FlowManager();
        try {
            flowMgr.executeBlockingErrorLocally(flow, props);
        } catch (final FlowAlreadyExecutingException e) {
            e.printStackTrace();
            // Assert.fail("flow is already executing " + e.getMessage());
        } catch (final PasserelleException e) {
            e.printStackTrace();
            // Assert.fail("impossible to execute flow " + e.getMessage());
        }
        return flowMgr;
    }

    public static FlowManager executeNonBlocking(final Flow flow, final Map<String, String> props) {
        final FlowManager flowMgr = new FlowManager();
        try {
            flowMgr.execute(flow, props);
        } catch (final FlowAlreadyExecutingException e) {
            e.printStackTrace();
            Assert.fail("flow is already executing " + e.getMessage());
        } catch (final PasserelleException e) {
            e.printStackTrace();
            Assert.fail("impossible to execute flow " + e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail("impossible to execute flow " + e.getMessage());
        }
        return flowMgr;
    }

    public static Flow loadMoml(final Class<?> clazz, final String momlPath) {
        final Reader in = new InputStreamReader(clazz.getClass().getResourceAsStream(momlPath));
        Flow flow = null;
        try {
            flow = FlowManager.readMoml(in);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail(momlPath + " not found");
        }
        return flow;
    }

    public static BasicDirector setBasicDirector(final Flow flow, final String name) {
        BasicDirector dir = null;
        try {
            dir = new BasicDirector(flow, name);
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail("impossible to create director " + e.getMessage());
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail("impossible to create director " + e.getMessage());
        }
        try {
            flow.setDirector(dir);
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail("impossible set director on flow " + e.getMessage());
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail("impossible set director on flow " + e.getMessage());
        }
        return dir;
    }

    public static void setProperties(final Class clazz) {
        System.out.println("SET PROPERTIES");
        // System.setProperty("TANGO_HOST", "tangodb:20001,tangodb:20002");
        System.setProperty("TANGO_HOST", "calypso:20001");
        final String logFile = clazz.getClass().getResource(Constants.LOG4J_FILE).toString();
        System.out.println(logFile);
        System.setProperty("log4j.configuration", logFile);
    }

    public static RecordingDirector setRecodingDirector(final Flow flow, final String name) {
        RecordingDirector dir = null;
        try {
            dir = new RecordingDirector(flow, name);
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail("impossible to create director " + e.getMessage());
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail("impossible to create director " + e.getMessage());
        }
        try {
            flow.setDirector(dir);
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail("impossible set director on flow " + e.getMessage());
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail("impossible set director on flow " + e.getMessage());
        }
        return dir;
    }

    public static void stopExecution(final FlowManager manager, final Flow flow) {
        try {
            manager.stopExecution(flow);
        } catch (final FlowNotExecutingException e) {
            e.printStackTrace();
            Assert.fail("flow was not executing " + e.getMessage());
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            Assert.fail("flow was not executing " + e.getMessage());
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            Assert.fail("flow was not executing " + e.getMessage());
        } catch (final PasserelleException e) {
            e.printStackTrace();
            Assert.fail("flow was not executing " + e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail("flow was not executing " + e.getMessage());
        }
    }

}
