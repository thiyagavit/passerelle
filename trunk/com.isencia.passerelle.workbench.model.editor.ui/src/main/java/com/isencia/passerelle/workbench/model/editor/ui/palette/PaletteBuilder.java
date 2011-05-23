package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteBuilder {

	private static final String ACTORGROUP_UTILITIES = "com.isencia.passerelle.actor.actorgroup.utilities";
	private static Logger logger = LoggerFactory
			.getLogger(PaletteBuilder.class);
	private static PaletteRoot paletteRoot;

	static public PaletteRoot createPalette(EditorPart parent) {
		if (paletteRoot == null) {
			paletteRoot = new PaletteRoot();
			Collection<PaletteGroup> groups = PaletteItemFactory.get()
					.getAllPaletteGroups();
			paletteRoot.addAll(createCategories(paletteRoot, parent,
					PaletteItemFactory.get().getPaletteGroup(
							ACTORGROUP_UTILITIES)));
		}

		return paletteRoot;
	}

	static private List createCategories(PaletteRoot root, EditorPart parent,
			PaletteGroup group) {

		List categories = new ArrayList();
		PaletteItemFactory factory = PaletteItemFactory.get();
		categories.add(createControlGroup(root));
		try {
			PaletteContainer paletteContainer = createPaletteContainer(group
					.getName(), group.getIcon(), true);
			logger.trace("Created category " + paletteContainer.getLabel());
			for (PaletteItemDefinition def : group.getPaletteItems()) {
				CombinedTemplateCreationEntry entry = factory
						.createPaletteEntryFromPaletteDefinition(def);
				paletteContainer.add(entry);

			}

			categories.add(paletteContainer);
		} catch (Exception e) {
			logger.error("Error creating Palette Categories", e);
		}
		String[] favoriteGroups = factory.getFavoriteGroupNames();
		for (String favoriteGroup : favoriteGroups) {
			PaletteContainer createPaletteContainer = createFavoriteContainer(favoriteGroup);
			favoritesContainers.put(favoriteGroup, createPaletteContainer);
			categories.add(createPaletteContainer);
			String favorites = factory.getStore().getString(favoriteGroup);
			if (favorites != null && !favorites.isEmpty()) {
				String[] names = favorites.split(",");
				for (String name : names) {
					factory.addFavorite(name,
							(PaletteContainer) createPaletteContainer);
				}
			}
		}

		return categories;
	}

	private static boolean isClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	public static PaletteContainer createFavoriteContainer(String favoriteGroup) {
		PaletteContainer createPaletteContainer = createPaletteContainer(
				favoriteGroup, Activator
						.getImageDescriptor("icons/favourites.gif"), true);
		return createPaletteContainer;
	}

	public static void synchFavorites() {
		PaletteItemFactory factory = PaletteItemFactory.get();

		StringBuffer containers = new StringBuffer();
		List containertLis = paletteRoot.getChildren();
		for (Object e : containertLis) {

			if (e instanceof PaletteDrawer) {
				PaletteContainer favoritesContainer = (PaletteDrawer) e;
				if (!favoritesContainer.getLabel().equals(
						PaletteItemFactory.get().getPaletteGroup(
								ACTORGROUP_UTILITIES).getName())) {
					containers.append(favoritesContainer.getLabel());
					containers.append(",");
					StringBuffer entries = new StringBuffer();
					for (Object o : favoritesContainer.getChildren()) {
						if (o instanceof CombinedTemplateCreationEntry) {
							CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) o;
							ClassTypeFactory entryType = (ClassTypeFactory) entry
									.getTemplate();
							Object objectType = entryType.getObjectType();
							if (entryType.getNewObject() instanceof SubModelPaletteItemDefinition) {
								entries
										.append(((SubModelPaletteItemDefinition) entryType
												.getNewObject()).getName());
								entries.append(",");
							} else {
								entries.append(((Class) objectType).getName());
								entries.append(",");

							}
						}
					}
					addFavoriteGroup(favoritesContainer.getLabel(),
							favoritesContainer);
					factory.getStore().putValue(favoritesContainer.getLabel(),
							entries.toString());
				}

			}
		}
		factory.getStore().putValue(PaletteItemFactory.FAVORITE_GROUPS,
				containers.toString());
		try {
			factory.getStore().save();
		} catch (IOException ex) {
		}

	}

	public static PaletteContainer getDefaultFavoriteGroup() {
		PaletteEntry entry = favoritesContainers
				.get(PaletteItemFactory.DEFAULT_FAVORITES_NAME);
		if (entry instanceof PaletteContainer) {
			return (PaletteContainer) entry;
		}
		return createFavoriteContainer(PaletteItemFactory.DEFAULT_FAVORITES_NAME);
	}

	public static PaletteEntry getFavoriteGroup(String name) {
		return favoritesContainers.get(name);
	}

	public static void addFavoriteGroup(String name, PaletteEntry e) {
		favoritesContainers.put(name, e);

	}

	static public HashMap<String, PaletteEntry> favoritesContainers = new HashMap<String, PaletteEntry>();

	static private PaletteContainer createControlGroup(PaletteRoot root) {

		org.eclipse.gef.palette.PaletteGroup controlGroup = new org.eclipse.gef.palette.PaletteGroup(
				"ControlGroup");

		List entries = new ArrayList();

		final ToolEntry tool = new PanningSelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);

		PaletteStack marqueeStack = new PaletteStack("Stack", "", null); //$NON-NLS-1$
		marqueeStack.add(new MarqueeToolEntry());
		MarqueeToolEntry marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
		marqueeStack.add(marquee);
		marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
						| MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED));
		marqueeStack.add(marquee);
		marqueeStack
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(marqueeStack);

		final ConnectionCreationToolEntry ctool = new ConnectionCreationToolEntry(
				"Connection", "Connection", null, Activator
						.getImageDescriptor("icons/connection16.gif"),
				Activator.getImageDescriptor("icons/connection24.gif"));
		entries.add(ctool);
		controlGroup.addAll(entries);
		return controlGroup;
	}

	/**
	 * Change to make first palette open and others closed, as we will put the
	 * most important actors in this palette.
	 * 
	 * @param name
	 * @param image
	 * @return
	 */
	static private PaletteContainer createPaletteContainer(final String name,
			final ImageDescriptor image, final boolean open) {

		PaletteDrawer drawer = new PaletteDrawer(name, image);
		if (open) {
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
		} else {
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
		}

		return drawer;
	}

}
