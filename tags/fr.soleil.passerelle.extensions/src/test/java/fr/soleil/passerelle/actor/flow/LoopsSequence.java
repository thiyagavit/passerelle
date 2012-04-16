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

public class LoopsSequence {

    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;
    private Flow topLevel;

    @Test
    public void test1() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/loops.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
