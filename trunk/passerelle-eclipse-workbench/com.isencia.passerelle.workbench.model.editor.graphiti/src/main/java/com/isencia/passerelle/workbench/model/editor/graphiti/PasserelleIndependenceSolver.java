package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.features.impl.IIndependenceSolver;
import com.isencia.passerelle.model.util.ModelUtils;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.NamedObj;

public class PasserelleIndependenceSolver implements IIndependenceSolver {
  private CompositeActor topLevel;

  @Override
  public String getKeyForBusinessObject(Object bo) {
    if(bo instanceof NamedObj) {
      NamedObj no = (NamedObj)bo;
      if(topLevel==null) {
        topLevel = (CompositeActor) no.toplevel();
      }
      return ModelUtils.getFullNameButWithoutModelName(topLevel, no);
    } else {
      return null;
    }
  }

  @Override
  public Object getBusinessObjectForKey(String key) {
    if(topLevel!=null) {
      Object o = topLevel.getEntity(key);
      if(o!=null) {
        return o;
      } else {
        o = topLevel.getPort(key);
        if(o!=null) {
          return o;
        } else {
          return topLevel.getRelation(key);
        }
      }
    } else {
      return null;
    }
  }
}
