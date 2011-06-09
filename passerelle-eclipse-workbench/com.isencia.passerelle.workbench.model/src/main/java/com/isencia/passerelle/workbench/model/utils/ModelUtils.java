package com.isencia.passerelle.workbench.model.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.substitution.MultiVariableExpander;
import org.apache.commons.digester.substitution.VariableSubstitutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;

public class ModelUtils {
	public static final String SUBMODELS = "Submodels";

	public static Logger logger = LoggerFactory.getLogger(ModelUtils.class);
	private static PreferenceStore store;

	public static PreferenceStore getStore() {
		if (store == null) {
			store = new PreferenceStore();
			final String workspacePath = ResourcesPlugin.getWorkspace()
					.getRoot().getLocation().toOSString();
			store
					.setFilename(workspacePath
							+ "/.metadata/favorites.properties");
			try {
				store.load();
			} catch (IOException e) {
				return store;
			}
		}
		return store;
	}

	public static HashMap<String, Flow> readSubModels() {
		String models = getStore().getString(SUBMODELS);
		HashMap<String, Flow> modelList = new HashMap<String, Flow>();
		for (String model : models.split(",")) {

			InputStreamReader reader = new InputStreamReader(
					createEmptySubModel(model));
			try {
				Flow flow = FlowManager.readMoml(reader);
				MoMLParser.putActorClass(model, flow);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString();
		File metaData = new File(workspacePath + File.separator + ".metadata");

		if (metaData.isDirectory()) {
			File[] files = metaData.listFiles();
			for (File file : files) {
				try {
					String momlName = file.getName().substring(0,
							file.getName().length() - 5);
					if (file.getName().endsWith(".moml")) {

						FileReader in = new FileReader(file);
						Flow flow = FlowManager.readMoml(in);
						if (flow.isClassDefinition()) {
							MoMLParser.putActorClass(momlName, flow);
							modelList.put(flow.getName(), flow);
						}

					}

				} catch (Exception e1) {
				}
			}
		}
		return modelList;
	}

	private static InputStream createEmptySubModel(String subModel) {
		String contents = "<?xml version=\"1.0\" standalone=\"no\"?> \r\n"
				+ "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\"> \r\n"
				+ "<class name=\"" + subModel
				+ "\" extends=\"ptolemy.actor.TypedCompositeActor\"> </class>";
		return new ByteArrayInputStream(contents.getBytes());
	}

	public static enum ConnectionType {
		SOURCE, TARGET
	};

	public static boolean isClassDefinition(Object model) {
		if (model instanceof CompositeActor) {
			return ((CompositeActor) model).isClassDefinition();
		}
		return false;
	}

	public static final boolean isPort(String type) {
		return ModelConstants.INPUT_IOPORT.equals(type)
				|| ModelConstants.OUTPUT_IOPORT.equals(type);
	}

	public static final boolean isOutputPort(String type) {
		return ModelConstants.OUTPUT_IOPORT.equals(type);
	}

	public static final boolean isInputPort(String type) {
		return ModelConstants.INPUT_IOPORT.equals(type);
	}

	public static Set<Relation> getConnectedRelations(Nameable model,
			ConnectionType connectionType) {

		return getConnectedRelations(model, connectionType, false);

	}

	public static boolean containsVertex(Relation model) {
		Enumeration attributes = model.getAttributes();
		while (attributes.hasMoreElements()) {
			Object temp = attributes.nextElement();
			if (temp instanceof Vertex) {
				return true;
			}
		}
		return false;
	}

	public static Vertex getVertex(Relation model) {
		Enumeration attributes = model.getAttributes();
		while (attributes.hasMoreElements()) {
			Object temp = attributes.nextElement();
			if (temp instanceof Vertex) {
				return (Vertex) temp;
			}
		}
		return null;
	}

	public static List<IOPort> getPorts(Relation relation, NamedObj obj) {
		List<IOPort> ports = new ArrayList<IOPort>();
		for (Object o : relation.linkedPortList()) {
			if (((Port) o).getContainer().equals(obj)) {
				ports.add((IOPort) o);

			}
		}
		return ports;
	}

	public static Set<Relation> getConnectedRelations(Nameable model,
			ConnectionType connectionType, boolean fullList) {
		Set<Relation> connections = new HashSet<Relation>();
		if (model.getContainer() == null
				|| !(model.getContainer() instanceof CompositeEntity))
			return Collections.EMPTY_SET;
		CompositeEntity composite = (CompositeEntity) model.getContainer();
		List<Relation> relationList = new ArrayList<Relation>();
		relationList.addAll(composite.relationList());
		if (model instanceof TypedIOPort) {
			if (fullList && composite != null
					&& composite.getContainer() != null) {
				relationList
						.addAll(((CompositeEntity) composite.getContainer())
								.relationList());
			}
		}
		if (relationList == null || relationList.size() == 0)
			return Collections.EMPTY_SET;

		for (Relation relation : relationList) {
			List linkedObjectsList = relation.linkedObjectsList();
			if (linkedObjectsList == null || linkedObjectsList.size() == 0)
				continue;
			for (Object o : linkedObjectsList) {
				if (o instanceof Port) {
					Port port = (Port) o;
					if (port.getContainer().equals(model)
							|| (model instanceof IOPort && (port
									.equals(((IOPort) model))))) {
						if (connectionType.equals(ConnectionType.SOURCE)) {
							if (port instanceof IOPort
									&& (!(model instanceof IOPort) && ((IOPort) port)
											.isOutput())
									|| ((model instanceof IOPort) && ((IOPort) port)
											.isInput()))
								connections.add(relation);
						} else {
							if (port instanceof IOPort
									&& (!(model instanceof IOPort) && ((IOPort) port)
											.isInput())
									|| ((model instanceof IOPort) && ((IOPort) port)
											.isOutput()))
								connections.add(relation);
						}
					}
				}
			}

		}
		return connections;
	}

	public static List<Relation> getRelations(NamedObj model) {
		ArrayList<Relation> relations = new ArrayList<Relation>();
		if (model == null || !(model instanceof CompositeEntity))
			return Collections.EMPTY_LIST;
		List<Relation> relationList = ((CompositeEntity) model).relationList();
		return relationList;
	}

	@SuppressWarnings("unchecked")
	public static double[] getLocation(NamedObj model) {
		if (model instanceof Locatable) {
			Locatable locationAttribute = (Locatable) model;
			return locationAttribute.getLocation();
		}
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if (attributes == null || attributes.size() == 0) {
			return new double[] { 0.0D, 0.0D };
		}
		Locatable locationAttribute = (Locatable) attributes.get(0);
		return locationAttribute.getLocation();
	}

	@SuppressWarnings("unchecked")
	public static void setLocation(NamedObj model, double[] location) {
		if (model instanceof Locatable) {
			try {
				((Locatable) model).setLocation(location);
				NamedObj cont = model.getContainer();
				cont.attributeChanged((Attribute) model);
			} catch (IllegalActionException e) {
				// TODO Auto-generated catch block
				logger.error("Unable to change location of component", e);
			}

		}
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if (attributes == null)
			return;
		if (attributes.size() > 0) {
			Locatable locationAttribute = (Locatable) attributes.get(0);
			try {
				locationAttribute.setLocation(location);
				model.attributeChanged(attributes.get(0));
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component", e);
			}
		} else {
			try {
				new Location(model, "_location").setLocation(location);
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component", e);
			} catch (NameDuplicationException e) {
				logger
						.error(
								"Duplicate name encountered during change location of component",
								e);
			}
		}
	}

	public static boolean isPortOfActor(IOPort port, Actor actor) {
		for (Object o : actor.inputPortList()) {
			if (o == port) {
				return true;
			}
		}
		for (Object o : actor.outputPortList()) {
			if (o == port) {
				return true;
			}
		}
		return false;
	}

	public static String findUniqueActorName(CompositeEntity parentModel,
			String name) {
		String newName = name;
		if (parentModel == null)
			return newName;
		List entityList = parentModel.entityList();
		if (entityList == null || entityList.size() == 0)
			return newName;

		ComponentEntity entity = parentModel.getEntity(newName);
		int i = 1;
		while (entity != null) {
			newName = name + "(" + i++ + ")";
			entity = parentModel.getEntity(newName);
		}

		return newName;
	}

	/**
	 * Attempts to clean up names in the event that they are not compatible
	 * 
	 * @param newName
	 * @return
	 */
	public static String getLegalName(final String name) {
		String newName = name;
		// newName = newName.replace(' ', '_');
		if (name.indexOf('.') > -1) {
			newName = newName.substring(0, newName.lastIndexOf('.'));
		}
		newName = newName.replace('.', '_');
		return newName;
	}

	public static boolean isNameLegal(String text) {
		if (text == null)
			return false;
		if ("".equals(text))
			return false;
		if (text.indexOf('.') > -1)
			return false;
		return true;
	}

	private static String generateUniqueTextAttributeName(String name,
			NamedObj parent, int index, Class clazz) {
		try {
			String newName = index != 0 ? (name + "(" + index + ")") : name;
			if (parent.getAttribute(newName, clazz) == null) {
				return newName;
			} else {
				index++;
				return generateUniqueTextAttributeName(name, parent, index,
						clazz);
			}
		} catch (IllegalActionException e) {
			return name;
		}

	}

	private static String generateUniqueVertexName(String name,
			NamedObj parent, int index, Class clazz) {

		return "Vertex" + System.currentTimeMillis();

	}

	private static String generateUniquePortName(String name,
			CompositeEntity parent, int index) {
		String newName = index != 0 ? (name + "(" + index + ")") : name;
		boolean contains = false;
		Enumeration ports = parent.getPorts();
		while (ports.hasMoreElements()) {
			String portName = ((Port) ports.nextElement()).getName();
			if (newName.equals(portName)) {
				contains = true;
				break;
			}

		}
		if (!contains) {
			return newName;
		}
		index++;
		return generateUniquePortName(name, parent, index);

	}

	public static String findUniqueName(CompositeEntity parentModel,
			Class clazz, String startName, String actorName) {

		if (clazz.getSimpleName().equals("Vertex")) {
			return generateUniqueVertexName(clazz.getSimpleName(), parentModel,
					0, clazz);
		} else if (clazz.getSimpleName().equals("TextAttribute")) {
			return generateUniqueTextAttributeName(clazz.getSimpleName(),
					parentModel, 0, clazz);
		} else if (clazz.getSimpleName().equals("TypedIOPort")) {
			return generateUniquePortName(startName, parentModel, 0);
		} else {
			return findUniqueActorName(parentModel,
					actorName != null ? actorName : clazz.getSimpleName());
		}
	}

	/**
	 * Currently does not do much as toplevel.getEntity(...) does this search.
	 * 
	 * @param toplevel
	 * @param actorName
	 * @return
	 */
	public static ComponentEntity findEntityByName(CompositeActor toplevel,
			String actorName) {

		ComponentEntity entity = toplevel.getEntity(actorName);
		if (entity != null)
			return entity;

		return null;
	}

	private static void printChildren(InstantiableNamedObj entity) {

		if (entity == null)
			return;
		final List childs = entity.getChildren();
		if (childs == null)
			return;
		for (Object o : childs) {
			if (o instanceof NamedObj) {
				System.out.println(((NamedObj) o).getName());
			}
			if (o instanceof InstantiableNamedObj) {
				printChildren((InstantiableNamedObj) o);
			}
		}
	}

	/**
	 * This names the parent actor workspace name after the eclipse project
	 * being used and this is used by actors to determine which eclipse project
	 * they are executing in.
	 * 
	 * @param compositeActor
	 * @param modelPath
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public static void setCompositeProjectName(
			final CompositeActor compositeActor, final String modelPath)
			throws IllegalActionException, NameDuplicationException {

		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString();

		// We must tell the composite actor the containing project name
		String relPath = modelPath.substring(workspacePath.length());
		IFile projFile = (IFile) ResourcesPlugin.getWorkspace().getRoot()
				.findMember(relPath);
		if (projFile == null) {
			relPath = modelPath.substring(workspacePath.length() + 2);
			projFile = (IFile) ResourcesPlugin.getWorkspace().getRoot()
					.findMember(relPath);

		}
		if (projFile != null) {
			compositeActor.workspace().setName(projFile.getProject().getName());
		} else {
			compositeActor.workspace().setName("default");
		}
		compositeActor.setSource(modelPath);
	}

	/**
	 * Attempts to find the project from the top CompositeActor by using the
	 * workspace name.
	 * 
	 * @param actor
	 * @return
	 */
	public static IProject getProject(final NamedObj actor) {

		// Get top level actor, which knows the project we have.
		CompositeActor comp = (CompositeActor) actor.getContainer();
		while (comp.getContainer() != null) {
			comp = (CompositeActor) comp.getContainer();
		}

		return (IProject) ResourcesPlugin.getWorkspace().getRoot().findMember(
				comp.workspace().getName());
	}

	/**
	 * substutes variables in the string as determined from the actor.
	 * 
	 * @param filePath
	 * @param dataExportTransformer
	 */
	public static String substitute(final String sub, final NamedObj actor) {

		final Map<String, String> variables = new HashMap<String, String>(3);
		variables.put("project_name", getProject(actor).getName());
		variables.put("actor_name", actor.getName());

		MultiVariableExpander expander = new MultiVariableExpander();
		expander.addSource("$", variables);
		// Create a substitutor with the expander
		VariableSubstitutor substitutor = new VariableSubstitutor(expander);

		return substitutor.substitute(sub);
	}
}
