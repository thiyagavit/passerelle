package fr.soleil.passerelle.actor.acquisition;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.actor.tango.acquisition.Scan;
import fr.soleil.passerelle.actor.tango.recording.EndRecording;
import fr.soleil.passerelle.actor.tango.recording.StartRecording;
import fr.soleil.passerelle.domain.RecordingDirector;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;

@RunWith(Parameterized.class)
public class ScanSequence {

    // TODO: start a scan server automatically
    @Parameters
    public static List<Object[]> getParametres() {
        return Arrays.asList(new Object[][] { { "DirMock", true }, { "DirTango", false } });
    }

    private final String dirName;
    Map<String, String> parameters = new HashMap<String, String>();

    public ScanSequence(final String dirName, final boolean mockMode) {
        this.dirName = dirName;
        parameters.clear();
        parameters.put(dirName + ".Mock Mode", Boolean.toString(mockMode));
        parameters.put("ScanWithSalsa.Scan Config",
                getClass().getResource(Constants.SALSA_PATH + "1DScan.salsa").getFile());
    }

    @BeforeClass
    public static void setProperties() {
        FlowHelperForTests.setProperties(ScanSequence.class);
    }

    @Test(timeout = 20000)
    public void testOneScan() {
        final Flow flow = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + "scan.moml");

        FlowHelperForTests.setBasicDirector(flow, dirName);
        FlowHelperForTests.executeBlockingError(flow, parameters);

        final List<ComponentEntity> list = flow.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }

    @Test(timeout = 20000)
    public void testWithStop() {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$testRealStop IN$$$$$$$$$$$$$$$$$$$$$$$$$$");
        final Flow flow = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + " scan.moml");

        FlowHelperForTests.setBasicDirector(flow, dirName);

        System.out.println("=== start scan 1====");
        final FlowManager flowMgr = FlowHelperForTests.executeNonBlocking(flow, parameters);

        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("=== STOP scan ====");
        FlowHelperForTests.stopExecution(flowMgr, flow);
        final List<ComponentEntity> list = flow.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.getFullName(), element.isFinishRequested());
        }
        System.out.println("=== start scan 2====");
        FlowHelperForTests.executeBlockingError(flow, parameters);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$testRealStop OUT$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    @Test(timeout = 20000)
    public void testRecording() {
        final Flow flow = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + "scan.moml");
        final RecordingDirector dir = FlowHelperForTests.setRecodingDirector(flow, dirName);
        // dir.setRecorderName("test/stockage/dr.1");

        flow.removeAllRelations();
        final List entityList = flow.entityList();
        final List<Actor> listActors = entityList;
        StartRecording startRecording = null;
        EndRecording endRecording = null;
        try {
            startRecording = new StartRecording(flow, "StartRecording");
            endRecording = new EndRecording(flow, "EndRecording");
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Scan scan = null;
        for (final Object element2 : listActors) {
            final Actor element = (Actor) element2;
            if (element instanceof Scan) {
                scan = (Scan) element;
            }
        }
        try {
            final Port p1 = (Port) startRecording.getPort("output");
            final Port p2 = (Port) scan.getPort("Trigger");
            flow.connect(p1, p2, "r1");

            final Port p3 = (Port) endRecording.getPort("input");
            final Port p4 = (Port) scan.getPort("TriggerOut");
            flow.connect(p3, p4, "r2");
        } catch (final IllegalActionException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (final NameDuplicationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        parameters.put(dirName + ".Error Control", "retry");

        FlowHelperForTests.executeBlockingError(flow, parameters);

        System.out.println("dr " + dir.getDataRecorderName());
        final List<Actor> list = entityList;
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
