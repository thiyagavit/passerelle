package fr.soleil.salsa.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.core.Device;
import fr.soleil.salsa.model.SalsaModel;
import fr.soleil.salsa.model.ScanServer;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.salsa.model.scanmanagement.ScanManager;

public class ScanTest{
	
	@Test
	public void testfirst() throws Exception {
		ScanConfiguration config = null;
		SalsaModel model= null;
		ScanServer scanServer = null;
		Device dev;
		File file = new File("test/fr/soleil/passerelle/resources/test.salsa");
	
			// load the config from the salsa file
		config = ScanManager.loadScan(file);
		
		
		model = new SalsaModel();
		
		config.setScanNumber(1);
		scanServer = model.getScanServer();
		dev = scanServer.getDevice();
		if(dev == null){
			Assert.fail();
		}
		//scanServer.setActualScanConfig(config);
		model.getScanManager().setCurrentScan(config);
		scanServer.setRecordingSessionManaged(false);
		
		scanServer.start();
		DevState state = null;
		do{
			state = dev.state();
			Thread.sleep(1000);
		}while(state == DevState.MOVING);
		
	}
}
