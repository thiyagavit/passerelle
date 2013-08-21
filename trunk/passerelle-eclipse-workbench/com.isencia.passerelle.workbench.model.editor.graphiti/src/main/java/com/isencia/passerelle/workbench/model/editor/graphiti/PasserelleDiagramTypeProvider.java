/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.workbench.model.editor.graphiti;

import java.net.URL;
import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.ui.part.EditorPart;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.DiagramFlowRepository;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserelleIndependenceSolver;

/**
 * @author erwin
 */
public class PasserelleDiagramTypeProvider extends AbstractDiagramTypeProvider {

  private IToolBehaviorProvider[] toolBehaviorProviders;
  private PasserelleIndependenceSolver independenceSolver;

  /**
   * 
   */
  public PasserelleDiagramTypeProvider() {
    independenceSolver = new PasserelleIndependenceSolver();
    setFeatureProvider(new PasserelleDiagramFeatureProvider(this));
  }

  @Override
  public void init(Diagram diagram, IDiagramEditor diagramEditor) {
    super.init(diagram, diagramEditor);
    Flow flow = DiagramFlowRepository.getFlowForDiagram(diagram);
    if (flow == null && diagramEditor != null) {
      try {
        DiagramEditorInput dei = (DiagramEditorInput) ((EditorPart) diagramEditor).getEditorInput();
        flow = new Flow(diagram.getName(), new URL(dei.getUri().toString()));
        DiagramFlowRepository.registerDiagramAndFlow(diagram, flow);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    independenceSolver.setTopLevel(flow);
  }

  @Override
  public IToolBehaviorProvider[] getAvailableToolBehaviorProviders() {
    if (toolBehaviorProviders == null) {
      toolBehaviorProviders = new IToolBehaviorProvider[] { new PasserelleToolBehaviorProvider(this) };
    }
    return toolBehaviorProviders;
  }
  
  public PasserelleIndependenceSolver getIndependenceSolver() {
    return independenceSolver;
  }
}
