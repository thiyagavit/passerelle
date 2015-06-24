package fr.soleil.bossanova.gui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import ptolemy.actor.Director;

import net.infonode.docking.View;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.action.ApplyDirectorChangeToAllStepsAction;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewer;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewerTableModel;
import fr.soleil.bossanova.model.DirectorType;
import fr.soleil.bossanova.resources.Icons;

public class BossaNovaViewFactory {

	public final static String LOG_PANEL = "Logs";
	public final static String PARAMETERS_PANEL = "Parameters";
	public final static String BATCH_PANEL = "Batch";
	// Bug 17641
	// public final static String VIEW_PANEL = "View"; 
	public final static String DIRECTOR_PANEL = "Director";

	private BossaNovaSequencerImpl sequencer;
	private Map<String, View> viewMap;

	public BossaNovaViewFactory( /*Bossanova2 application,*/ BossaNovaSequencerImpl sequencer){
		//this.application = application;
		this.sequencer = sequencer;
		viewMap = new WeakHashMap<String, View>();
	}

	public View getView(String viewName) {
		View result = null;
		result = viewMap.get(viewName);
		if (result == null){
			result = createView(viewName);
			viewMap.put(viewName, result);
		}
		return result;
	}
	public View createView(String viewName){
		View result;
		Icon icon = null;
		Component comp = null;
		if (BATCH_PANEL.equals(viewName)) {
			comp = new BatchViewer(/*application,*/ sequencer);

			BatchViewerTableModel tableModel = ((BatchViewer)comp).getModel();
			if (tableModel != null) {
				sequencer.addObserver(tableModel);
			}
			icon = Icons.getIcon("bossanova.batch");
		} else if (PARAMETERS_PANEL.equals(viewName)) {
			comp = createParameterPanel();
			icon = Icons.getIcon("bossanova.parameters");
		} else if (LOG_PANEL.equals(viewName)){
			comp = BossaNovaData.getSingleton().getApplication().getTracePanel();
			icon = Icons.getIcon("bossanova.logs");
		}
		// bug 17641
		/***
		else if (VIEW_PANEL.equals(viewName)){
			comp = new JPanel();
			icon = Icons.getIcon("bossanova.view");
		}
		***/
		// End bug 17641
		 else if (DIRECTOR_PANEL.equals(viewName)){
			comp = createDirectorPanel();
			icon = Icons.getIcon("bossanova.director");
		}
		result = new View(viewName, icon, comp);
		result.setName(viewName);
		return result;
	}
	private JPanel createParameterPanel() {
		JPanel parametersPanel = null;
		try {
			parametersPanel = new JPanel();
			parametersPanel.setLayout(new BorderLayout());

			// we put an empty border to this panel
			// no !
			// application.getUIScrollPane().setBorder(BorderFactory.createTitledBorder("Parameters"));

			parametersPanel.add(BossaNovaData.getSingleton().getApplication().getParameterScrollPane(), BorderLayout.CENTER);
			parametersPanel.setBorder(BorderFactory.createEmptyBorder());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parametersPanel;
	}
	private JPanel createDirectorPanel(){
		JPanel result = new JPanel(new BorderLayout());
		try {
			Director director = sequencer.createBatchDirector(DirectorType.SOLEILSTD);
			BossaNovaData.getSingleton().getApplication().renderDirectorCfgPanel(director, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton applyButton = new JButton(new ApplyDirectorChangeToAllStepsAction(sequencer));
		buttonPanel.add(applyButton);
		result.add(buttonPanel,BorderLayout.SOUTH);
		result.setBorder(BorderFactory.createEmptyBorder());

		return result;
	}
}
