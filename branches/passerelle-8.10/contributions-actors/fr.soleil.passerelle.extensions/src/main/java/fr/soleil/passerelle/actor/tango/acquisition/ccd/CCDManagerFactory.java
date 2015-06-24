package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.HashMap;
import java.util.Map;

import com.isencia.passerelle.actor.Actor;

public class CCDManagerFactory {

	private static CCDManagerFactory instance = new CCDManagerFactory();
	private Map<String, CCDManager> ccdManagersMap = new HashMap<String, CCDManager> ();
	
	public static CCDManagerFactory getInstance(){
		return instance;
	}
	
	public synchronized CCDManager createCCDManager(Actor actor, String deviceName){
		CCDManager man;
		if(!ccdManagersMap.containsKey(deviceName)){
			man = new CCDManager(actor, deviceName);
			ccdManagersMap.put(deviceName, man);
		}else{
			man = ccdManagersMap.get(deviceName);
		}
		return man;
	}
}
