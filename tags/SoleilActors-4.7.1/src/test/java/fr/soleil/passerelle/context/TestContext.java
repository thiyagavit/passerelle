/*package fr.soleil.passerelle.context;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ptolemy.actor.Manager;

import be.isencia.passerelle.actor.Actor;
import be.isencia.passerelle.model.Flow;
import be.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.tangounit.DeviceManager;
import fr.soleil.tangounit.TangoUnitFactory;

public class TestContext {

	static {
		System.setProperty("be.isencia.home", "D:\\temp\\test\\passerelle");
		System.setProperty("log4j.configuration", "file:D:\\temp\\test\\passerelle\\conf\\log4j.properties");
	}
	private Reader in;
	private Flow topLevel; 
	private FlowManager flowMgr; 
	
	@Test
	public void contextPauseAndDeviceAlive() throws Exception {
	
		//1 - start tangotest
		//2- create a BL status with properties
		try {
		DeviceManager tangotest = TangoUnitFactory.getInstance().createDeviceManager("TangoUnit/tangotest/1","TangoTest", "TangoTest/TangoUnit");
		DeviceManager beamlineStatus =TangoUnitFactory.getInstance().createDeviceManager("TangoUnit/beamlinestatus/1","BeamlineStatus","BeamlineStatus/TangoUnit");
		
		tangotest.createDevice();	
		tangotest.startServer(10000);
		System.out.println("tangotest started");
				
		beamlineStatus.createDevice();
		beamlineStatus.createDeviceProperty("contextCondition", "test1>100");
		//beamlineStatus.createDeviceProperty("contextConditionList", "context1:test1=100");
		beamlineStatus.createDeviceProperty("contextVariables", "test1:TangoUnit/tangotest/1/double_scalar_w");
		
		beamlineStatus.startServer(10000);
		System.out.println("beamlineStatus started");
		
		DeviceProxy devTangoTest = tangotest.getDeviceProxy();
		DeviceAttribute devattr = devTangoTest.read_attribute("double_scalar_w");
		devattr.insert(300.0);
		devTangoTest.write_attribute(devattr);
		
		ContextEventListener.setDeviceName("TangoUnit/beamlinestatus/1");
		//3- Launch sequence
		in = new InputStreamReader(getClass().getResourceAsStream("/fr/soleil/passerelle/resources/context.moml"));
		flowMgr = new FlowManager();
		topLevel = FlowManager.readMoml(in);
		Map<String, String> props = new HashMap<String, String>();
		props.put("Context.Context name", ""); //defaut context
		props.put("Context.Context strategy", "pause");
		props.put("SimpleLoop.Number of Loops", "10");
	
		flowMgr.executeNonBlocking(topLevel, props);
		//wait sequence is started
		System.out.println("wait sequence is started");
		while(flowMgr.getExecutionState(topLevel) != Manager.ITERATING) {
			Thread.sleep(1000);
		}
		
		System.out.println("setting an invalid context");
		devattr.insert(10.0);
		devTangoTest.write_attribute(devattr);
		
		Thread.sleep(3000);
		
		System.out.println("setting a valid context");
		devattr.insert(300.0);
		devTangoTest.write_attribute(devattr);
		
		//wait end sequence
		System.out.println("wait end sequence");
		while(flowMgr.getExecutionState(topLevel) != Manager.IDLE) {
			Thread.sleep(1000);
		}		
		
				
		}catch(DevFailed e) {
			//TODO: kill and delete devices correcty
			Except.print_exception(e);
			throw e;
		}finally {
			// kill and delete all devices
			TangoUnitFactory.getInstance().clearDeviceManagers();
		}
		
	}
	
	@Ignore
	public void contextPauseAndDeviceStop(){
		
	}
	
	
	@Ignore
	public void contextStopAndDeviceAlive(){
		
	}
	
	@Ignore
	public void contextStopAndDeviceStop(){
		
	}
	
	@Ignore
	public void contextIgnore(){
		
	}
}
 */