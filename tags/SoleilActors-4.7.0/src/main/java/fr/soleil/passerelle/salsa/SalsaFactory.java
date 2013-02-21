package fr.soleil.passerelle.salsa;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.isencia.passerelle.actor.Actor;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.soleil.salsa.SalsaPasserelleBean;
import fr.soleil.salsa.model.SalsaModel;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.salsa.model.scanmanagement.ScanManager;

public class SalsaFactory {

	private static SalsaFactory instance = new SalsaFactory();
	private SalsaModel defautModel;
	private SalsaModel model;
	// private final Map<File, ScanConfiguration> salsaConfigs = new
	// HashMap<File, ScanConfiguration>();

	private final Map<Actor, SalsaPasserelleBean> salsaBeanMap = new HashMap<Actor, SalsaPasserelleBean>();

	private SalsaFactory() {
	}

	public static SalsaFactory getInstance() {
		return instance;
	}

	public synchronized SalsaModel createSalsaModel() throws DevFailed,
			ConnectionException {
		if (model == null) {
			model = new SalsaModel(false);
		}
		return model;
	}

	public synchronized SalsaModel createDefaultSalsaModel() {
		if (defautModel == null) {
			defautModel = new SalsaModel();
		}
		return defautModel;
	}

	public synchronized SalsaPasserelleBean loadSalsaBean(final Actor actor) {
		SalsaPasserelleBean salsaBean = null;

		defautModel = new SalsaModel();

		if (!salsaBeanMap.containsKey(actor)) {
			salsaBean = new SalsaPasserelleBean(defautModel);
			salsaBeanMap.put(actor, salsaBean);
		} else {
			salsaBean = salsaBeanMap.get(actor);
		}
		return salsaBean;
	}

	public synchronized ScanConfiguration loadConfig(final File file)
			throws IOException {
		// reverse: if salsa file is changed between two executions
		// in HMI, the actor will no see file changes.
		ScanConfiguration config = null;
		// if(!salsaConfigs.containsKey(file)) {
		config = ScanManager.loadScan(file);
		// salsaConfigs.put(file, config);
		/*
		 * }else { config = salsaConfigs.get(file); }
		 */
		return config;
	}

	public synchronized void clearAll() {
		model = null;
		// salsaConfigs.clear();
	}

}
