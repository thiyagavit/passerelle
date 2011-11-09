package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;
import com.isencia.passerelle.workbench.model.utils.SubModelUtils;

public class PaletteItemFactory implements Serializable {

	public static final String FAVORITE_GROUPS = "FavoriteGroups";
	public static final String DEFAULT_FAVORITES_NAME = "Favorites";

	private CreationFactory selectedItem;

	public CreationFactory getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(CreationFactory selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String[] getFavoriteGroupNames() throws Exception {
		String groups = ModelUtils.getFavouritesStore().getString(
				FAVORITE_GROUPS);
		if (groups == null || groups.isEmpty()) {
			return new String[] { DEFAULT_FAVORITES_NAME };
		}
		return groups.split(",");
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
		PreferenceStore store;
		try {
			store = ModelUtils.getFavouritesStore();

			if (store == null) {
				return new String[0];
			}
			return store.preferenceNames();
		} catch (Exception e) {
			return null;
		}
	}

	public static PaletteItemFactory getInstance() {
		if (factory == null) {
			factory = new PaletteItemFactory();
		}
		return factory;
	}

	private PaletteItemFactory() {
		super();
		try {
			init();
		} catch (Exception e) {

		}
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
					(SubModelPaletteItemDefinition) def), def.getIcon(), //$NON-NLS-1$
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
						|| (entryType.getNewObject() instanceof SubModelPaletteItemDefinition && ((SubModelPaletteItemDefinition) entryType
								.getNewObject()).getName().equals(name))) {
					return true;
				}
			}
		}
		return false;
	}

	public void removeFavorite(String name) {
		try {
			ModelUtils.getFavouritesStore().putValue(name, "");
		} catch (Exception e) {

		}
	}

	public boolean addFavorite(String name, PaletteContainer container) {
		PaletteItemDefinition paletteItem = PaletteItemFactory.getInstance()
				.getPaletteItem(name);
		if (paletteItem != null
				&& !containsFavorite(container, name, paletteItem.getName())) {

			CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition = createPaletteEntryFromPaletteDefinition(paletteItem);
			container.add(createPaletteEntryFromPaletteDefinition);

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

	public static final String COMPOSITE_ID = "com.isencia.passerelle.workbench.model.editor.ui.palette.composites";
	public static final String COMPOSITE_LABEL = "Composites";
	private PaletteGroup composites;

	public PaletteGroup getComposites() {
		return composites;
	}

	private void init() throws Exception {

		paletteGroups = new ArrayList<PaletteGroup>();

		composites = new PaletteGroup(COMPOSITE_ID, COMPOSITE_LABEL);
		composites
				.setIcon(Activator.getImageDescriptor("icons/Composites.png"));

		paletteGroups.add(composites);
		groups = new HashMap<String, PaletteGroup>();
		paletteItemMap = new HashMap<String, PaletteItemDefinition>();
		ImageDescriptor icon = null;

		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"com.isencia.passerelle.engine.actorGroups");
		if (config != null) {
			for (IConfigurationElement configurationElement : config) {

				String nameAttribute = configurationElement
						.getAttribute("name");
				String idAttribute = configurationElement.getAttribute("id");
				String iconAttribute = configurationElement
						.getAttribute("icon");
				String priorityAttribute = configurationElement
						.getAttribute("priority");
				String expandedAttribute = configurationElement
						.getAttribute("open");

				PaletteGroup e = new PaletteGroup(idAttribute, nameAttribute);
				if (priorityAttribute != null) {
					e.setPriority(Integer.parseInt(priorityAttribute));
				}
				if (expandedAttribute != null) {
					e.setExpanded(new Boolean(expandedAttribute));
				}
				final String bundleId = configurationElement
						.getDeclaringExtension().getContributor().getName();

				icon = Activator.getImageDescriptor("icons/ide.gif");
				if (iconAttribute != null && !iconAttribute.isEmpty()) {
					icon = Activator
							.getImageDescriptor(bundleId, iconAttribute);
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

		for (String id : SubModelUtils.getSubModels().keySet()) {
			final Flow flow = SubModelUtils.getSubModels().get(id);
			addSubModel(flow);
		}

		config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"com.isencia.passerelle.engine.actors");
		if (config != null) {
			for (IConfigurationElement ele : config) {

				String nameAttribute = ele.getAttribute("name");
				String colorAttribute = ele.getAttribute("color");
				String idAttribute = ele.getAttribute("id");

				String groupAttribute = ele.getAttribute("group");
				PaletteGroup group = groups.get(groupAttribute);
				String iconAttribute = ele.getAttribute("icon");

				final String bundleId = ele.getDeclaringExtension()
						.getContributor().getName();

				icon = Activator.getImageDescriptor("icons/ide.gif");
				if (iconAttribute != null && !iconAttribute.isEmpty()) {
					icon = Activator
							.getImageDescriptor(bundleId, iconAttribute);
				}

				final Class<?> clazz = loadClass(ele, bundleId);

				if (clazz != null && group != null) {
					actorBundleMap.put(clazz.getName(), bundleId);
					PaletteItemDefinition item = new PaletteItemDefinition(
							icon, group, idAttribute, nameAttribute,
							colorAttribute, clazz);
					paletteItemMap.put(item.getClazz().getName(), item);
				}
			}
		}
	}

	public void addSubModel(Flow flow) throws Exception {

		if (!paletteItemMap.containsKey(flow.getName())) {
			SubModelPaletteItemDefinition item = new SubModelPaletteItemDefinition(
					flow, composites, flow.getName(), flow.getName());
			paletteItemMap.put(flow.getName(), item);
		} else {
			PaletteItemDefinition item = paletteItemMap.get(flow.getName());
			if (item instanceof SubModelPaletteItemDefinition) {
				((SubModelPaletteItemDefinition) item).setFlow(flow);
			}
		}
	}

	public PaletteItemDefinition removeSubModel(String name) throws Exception {
		PaletteItemDefinition def = paletteItemMap.remove(name);
		final PaletteGroup grp = def.getGroup();
		if (grp != null)
			grp.removePaletteItem(def);
		return def;
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
