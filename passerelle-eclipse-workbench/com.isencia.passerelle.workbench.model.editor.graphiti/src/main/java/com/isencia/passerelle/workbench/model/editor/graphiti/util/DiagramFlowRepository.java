package com.isencia.passerelle.workbench.model.editor.graphiti.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import com.isencia.passerelle.model.Flow;

public class DiagramFlowRepository {
  
  private final static Map<Diagram, Flow> diagramFlowMap = new HashMap<Diagram, Flow>();
  private final static Map<Flow, Diagram> flowDiagramMap = new HashMap<Flow,Diagram>();
  
  public static void registerDiagramAndFlow(Diagram d, Flow f) {
    diagramFlowMap.put(d, f);
    flowDiagramMap.put(f,d);
  }
  
  public static void unregisterDiagramAndFlow(Diagram d, Flow f) {
    diagramFlowMap.remove(d);
    flowDiagramMap.remove(f);
  }
  
  public static Flow getFlowForDiagram(Diagram d) {
    return diagramFlowMap.get(d);
  }
  
  public static Diagram getDiagramForFlow(Flow f) {
    return flowDiagramMap.get(f);
  }

}
