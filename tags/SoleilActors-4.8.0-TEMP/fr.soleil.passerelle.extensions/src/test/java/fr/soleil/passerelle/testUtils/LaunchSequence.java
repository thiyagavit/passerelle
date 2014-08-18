package fr.soleil.passerelle.testUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.isencia.passerelle.model.Flow;

public class LaunchSequence {

    @DataProvider(name = "provider")
    public static Object[][] getParametres() {

        return new Object[][] { { Constants.SEQUENCES_PATH + "automaticConversions.moml" },
                { Constants.SEQUENCES_PATH + "WriterWait.moml" } };
    }

    @BeforeClass
    public static void setUp() {
        FlowHelperForTests.setProperties(LaunchSequence.class);
    }

    @Test(dataProvider = "provider")
    public void execute(final String sequencePath) {
        final Flow topLevel = FlowHelperForTests.loadMoml(this.getClass(), sequencePath);
        FlowHelperForTests.executeBlockingError(topLevel, null);
    }

}
