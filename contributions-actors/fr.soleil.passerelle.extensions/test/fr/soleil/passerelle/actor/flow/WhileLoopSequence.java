package fr.soleil.passerelle.actor.flow;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ptolemy.kernel.ComponentEntity;
import be.isencia.passerelle.actor.Actor;
import be.isencia.passerelle.model.Flow;
import be.isencia.passerelle.model.FlowManager;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.soleil.passerelle.domain.BasicDirector;

public class WhileLoopSequence {

    private BasicDirector dir;
    private FlowManager flowMgr;
    private Reader in;
    private Flow topLevel;

    @Test
    public void test1() throws Exception {
        in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/whileloop.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        dir = new BasicDirector(topLevel, "DirBasic");
        topLevel.setDirector(dir);

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");

        // while actor test changing value on tango/tangotest/1/boolean_scalar
        new Thread() {
            @Override
            public void run() {
                AttributeProxy attr;
                try {
                    attr = new AttributeProxy("tango/tangotest/1/boolean_scalar");
                    final DeviceAttribute da = attr.read();
                    da.insert(false);
                    attr.write(da);
                    System.out.println("waiting for 5 secs");
                    Thread.sleep(1000);
                    System.out.println("***************writing true");
                    da.insert(true);
                    attr.write(da);
                }
                catch (final DevFailed e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

        Thread.sleep(2);
        flowMgr.executeBlockingErrorLocally(topLevel, props);

        final List<ComponentEntity> list = topLevel.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
