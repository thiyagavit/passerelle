package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;

import com.isencia.passerelle.actor.gui.PasserelleActorControllerFactory;
import com.isencia.passerelle.actor.gui.PasserelleEditorFactory;
import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

public class CreateSubModelAction extends SelectionAction {
	private PasserelleModelMultiPageEditor parent;
	private final String icon = "icons/flow.png";
	public static String CREATE_SUBMODEL = "createSubModel";

	public CreateSubModelAction(IEditorPart part,
			PasserelleModelMultiPageEditor parent) {
		super(part);
		this.parent = parent;
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Export submodel");
		setId(ActionFactory.EXPORT.getId());
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);

	}

	@Override
	public void run() {
		try {
			
			exportEntityToClassFile(parent.getSelectedContainer());
			parent.getActorTreeViewPage().getTreeViewer().refresh();
		} catch (Exception e) {
		}
	}

	public Entity exportEntityToClassFile(Entity entity) throws Exception {
		Entity entityAsClass = (Entity) entity.clone(entity.workspace());
		entityAsClass.setClassDefinition(true);

		if (entityAsClass instanceof CompositeActor) {
			CompositeActor compActor = ((CompositeActor) entityAsClass);
			Director d = compActor.getDirector();
			if (d != null) {
				// remove the director from the class definition
				d.setContainer(null);
			}

			Attribute ctrlFact = compActor.getAttribute("_controllerFactory");
			if (ctrlFact == null) {
				new PasserelleActorControllerFactory(compActor,
						"_controllerFactory");
			} else if (!(ctrlFact instanceof PasserelleActorControllerFactory)) {
				ctrlFact.setContainer(null);
				new PasserelleActorControllerFactory(compActor,
						"_controllerFactory");
			}
			Attribute editorFact = compActor.getAttribute("_editorFactory");
			if (editorFact == null) {
				new PasserelleEditorFactory(compActor, "_editorFactory");
			} else if (!(editorFact instanceof PasserelleEditorFactory)) {
				editorFact.setContainer(null);
				new PasserelleEditorFactory(compActor, "_editorFactory");
			}
			Attribute editorPaneFact = compActor
					.getAttribute("_editorPaneFactory");
			if (editorPaneFact == null) {
				new PasserelleEditorPaneFactory(compActor, "_editorPaneFactory");
			} else if (!(editorPaneFact instanceof PasserelleEditorPaneFactory)) {
				editorPaneFact.setContainer(null);
				new PasserelleEditorPaneFactory(compActor, "_editorPaneFactory");
			}
		}
		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString();
		File file = new File(workspacePath + File.separator + ".metadata"
				+ File.separator + entityAsClass.getName() + ".moml");
		String name = entityAsClass.getName();
		String filename = file.getName();
		int period = filename.indexOf(".");
		if (period > 0) {
			name = filename.substring(0, period);
		} else {
			name = filename;
		}

		FileWriter fileWriter = new FileWriter(file);
		try {
			if (entityAsClass.getContainer() != null) {
				// in this case the exportMoML below does not add the xml
				// header itself
				// if the entity is a top-level one,without container the
				// exportMoML does add it
				fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
						+ "<!DOCTYPE " + entityAsClass.getElementName()
						+ " PUBLIC " + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
						+ "    \"http://ptolemy.eecs.berkeley.edu"
						+ "/xml/dtd/MoML_1.dtd\">\n");
			}
			entityAsClass.exportMoML(fileWriter, 0, name);
		} finally {
			fileWriter.close();
		}
		PaletteItemFactory factory = PaletteItemFactory.get();
		Flow flow = FlowManager.readMoml(new FileReader(file));
		factory.addSubModel(flow);
		return entityAsClass;
	}

	@Override
	protected boolean calculateEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
}
