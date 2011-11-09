package com.isencia.passerelle.workbench.model.editor.ui.properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ActorGeneralSection extends AbstractPropertySection {
	
	private NamedObj model = null;
	private Text nameText;
	private Text typeText;
	
	/**
	 * Update model if properties have been changed
	 */
	private ModifyListener listener = new ModifyListener() {

		public void modifyText(ModifyEvent event) {
			if( model != null ) {
				final String text = nameText.getText();
				if( text != null ) {
					ActorGeneralSection.setName(model, text);		               
				}
			}
		}
	};
	
	/**
	 * Adds a name change event to the command stack.
	 * 
	 * This method will show an error message to the user if the name is not legal.
	 * 
	 * @param model
	 * @param text
	 */
	public static void setName(final NamedObj model, final String text) {
		
		// Perform Change in a ChangeRequest so that all Listeners are notified
		if (ModelUtils.isNameLegal(text)) {
			model.requestChange(new ModelChangeRequest(ActorGeneralSection.class, model, "changeName") {
				protected void _execute() throws Exception {
					model.setDisplayName(text);
					model.setName(text);
				}
			});
		} else {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					               "Invalid Name",
					               "The name '"+text+"' is not allowed.\n\n"+
					               "Names should not contain '.'");
		}

	}
	
	public void createControls(Composite parent, TabbedPropertySheetPage page) {
		super.createControls(parent, page);
		
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData layoutData;
		// Create Name TextInput Control
		layoutData = new FormData();
		nameText = getWidgetFactory().createText(composite, "");
		layoutData.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		layoutData.right = new FormAttachment(100, 0);
		layoutData.top = new FormAttachment(0,ITabbedPropertyConstants.VSPACE);
		nameText.setLayoutData(layoutData);
		nameText.addModifyListener(listener);
		
		// Create Name Label Control
		CLabel nameLabel = getWidgetFactory().createCLabel(composite, "Name:");
		layoutData = new FormData();
		layoutData.left = new FormAttachment(0, 0);
		layoutData.right = new FormAttachment(nameText,-ITabbedPropertyConstants.HSPACE);
		layoutData.top = new FormAttachment(nameText,0,SWT.CENTER);
		nameLabel.setLayoutData(layoutData);
	}
	
	/**
	 * Get the model
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if( !(selection instanceof IStructuredSelection))
			return;
		Object input = ((IStructuredSelection)selection).getFirstElement();
		if( input instanceof AbstractBaseEditPart ) {
			model = (NamedObj) ((AbstractBaseEditPart)input).getEntity();
		}
	}
	
	/**
	 * Set/Update properties based on model
	 */
	public void refresh() {
		nameText.removeModifyListener(listener);
		String name = "";
		if( model != null )
			name = model.getName();
		nameText.setText(name);
		nameText.addModifyListener(listener);
	}
}
