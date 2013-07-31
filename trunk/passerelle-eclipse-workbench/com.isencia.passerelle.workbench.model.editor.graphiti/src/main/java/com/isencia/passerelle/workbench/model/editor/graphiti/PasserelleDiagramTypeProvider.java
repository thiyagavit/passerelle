/**
 * 
 */
package com.isencia.passerelle.workbench.model.editor.graphiti;

import java.net.URL;
import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.ui.part.EditorPart;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.util.DiagramFlowRepository;

/**
 * @author delerw
 *
 */
public class PasserelleDiagramTypeProvider extends AbstractDiagramTypeProvider {

  /**
   * 
   */
  public PasserelleDiagramTypeProvider() {
    setFeatureProvider(new PasserelleDiagramFeatureProvider(this));
  }
  
  @Override
  public void init(Diagram diagram, IDiagramEditor diagramEditor) {
    super.init(diagram, diagramEditor);
    Flow flow = DiagramFlowRepository.getFlowForDiagram(diagram);
    if(flow==null && diagramEditor!=null) {
      try {
        DiagramEditorInput dei = (DiagramEditorInput) ((EditorPart)diagramEditor).getEditorInput();
        flow = new Flow(diagram.getName(), new URL(dei.getUri().toString()));
        DiagramFlowRepository.registerDiagramAndFlow(diagram, flow);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
