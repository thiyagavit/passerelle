package com.isencia.passerelle.workbench.model.editor.graphiti.input;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;

// TODO add memento-support
// need a specialized org.eclipse.graphiti.ui.editor.DiagramEditorInputFactory
// and defining the needed memento property IDs and an overridden saveState()
public class PasserelleEditorInput extends DiagramEditorInput {

  public PasserelleEditorInput(URI diagramUri, String providerId) {
    super(diagramUri, providerId);
  }
  
  @Override
  public String getFactoryId() {
    return PasserelleEditorInputFactory.class.getName();
  }

  @Override
  public Object getAdapter(Class adapter) {
    if (IResource.class.isAssignableFrom(adapter)) {
      return GraphitiUiInternal.getEmfService().getFile(getUri());
      } 
    return null;

  }
}
