package fr.soleil.passerelle.actor.acquisition;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ptolemy.kernel.ComponentEntity;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.model.Flow;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.FlowHelperForTests;
import fr.soleil.tangounit.junit.TangoUnitTest;

@RunWith(Parameterized.class)
public class PreDefinedScanSequence extends TangoUnitTest {

    // TODO: start a scan server automatically
    @Parameters
    public static List<Object[]> getParametres() {
        return Arrays.asList(new Object[][] { { "DirMock", true }, { "DirTango", false } });
    }

    private final String dirName;
    Map<String, String> parameters = new HashMap<String, String>();

    public PreDefinedScanSequence(final String dirName, final boolean mockMode) {
        this.dirName = dirName;
        parameters.clear();
        parameters.put(dirName + ".Mock Mode", "false");
        parameters.put("PreConfiguredScan.Scan Config",
                getClass().getResource(Constants.SALSA_PATH + "1DScan.salsa").getFile());
        parameters.put("From.value", "0");
        parameters.put("To.value", "60");
        parameters.put("NbSteps.value", "5");
        parameters.put("IntegrationTime.value", "0.750");
    }

    @BeforeClass
    public static void setProperties() {
        FlowHelperForTests.setProperties(PreDefinedScanSequence.class);
    }

    @Test(timeout = 20000)
    public void test() throws Exception {

        final Flow flow = FlowHelperForTests.loadMoml(this.getClass(), Constants.SEQUENCES_PATH
                + "predefinedScan.moml");

        FlowHelperForTests.setBasicDirector(flow, dirName);
        FlowHelperForTests.executeBlockingError(flow, parameters);

        final List<ComponentEntity> list = flow.entityList();
        for (final Object element2 : list) {
            final Actor element = (Actor) element2;
            assertTrue(element.isFinishRequested());
        }
    }
}
