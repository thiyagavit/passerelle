package fr.soleil.salsa.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

import org.testng.annotations.Test;

import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.core.Device;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.salsa.model.SalsaModel;
import fr.soleil.salsa.model.ScanServer;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.salsa.model.scanmanagement.ScanManager;

public class ScanTest {

    @Test(enabled = false)
    public void testfirst() throws Exception {
        ScanConfiguration config = null;
        SalsaModel model = null;
        ScanServer scanServer = null;
        Device dev;

        final File file = new File(getClass().getResource(Constants.SALSA_PATH + "test.salsa")
                .toURI());

        // load the config from the salsa file
        config = ScanManager.loadScan(file);

        model = new SalsaModel();

        config.setScanNumber(1);
        scanServer = model.getScanServer();
        dev = scanServer.getDevice();

        assertThat(dev).isNotNull();

        // scanServer.setActualScanConfig(config);
        model.getScanManager().setCurrentScan(config);
        scanServer.setRecordingSessionManaged(false);

        scanServer.start();
        DevState state = null;
        do {
            state = dev.state();
            Thread.sleep(1000);
        } while (state == DevState.MOVING);

    }
}
