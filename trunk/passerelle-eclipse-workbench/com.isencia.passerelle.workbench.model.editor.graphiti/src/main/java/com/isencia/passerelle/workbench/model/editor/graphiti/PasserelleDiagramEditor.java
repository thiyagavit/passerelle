package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.ui.editor.DefaultPersistencyBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.Page;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.palette.DragSupportBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorPalettePage;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeViewerPage;

public class PasserelleDiagramEditor extends DiagramEditor {

  public final static String EDITOR_ID = "com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramEditor";

  @Override
  protected DefaultPersistencyBehavior createPersistencyBehavior() {
    return new PasserellePersistencyBehavior(this);
  }

  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);

    // this.getGraphicalViewer().addDropTargetListener(new PasserelleTemplateTransferDropTargetListener(this.getGraphicalViewer()));

    getRefreshBehavior().refresh();
  }

  public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
    if (type == ActorPalettePage.class || type == Page.class) {
      ActorTreeViewerPage actorTreeViewPage = new ActorTreeViewerPage(getActionRegistry(), new MyDragSupportBuilder());
      return actorTreeViewPage;
    }

    return super.getAdapter(type);
  }

  private class MyDragSupportBuilder implements DragSupportBuilder {
    @Override
    public void addDragSupport(TreeViewer treeViewer) {
      int ops = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transfers = new Transfer[] { TemplateTransfer.getInstance() };
      SelectionDragAdapter dragListener = new SelectionDragAdapter(treeViewer);
      treeViewer.addDragSupport(ops, transfers, dragListener);
    }
  }

  public class SelectionDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    private TreeViewer fViewer;

    public SelectionDragAdapter(TreeViewer viewer) {
      Assert.isNotNull(viewer);
      fViewer = viewer;
    }

    public Transfer getTransfer() {
      return TemplateTransfer.getInstance();
    }

    public void dragStart(DragSourceEvent event) {
      ISelection _selection = fViewer.getSelection();
      boolean doit = !_selection.isEmpty();
      if (doit) {
        if (_selection instanceof ITreeSelection) {
          ITreeSelection selection = (ITreeSelection) _selection;
          final Object selected = selection.getFirstElement();
          if (selected instanceof PaletteItemDefinition) {
            TemplateTransfer.getInstance().setTemplate(new PaletteItemDefCreationFactory((PaletteItemDefinition) selected));
          }
        } else {
          doit = false;
        }
      }
      event.doit = doit;
   }

    public void dragSetData(DragSourceEvent event) {
      event.data = TemplateTransfer.getInstance().getTemplate();
    }

    public void dragFinished(DragSourceEvent event) {
      TemplateTransfer.getInstance().setTemplate(null);
    }
  }

  public class PaletteItemDefCreationFactory implements CreationFactory {
    PaletteItemDefinition selected;

    public PaletteItemDefCreationFactory(PaletteItemDefinition selected) {
      this.selected = selected;
    }

    public Object getObjectType() {
      return ICreateFeature.class;
    }

    public Object getNewObject() {
      return new ActorCreateFeatureFromPaletteItemDefinition(getDiagramTypeProvider().getFeatureProvider());
    }
  }

}