package com.isencia.passerelle.workbench.model.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;

/**
 * Class replaces submodel storage with a proper properties file. This
 * allows reliable operations where the old comma separated string did not.
 * 
 * @author gerring
 *
 */
public class SubModelUtils {
	
	private static Logger logger = LoggerFactory.getLogger(SubModelUtils.class);
	
	private static Map<String, Flow> modelMap;

	public static Map<String, Flow> getSubModels() throws Exception {
		if (modelMap == null) {
			return modelMap = SubModelUtils.readSubModels();
		}
		return modelMap;
	}
	
	public static boolean isSubModel(final String name) {
		return modelMap!=null && modelMap.keySet().contains(name);
	}

	public static void addSubModel(Flow flow) throws Exception {
		MoMLParser.putActorClass(flow.getName(), flow);
		registerSubModel(flow);
	}

	private static void registerSubModel(Flow flow) throws Exception {
		
		if (modelMap == null) {
			modelMap = getSubModels();
		}
		String model = flow.getName();
		
		modelMap.put(model, flow);

		final IFile      store  = getModelStore();
		final Properties models = PropUtils.loadProperties(store.getContents());

		models.put(model, "");

		PropUtils.storeProperties(models, store.getLocation().toOSString());
		store.refreshLocal(IResource.DEPTH_ONE, null);

	}

	public static Map<String, Flow> readSubModels() throws Exception {
		
		final IProject pass = ModelUtils.getPasserelleProject();
		pass.refreshLocal(IResource.DEPTH_INFINITE, null);
		
		final Properties models = PropUtils.loadProperties(getModelStore().getContents());
				
		final Set<Object> sorted = new TreeSet<Object>();
		sorted.addAll(models.keySet());
		
		// Use map that retains order
		final Map<String, Flow> modelList = new LinkedHashMap<String, Flow>();
		
		for (Object modelOb : sorted) {

			final String modelName = (String)modelOb;
			if (modelName==null||"".equals(modelName)) continue;
			
			final IFile file = pass.getFile(modelName+".moml");
			try {
				if (file.exists()) {

					Flow flow = FlowManager.readMoml(new InputStreamReader(file.getContents()));
//					flow.setSource(file.getLocation().toOSString());
					if (flow.isClassDefinition()) {
						MoMLParser.putActorClass(modelName, flow);
						flow.setName(modelName);
						modelList.put(modelName, flow);
					}

				}

			} catch (Exception e1) {
				logger.error("Cannot read moml file!", e1);
			}
		}
		
		pass.refreshLocal(IResource.DEPTH_INFINITE, null);
		

	    return modelList;
	}

	private static IFile getModelStore() throws Exception {
		final IFile file = ModelUtils.getPasserelleProject().getFile("submodels.properties");
		if (!file.exists()) {
			file.create(new ByteArrayInputStream("# DAWB Properties".getBytes()), true, null);
		}
		return file;
	}

	public static void deleteSubModel(final String name)  throws Exception {
		
		if (modelMap == null) {
			modelMap = getSubModels();
		}
		
		modelMap.remove(name);
		
		final IFile      store  = getModelStore();
		final Properties models = PropUtils.loadProperties(store.getContents());
		
		models.remove(name);

		PropUtils.storeProperties(models, store.getLocation().toOSString());
		store.refreshLocal(IResource.DEPTH_ONE, null);
	}

}
