package fr.soleil.passerelle.actor.snapshot;

import static org.testng.AssertJUnit.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ptolemy.kernel.ComponentEntity;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.testUtils.Constants;

public class SnapSequence {

    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;
    private Flow topLevel;

    @Test
    public void testMock() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "snap.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasicMock");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasicMock.Mock Mode", "true");
        props.put("Constant.value", "3");
        props.put("LaunchSnapShot.Device Name", "archiving/snap/snaparchiver.1");
        props.put("EquipementsSetter.Device Name", "archiving/snap/snapmanager.1");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }

    @Test
    public void testRead() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                Constants.SEQUENCES_PATH + "snap.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("Constant.value", "6");
        props.put("LaunchSnapShot.Device Name", "archiving/snap/snaparchiver.1");
        props.put("EquipementsSetter.Device Name", "archiving/snap/snapmanager.1");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
