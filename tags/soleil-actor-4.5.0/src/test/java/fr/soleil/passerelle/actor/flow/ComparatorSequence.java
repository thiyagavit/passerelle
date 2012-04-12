package fr.soleil.passerelle.actor.flow;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ptolemy.kernel.ComponentEntity;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;

public class ComparatorSequence {
    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;
    private Flow topLevel;

    @Test
    public void testBoolean() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/comparator.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasicBoolean");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasicBoolean.Mock Mode", "true");
        props.put("Constant.value", "1");
        props.put("Constant2.value", "true");
        props.put("Comparator.comparison", "==");
        props.put("ComparatorWithConst.comparison", "==");
        props.put("ComparatorWithConst.right value", "true");

        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }

    @Test
    public void testDouble() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/comparator.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasicDouble");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasicDouble.Mock Mode", "true");
        props.put("Constant.value", "2");
        props.put("Constant2.value", "3.4");
        props.put("Comparator.comparison", "==");
        props.put("ComparatorWithConst.comparison", "==");
        props.put("ComparatorWithConst.right value", "3.4");

        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }

    @Test
    public void testString() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/comparator.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasicString");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasicString.Mock Mode", "true");
        props.put("Constant.value", "test");
        props.put("Constant2.value", "toto");
        props.put("Comparator.comparison", "!=");
        props.put("ComparatorWithConst.comparison", "==");
        props.put("ComparatorWithConst.right value", "toto");

        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
