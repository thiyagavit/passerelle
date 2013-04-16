package fr.soleil.passerelle.actor.acquisition;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.testng.AssertJUnit.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.RecordingDirector;
import fr.soleil.passerelle.testUtils.Constants;

public class Storage {
    private FlowManager flowMgr;
    private Reader in;

    @Test
    public void testAsyncRecording() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "storage.moml"));
        flowMgr = new FlowManager();
        final Flow topLevel = FlowManager.readMoml(in);
        final RecordingDirector dir = new RecordingDirector(topLevel, "DirRecording");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirRecording.Mock Mode", "false");
        props.put("DirRecording.Asynchronous Recording", "true");

        final FlowManager flowMgr = new FlowManager();

        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<Actor> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }

    @Test
    public void testSyncRecording() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "storage.moml"));
        flowMgr = new FlowManager();
        final Flow topLevel = FlowManager.readMoml(in);
        final RecordingDirector dir = new RecordingDirector(topLevel, "DirRecording");
        // dir.setRecorderName("test/stockage/dr.1");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirRecording.Mock Mode", "false");
        props.put("DirRecording.Asynchronous Recording", "false");

        final FlowManager flowMgr = new FlowManager();

        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<Actor> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertThat(element.isFinishRequested()).isTrue();
        }
    }

}
