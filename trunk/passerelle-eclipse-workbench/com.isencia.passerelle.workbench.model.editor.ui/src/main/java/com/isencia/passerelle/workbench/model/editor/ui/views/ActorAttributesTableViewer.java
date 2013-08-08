package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.editor.common.utils.ParameterUtils;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.HelpUtils;
import com.isencia.passerelle.workbench.model.editor.ui.PreferenceConstants;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteAttributeHandler;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.properties.NamedObjComparator;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;
import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ActorAttributesTableViewer extends TableViewer implements CommandStackEventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(ActorAttributesTableViewer.class);

  private VariableEditingSupport valueColumnEditor;

  private NamedObj entity;
  private IWorkbenchPart actorSourcePart;

  public ActorAttributesTableViewer(NamedObj entity, IWorkbenchPart actorSourcePart, Composite parent, int style) {
    super(parent, style);
    this.entity = entity;
    this.actorSourcePart = actorSourcePart;
    getTable().setLinesVisible(true);
    getTable().setHeaderVisible(true);
    
    createColumns();
    setUseHashlookup(true);
    setColumnProperties(new String[] { "Property", "Value" });

    createPopupMenu();
    getTable().addKeyListener(new KeyListener() {

      public void keyReleased(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.F1) {
          try {
            showHelpSelectedParameter();
          } catch (IllegalActionException e1) {

          }
        }
        if (e.character == SWT.DEL) {
          try {
            deleteSelectedParameter();
          } catch (IllegalActionException e1) {
            LOGGER.error("Cannot delete ", e1);
          }
        }
      }
    });
  }

  public void createTableModel(final IWorkbenchPart selectedEntitySourcePart, final NamedObj selectedEntity) {
    this.actorSourcePart = selectedEntitySourcePart;
    this.entity = selectedEntity;
    
    final List<Attribute> parameterList = new ArrayList<Attribute>();
    if (selectedEntity != null) {
      Class filter = null;
      if (selectedEntity instanceof TextAttribute) {
        filter = StringAttribute.class;
      } else {
        filter = Parameter.class;
      }
      Iterator parameterIterator = selectedEntity.attributeList(filter).iterator();
      boolean expert = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT);
      while (parameterIterator.hasNext()) {
        Attribute parameter = (Attribute) parameterIterator.next();

        if (!(parameter instanceof Parameter) || (ParameterUtils.isVisible(selectedEntity, (Parameter) parameter, expert))) {
          parameterList.add(parameter);
        }
      }
    }

    Collections.sort(parameterList, new NamedObjComparator());
    
    try {
      setContentProvider(new IStructuredContentProvider() {
        public void dispose() {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

        public Object[] getElements(Object inputElement) {
          if (entity == null) {
            return new Parameter[] {};
          } else {
            final List<Object> ret = new ArrayList<Object>(parameterList.size() + 1);

            final Director director = entity instanceof Actor ? (Director) ((Actor) entity).getDirector() : null;

            boolean expert = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT);
            if (entity instanceof Actor && expert)
              ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.TYPE, PaletteBuilder.getInstance().getType(entity.getClass())));

            if (entity instanceof Actor && director != null && expert)
              ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.CLASS, entity.getClass().getName()));

            ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.NAME, PaletteBuilder.getInstance().getType(entity.getName())));
            ret.addAll(parameterList);
            return ret.toArray(new Object[ret.size()]);
          }
        }
      });

      setInput(new Object());
      refresh();
    } catch (Exception e) {
      LOGGER.error("Cannot set input", e);
    }
  }

  public void stackChanged(CommandStackEvent event) {
    refresh();
  }

  public void clear() {
    if (actorSourcePart != null && actorSourcePart instanceof PasserelleModelMultiPageEditor) {
      ((PasserelleModelMultiPageEditor) actorSourcePart).getEditor().getEditDomain().getCommandStack().removeCommandStackEventListener(this);
    }
    createTableModel(null, null);
  }

  private void createColumns() {

    final TableViewerColumn name = new TableViewerColumn(this, SWT.LEFT, 0);

    name.getColumn().setText("Property");
    name.getColumn().setWidth(200);
    name.setLabelProvider(new PropertyLabelProvider());
    final TableViewerColumn value = new TableViewerColumn(this, SWT.LEFT, 1);

    value.getColumn().setText("Value");
    value.getColumn().setWidth(700);
    value.setLabelProvider(new VariableLabelProvider(this));
    this.valueColumnEditor = new VariableEditingSupport(this);
    value.setEditingSupport(valueColumnEditor);
  }

  public boolean canEditAttribute(final Object attribute) {
    return valueColumnEditor.canEdit(attribute);
  }

  public void deleteSelectedParameter() throws IllegalActionException {

    final ISelection sel = getSelection();
    if (sel != null && sel instanceof StructuredSelection) {
      final StructuredSelection s = (StructuredSelection) sel;
      final Object o = s.getFirstElement();
      if (o instanceof String)
        return; // Cannot delete name
      if (o instanceof Attribute) {
        setAttributeValue(o, null);
      }
    }
  }

  public void showHelpSelectedParameter() throws IllegalActionException {

    final ISelection sel = getSelection();
    if (sel != null && sel instanceof StructuredSelection) {
      final StructuredSelection s = (StructuredSelection) sel;
      final Object o = s.getFirstElement();
      String contextId = HelpUtils.getContextId(o);
      if (contextId != null) {
        // TODO revert this when using context specific help
        // WorkbenchHelp.displayHelp(contextId);
        WorkbenchHelpSystem.getInstance().displayHelpResource(contextId);
      }

    }
  }

  public void setActorName(final GeneralAttribute element, String name) {
    if (ModelUtils.isNameLegal(name)) {
      element.setValue(name);
      if (actorSourcePart != null && actorSourcePart instanceof PasserelleModelMultiPageEditor) {
        final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.actorSourcePart;
        try {
          final RenameCommand cmd = new RenameCommand(this, entity, element);
          ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
          ed.refreshActions();
        } catch (Exception ne) {
          MessageDialog.openError(Display.getCurrent().getActiveShell(), "Invalid Name", ne.getMessage());
        }
      }
    } else {
      MessageDialog.openError(Display.getCurrent().getActiveShell(), "Invalid Name", "The name '" + name + "' is not allowed.\n\n"
          + "Names should not contain '.'");
    }

  }

  public void setAttributeValue(Object element, Object value) throws IllegalActionException {
    if (this.actorSourcePart instanceof PasserelleModelMultiPageEditor) {
      final AttributeCommand cmd = new AttributeCommand(this, element, value);
      final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.actorSourcePart;
      ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
      ed.refreshActions();
      ed.getEditor().refresh();
    } else if (this.actorSourcePart instanceof PasserelleModelEditor) {
      final AttributeCommand cmd = new AttributeCommand(this, element, value);
      final PasserelleModelEditor ed = (PasserelleModelEditor) this.actorSourcePart;
      ed.getEditDomain().getCommandStack().execute(cmd);
      ed.getEditorSite().getActionBars().getToolBarManager().update(true);
      ed.refresh();
    } else if (this.actorSourcePart instanceof IDiagramEditor) {
      // System.out.println(element + "=" + value);
      if (element instanceof Parameter) {
        ((Parameter) element).setExpression(value.toString());
      }
      // IDiagramEditor ed = (IDiagramEditor) this.part;
      // ed.getEditingDomain().getCommandStack().execute(cmd);
    }
  }

  /**
   * Initialize the menu.
   */
  private void createPopupMenu() {
    MenuManager menuMan = new MenuManager();

    menuMan.add(new Action("Delete Attribute", Activator.getImageDescriptor("icons/delete_obj.gif")) {
      public void run() {
        (new DeleteAttributeHandler()).run(null);
      }
    });
    menuMan.add(new Separator());
    menuMan.add(new Action("Help", Activator.getImageDescriptor("icons/help.gif")) {
      public void run() {
        try {
          showHelpSelectedParameter();
        } catch (IllegalActionException e) {
        }
      }
    });
    menuMan.add(new Action("Help Contents", Activator.getImageDescriptor("icons/help.gif")) {
      public void run() {
        WorkbenchHelpSystem.getInstance().displayHelp();
      }
    });

    getControl().setMenu(menuMan.createContextMenu(getControl()));
  }

}
