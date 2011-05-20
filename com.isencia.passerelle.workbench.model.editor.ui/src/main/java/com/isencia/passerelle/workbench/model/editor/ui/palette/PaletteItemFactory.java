package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.osgi.framework.Bundle;

import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteItemFactory implements Serializable {

	public static final String FAVORITE_GROUPS = "FavoriteGroups";
	public static final String SUBMODELS = "Submodels";
	public static final String DEFAULT_FAVORITES_NAME = "Favorites";
	private static PreferenceStore store;
	private CreationFactory selectedItem;

	public CreationFactory getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(CreationFactory selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String[] getFavoriteGroupNames() {
		String groups = getStore().getString(FAVORITE_GROUPS);
		if (groups == null || groups.isEmpty()) {
			return new String[] { DEFAULT_FAVORITES_NAME };
		}
		return groups.split(",");
	}

	public PreferenceStore getStore() {
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

	List<PaletteGroup> paletteGroups;
	private static PaletteItemFactory factory;
	private static Map<String, String> actorBundleMap = new HashMap<String, String>();

	public static String getBuildId(String className) {
		return actorBundleMap.get(className);
	}

	public void addPaletteGroup(String label) {
		PaletteGroup paletteGroup = new PaletteGroup(label, label);
		paletteGroups.add(paletteGroup);
		groups.put(label, paletteGroup);
	}

	public List<PaletteGroup> getPaletteGroups() {
		return paletteGroups;
	}

	public Collection<PaletteGroup> getAllPaletteGroups() {
		return groups.values();
	}

	public String[] getFavorites() {
		PreferenceStore store = getStore();
		if (store == null) {
			return new String[0];
		}
		return store.preferenceNames();
	}

	public static PaletteItemFactory get() {
		if (factory == null) {
			factory = new PaletteItemFactory();
		}
		return factory;
	}

	private PaletteItemFactory() {
		super();
		init();
	}

	private Map<String, PaletteGroup> groups;
	private Map<String, PaletteItemDefinition> paletteItemMap;

	public PaletteItemDefinition getPaletteItem(String clazz) {

		return paletteItemMap.get(clazz);
	}

	public PaletteGroup getPaletteGroup(String id) {

		return groups.get(id);
	}

	public Collection<PaletteItemDefinition> getAllPaletteItems() {

		return paletteItemMap.values();
	}

	public PaletteItemDefinition getPaletteItem(String groupName, String id) {
		if (groupName == null) {
			for (Map.Entry<String, PaletteGroup> entry : groups.entrySet()) {
				PaletteGroup group = entry.getValue();
				PaletteItemDefinition def = group.getPaletteItem(id);
				if (def != null) {
					return def;
				}
			}
		}

		PaletteGroup group = groups.get(groupName);
		if (group == null) {
			return null;
		}
		return group.getPaletteItem(id);
	}

	public ImageDescriptor getIcon(Class clazz) {
		if (clazz == null) {
			return null;
		}
		return getIcon(clazz.getName());
	}

	public ImageDescriptor getIcon(String clazz) {
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getIcon();
		}
		return Activator.getImageDescriptor("icons/ide.gif");
	}

	public CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition(
			String type) {

		return createPaletteEntryFromPaletteDefinition(getPaletteItem(type));
	}

	public CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition(
			Class type) {
		if (type == null) {
			return null;
		}
		return createPaletteEntryFromPaletteDefinition(type.getName());
	}

	public CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition(
			PaletteItemDefinition def) {
		if (def instanceof SubModelPaletteItemDefinition) {
			return new CombinedTemplateCreationEntry(def.getName(), def
					.getName(), new ClassTypeFactory(def.getClazz(),
					((SubModelPaletteItemDefinition) def).getFlow()), def
					.getIcon(), //$NON-NLS-1$
					def.getIcon()//$NON-NLS-1$
			);
		} else {
			return new CombinedTemplateCreationEntry(def.getName(), def
					.getName(), new ClassTypeFactory(def.getClazz(), def
					.getName()), def.getIcon(), //$NON-NLS-1$
					def.getIcon()//$NON-NLS-1$
			);
		}

	}

	public boolean containsFavorite(PaletteContainer container, Object type,
			Object name) {
		List children = container.getChildren();
		for (Object child : children) {
			if (child instanceof CombinedTemplateCreationEntry) {
				CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) child;
				ClassTypeFactory entryType = (ClassTypeFactory) entry
						.getTemplate();
				if ((((Class) entryType.getObjectType()).getName().equals(type) && entryType
						.getNewObject().equals(name))
						|| (entryType.getNewObject() instanceof Flow && ((Flow) entryType
								.getNewObject()).getName().equals(name))) {
					return true;
				}
			}
		}
		return false;
	}

	public void removeFavorite(String name) {
		getStore().putValue(name, "");
	}

	public boolean addFavorite(String name, PaletteContainer container) {
		PaletteItemDefinition paletteItem = PaletteItemFactory.get()
				.getPaletteItem(name);
		if (paletteItem != null
				&& !containsFavorite(container, name, paletteItem.getName())) {

			CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition = createPaletteEntryFromPaletteDefinition(paletteItem);
			container.add(createPaletteEntryFromPaletteDefinition);
			PaletteBuilder.synchFavorites();
			return true;
		}

		return false;

	}

	public Color getColor(Class clazz) {
		if (clazz == null) {
			return null;
		}
		return getColor(clazz.getName());
	}

	public Color getColor(String clazz) {
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getColor();
		}
		return null;
	}

	public String getType(Class clazz) {
		if (clazz.equals(Flow.class)) {
			return "Subflow";
		}
		return getType(clazz.getName());
	}

	public String getType(String clazz) {
		try {
			if (Director.class.isAssignableFrom(Class.forName(clazz))) {
				return "Director";
			}
		} catch (ClassNotFoundException e) {

		}
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getName();
		}
		return clazz;
	}

	public static final String USER_LIBRARY = "User Library";
	private PaletteGroup userLibrary;

	public PaletteGroup getUserLibrary() {
		return userLibrary;
	}

	private void init() {
		paletteGroups = new ArrayList<PaletteGroup>();
		userLibrary = new PaletteGroup(USER_LIBRARY, USER_LIBRARY);
		paletteGroups.add(userLibrary);
		groups = new HashMap<String, PaletteGroup>();
		paletteItemMap = new HashMap<String, PaletteItemDefinition>();
		ImageDescriptor icon = null;

		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.isencia.passerelle.engine.actorGroups");
			if (config != null) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement
							.getAttribute("name");
					String idAttribute = configurationElement
							.getAttribute("id");
					String iconAttribute = configurationElement
							.getAttribute("icon");
					String priorityAttribute = configurationElement
							.getAttribute("priority");
					String expandedAttribute = configurationElement
							.getAttribute("open");

					PaletteGroup e = new PaletteGroup(idAttribute,
							nameAttribute);
					if (priorityAttribute != null) {
						try {
							e.setPriority(Integer.parseInt(priorityAttribute));
						} catch (Exception e2) {

						}
					}
					if (expandedAttribute != null) {
						try {
							e.setExpanded(new Boolean(expandedAttribute));
						} catch (Exception e2) {

						}
					}
					final String bundleId = configurationElement
							.getDeclaringExtension().getContributor().getName();
					Bundle bundle = Platform.getBundle(bundleId);
					icon = Activator.getImageDescriptor("icons/ide.gif");
					if (iconAttribute != null && !iconAttribute.isEmpty()) {
						try {
							icon = Activator.getImageDescriptor(bundleId,
									iconAttribute);

						} catch (Exception e2) {

						}
					}
					e.setIcon(icon);
					groups.put(e.getId(), e);

				}
			}
			for (IConfigurationElement configurationElement : config) {
				String parentAttribute = configurationElement
						.getAttribute("parent");
				String idAttribute = configurationElement.getAttribute("id");
				PaletteGroup currentGroup = groups.get(idAttribute);
				PaletteGroup parentGroup = null;
				if (parentAttribute != null) {
					parentGroup = groups.get(parentAttribute);
				}
				if (parentGroup != null) {
					parentGroup.addPaletteGroup(currentGroup);
					currentGroup.setParent(parentGroup);
				} else {
					paletteGroups.add(currentGroup);
				}

			}
			java.util.Collections.sort(paletteGroups);

		} catch (Exception e) {

		}

		for (Flow flow : getSubModels().values()) {
			addSubModel(flow);
		}

		// Find all Actors and add them to the corresponding container
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.isencia.passerelle.engine.actors");
			if (config != null) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement
							.getAttribute("name");
					String colorAttribute = configurationElement
							.getAttribute("color");
					String idAttribute = configurationElement
							.getAttribute("id");

					String groupAttribute = configurationElement
							.getAttribute("group");
					PaletteGroup group = groups.get(groupAttribute);
					String iconAttribute = configurationElement
							.getAttribute("icon");

					final String bundleId = configurationElement
							.getDeclaringExtension().getContributor().getName();

					Bundle bundle = Platform.getBundle(bundleId);
					icon = Activator.getImageDescriptor("icons/ide.gif");
					if (iconAttribute != null && !iconAttribute.isEmpty()) {
						try {
							icon = Activator.getImageDescriptor(bundleId,
									iconAttribute);

						} catch (Exception e) {

						}
					}

					final Class<?> clazz = loadClass(configurationElement,
							bundleId);

					if (clazz != null && group != null) {
						actorBundleMap.put(clazz.getName(), bundleId);
						PaletteItemDefinition item = new PaletteItemDefinition(
								icon, group, idAttribute, nameAttribute,
								colorAttribute, clazz);
						paletteItemMap.put(item.getClazz().getName(), item);
					}
				}
			}
		} catch (Exception e) {
		}

	}

	public void registerSubModel(Flow flow) {
		if (modelList == null) {
			modelList = getSubModels();
		}
		String model = flow.getName();
		if (!modelList.containsKey(model)) {
			modelList.put(model, flow);

			StringBuffer modelString = new StringBuffer(getStore().getString(
					SUBMODELS));
			modelString.append(model);
			modelString.append(",");
			getStore().putValue(SUBMODELS, modelString.toString());
			try {
				getStore().save();
			} catch (IOException e1) {

			}

		}

	}

	private Map<String, Flow> modelList;

	public Map<String, Flow> getSubModels() {

		if (modelList == null) {
			String models = getStore().getString(PaletteItemFactory.SUBMODELS);
			modelList = new HashMap<String, Flow>();
			for (String model : models.split(",")) {

				InputStreamReader reader = new InputStreamReader(
						createEmptySubModel(model));
				try {
					Flow flow = FlowManager.readMoml(reader);
					MoMLParser.putActorClass(model, flow);
					modelList.put(model, flow);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return modelList;
	}

	private InputStream createEmptySubModel(String subModel) {
		String contents = "<?xml version=\"1.0\" standalone=\"no\"?> \r\n"
				+ "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\"> \r\n"
				+ "<class name=\"" + subModel
				+ "\" extends=\"ptolemy.actor.TypedCompositeActor\"> </class>";
		return new ByteArrayInputStream(contents.getBytes());
	}

	public void addSubModel(Flow flow) {
		if (!paletteItemMap.containsKey(flow.getName())) {
			SubModelPaletteItemDefinition item = new SubModelPaletteItemDefinition(
					flow, userLibrary, flow.getName(), flow.getName());
			paletteItemMap.put(flow.getName(), item);
		} else {
			PaletteItemDefinition item = paletteItemMap.get(flow.getName());
			if (item instanceof SubModelPaletteItemDefinition) {
				((SubModelPaletteItemDefinition) item).setFlow(flow);
			}
		}
		MoMLParser.putActorClass(flow.getName(), flow);
		registerSubModel(flow);
	}

	private static Class<?> loadClass(
			final IConfigurationElement configurationElement,
			final String bundleId) {

		final Bundle bundle = Platform.getBundle(bundleId);
		try {
			return bundle.loadClass(configurationElement.getAttribute("class"));
		} catch (Exception e) {
			final Bundle actors = Platform
					.getBundle("com.isencia.passerelle.actor");
			try {
				return actors.loadClass(configurationElement
						.getAttribute("class"));
			} catch (Exception e1) {
				return null;
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static void setLocation(NamedObj model, double[] location) {
		if (model instanceof Locatable) {
			try {
				((Locatable) model).setLocation(location);
				NamedObj cont = model.getContainer();
				cont.attributeChanged((Attribute) model);
			} catch (IllegalActionException e) {
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
			}
		} else {
			try {
				new Location(model, "_location").setLocation(location);
			} catch (IllegalActionException e) {
			} catch (NameDuplicationException e) {
			}
		}
	}

}
