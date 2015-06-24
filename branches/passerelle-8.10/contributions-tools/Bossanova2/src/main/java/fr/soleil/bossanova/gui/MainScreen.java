package fr.soleil.bossanova.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.slf4j.LoggerFactory;

import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;
import fr.esrf.tangoatk.widget.util.Splash;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;
import fr.soleil.bossanova.gui.action.DefaultWindowLayoutAction;
import fr.soleil.bossanova.gui.action.ShowViewAction;
import fr.soleil.bossanova.gui.view.BossaNovaViewFactory;
import fr.soleil.bossanova.gui.view.batchViewer.BatchViewer;
import fr.soleil.bossanova.model.DirectorType;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.resources.Icons;

@SuppressWarnings("serial")
public class MainScreen extends JFrame implements Observer, ItemListener {

	// Docking Factory
	private BossaNovaViewFactory viewFactory;

	// Root docking Window
	private RootWindow rootWindow;
	private byte[] defaultLayout;

	// Panels
	private JPanel parametersPanel = null;
	private JPanel directorPanel = null;
	// private JPanel viewPanel = null;

	// Reference to the toolbar button
	private JButton insertStepUpBt = null;
	private JButton deleteStepBt = null;
	private JButton moveUpStepBt = null;
	private JButton moveDownStepBt = null;
	private JButton selectDeselectAllStepBt = null;
	//private JCheckBox useBatchDirectorCheckBox = null;
	private JCheckBox useDataStorageCheckBox = null;

	private JToolBar toolBar;
	private JMenuBar menuBar;
	private BatchViewer batchViewerPanel;
	private final BossaNovaSequencerImpl sequencer;
	private Splash splash;
	private JComboBox directorBox;
	private final boolean standAlone;

	public MainScreen(String title, final boolean standAlone) {
		super(title);
		this.standAlone = standAlone;
		sequencer = BatchManager.getSequencer();
		sequencer.addObserver(this);

		// Init UI
		if (standAlone) {
			initSplashScreen();
		}

		//
		Runnable runnable = new Runnable() {
			public void run() {
				initUI();
				if (standAlone) {
					displayFrame();
				}
			}
		};

		try {
			if (Thread.currentThread().getName().startsWith("AWT-Event")) {
				// GBS runs everything in the AWT EventDispatchThread
				runnable.run();
			} else {
				// Default Java apps are launched from Main Thread
				// We have to do this since Infonode is not thread safe
				SwingUtilities.invokeAndWait(runnable);
			}
		} catch (Exception e) {
            // Bug 18567
			LoggerFactory.getLogger(this.getClass()).error(e.toString());
			e.printStackTrace();
		}

	}

	private void initUI() {
		viewFactory = new BossaNovaViewFactory( sequencer);
		if (splash != null) {
			splash.setMessage("Creating UI");
			splash.progress(1);
		}

		ImageIcon ii = (ImageIcon) Icons.getIcon("bossanova.main");
		setIconImage(ii.getImage());

		setLayout(new BorderLayout());

		ViewMap viewMap = new ViewMap();

		// Batch Panel
		View batchView = viewFactory.getView(BossaNovaViewFactory.BATCH_PANEL);
		batchViewerPanel = (BatchViewer) batchView.getComponent();

		// Parameters Panel
		View parametersView = viewFactory
				.getView(BossaNovaViewFactory.PARAMETERS_PANEL);
		setParametersPanel((JPanel) parametersView.getComponent());

		// Director Panel
		JPanel northPanel = new JPanel(new BorderLayout());
		View directorView = viewFactory
				.getView(BossaNovaViewFactory.DIRECTOR_PANEL);
		setDirectorPanel((JPanel) directorView.getComponent());
		directorBox = new JComboBox(new Object[] {DirectorType.SOLEILSTD});
		JLabel directorLabel = new JLabel("Director Type:\t");
		northPanel.add(directorLabel, BorderLayout.WEST);
		northPanel.add(directorBox, BorderLayout.CENTER);
		getDirectorPanel().add(northPanel, BorderLayout.NORTH);

		// Log Panel
		View logView = viewFactory.getView(BossaNovaViewFactory.LOG_PANEL);

		// Viewer Panel
		// Bug 17641
		/***
		View viewView = viewFactory.getView(BossaNovaViewFactory.VIEW_PANEL);
		viewPanel = (JPanel) viewView.getComponent();

		***/
		initDocking(viewMap, batchView, parametersView, directorView, logView/*, viewView */);
		// End bug 17641

		initToolbar();
		initMenus();
		addListeners();
		if (splash != null) {
			splash.setMessage("Done");
			splash.progress(2);
			splash.setVisible(false);
		}
	}

	private void initDocking(ViewMap viewMap, View batchView,
			View parametersView, View directorView, View logView /*, View viewView*/ ) {
		// Add views to view Map
		int i = 0;
		viewMap.addView(i++, batchView);
		viewMap.addView(i++, parametersView);
		viewMap.addView(i++, directorView);
		viewMap.addView(i++, logView);
		// Bug 17641
		// viewMap.addView(i++, viewView);
		// End bug 17641

		rootWindow = DockingUtil.createRootWindow(viewMap, true);
		prepareRootWindow();

		// Bug 17641
        //TabWindow eastTab = new TabWindow(new DockingWindow[] { parametersView,
        //        viewView });
        TabWindow eastTab = new TabWindow(new DockingWindow[] { parametersView, });
        // End bug 17641
		eastTab.setSelectedTab(0);

		SplitWindow westWindow = new SplitWindow(false, 0.75f, batchView,
				logView);
		SplitWindow eastWindow = new SplitWindow(false, 0.25f, directorView,
				eastTab);

		rootWindow
				.setWindow(new SplitWindow(true, 0.4f, westWindow, eastWindow));
		getContentPane().add(rootWindow);

		storeDefaultLayout();
		loadUserLayout();
	}

	private void storeDefaultLayout() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			rootWindow.write(out, false);
			out.close();
			defaultLayout = bos.toByteArray();
			bos.close();
			// Removing null assignments to correct  Sonar Critical violation JC Pret Jan 2011
			// bos = null;
			// out = null;
			// End Removing null assignments...
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prepareRootWindow() {
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getRootWindowProperties().getDockingWindowProperties()
				.setUndockEnabled(false);
		DockingWindowsTheme theme = new ShapedGradientDockingTheme();
		// Apply theme
		rootWindow.getRootWindowProperties().addSuperObject(
				theme.getRootWindowProperties());

		if (!standAlone) {
			rootWindow.getRootWindowProperties().getDockingWindowProperties()
					.setCloseEnabled(false);
		}
	}

	private void loadUserLayout() {
		ScreenManager.loadWindowLayoutPreferences(rootWindow, defaultLayout);
	}

	private void initSplashScreen() {
		splash = new Splash((ImageIcon) (Icons.getIcon("bossanova.splash")),
				Color.BLACK);
		splash.initProgress();
		splash.setMaxProgress(2);
		splash.setTitle("BossaNova 2");
		splash.setCopyright("Synchrotron-SOLEIL");

	}

	private void displayFrame() {
		sizeAndPlaceFrame();
		validate();
		setVisible(true);

	}

	// Bug 17641
	/***
	public void displayViewComponent(Component viewComp, Step step) {
		// we remove the existing view component
		viewPanel.removeAll();
		if (viewComp == null) {
			String labelText = (step == null) ? "" : "Step " + step.getName()
					+ " is running";
			viewPanel.add(new JLabel(labelText));

		} else {
			viewPanel.add("View", viewComp);
		}
		viewPanel.validate();
		viewPanel.repaint();
	}
	***/

	private void sizeAndPlaceFrame() {
		setSize(new Dimension(1024, 768));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	// --------------------------------------------------------------------------
	// ----
	// LISTENERS STUFF
	// --------------------------------------------------------------------------
	// ----
	private void addListeners() {
		batchViewerPanel.addListeners();
		addWindowListener(new BossanovaWindowListener(this.rootWindow));
		addMouseListenerToAllComponents(this);
	}

	private void addMouseListenerToAllComponents(Component comp) {
		String compPackageName = comp.getClass().getPackage().getName();
		// DO NOT ADD THIS LISTENER TO DOCKING STUFF !!!!!
		// Dirty Hack :(
		if (!compPackageName.startsWith("net.infonode.")) {
			comp.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					try {
						if (batchViewerPanel.getTable() != e.getComponent()) {
							batchViewerPanel.getColumnModel().getComboEditor()
									.stopCellEditing();
						}
					} catch (Exception ex) {
						// SILENT CATCH
					}
				}
			});
		}
		if (comp instanceof Container) {
			Container container = (Container) comp;
			Component[] comps = container.getComponents();
			for (Component component : comps) {
				addMouseListenerToAllComponents(component);
			}
		}

	}

	// --------------------------------------------------------------------------
	// ----
	// MENU STUFF
	// --------------------------------------------------------------------------
	// ----
	private void initMenus() {
		// main menu
		Set<String> hideItemsSet = new HashSet<String>();
		hideItemsSet.add(HMIMessages.MENU_TEMPLATES);
		hideItemsSet.add(HMIMessages.MENU_TRACING);
		hideItemsSet.add(HMIMessages.MENU_OPEN);
		hideItemsSet.add(HMIMessages.MENU_CLOSE);
		hideItemsSet.add(HMIMessages.MENU_SAVE);
		hideItemsSet.add(HMIMessages.MENU_SAVEAS);
		menuBar = BossaNovaData.getSingleton().getApplication().createDefaultMenu(null, hideItemsSet);
		BossaNovaData.getSingleton().getApplication().addPrefsMenu(menuBar);
		addExtraFileMenuElements(menuBar);
		addExtraPreferenceMenuElements(menuBar);
		addWindowMenu(menuBar);
		addEditMenu(menuBar);

		StateMachine.getInstance().compile();
		StateMachine.getInstance().transitionTo(StateMachine.READY);

		setJMenuBar(menuBar);
	}

	protected void addWindowMenu(JMenuBar menuBar) {
		JMenu winMenu = new JMenu("Window");
		winMenu.setMnemonic('w');
		JMenuItem restoreSplitMenuItem = new JMenuItem(
				new DefaultWindowLayoutAction(this));
		winMenu.add(restoreSplitMenuItem);

		winMenu.add(new ShowViewAction(viewFactory
				.getView(BossaNovaViewFactory.BATCH_PANEL)));
		winMenu.add(new ShowViewAction(viewFactory
				.getView(BossaNovaViewFactory.DIRECTOR_PANEL)));
		winMenu.add(new ShowViewAction(viewFactory
				.getView(BossaNovaViewFactory.PARAMETERS_PANEL)));
		// Bug 17641
		//winMenu.add(new ShowViewAction(viewFactory
		//		.getView(BossaNovaViewFactory.VIEW_PANEL)));
		// End Bug 17641
		winMenu.add(new ShowViewAction(viewFactory
				.getView(BossaNovaViewFactory.LOG_PANEL)));
		menuBar.add(winMenu);

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, menuBar);
	}

	protected void addEditMenu(JMenuBar menuBar) {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		List<JMenuItem> menuItems = this.batchViewerPanel.getMenuItems();
		for (JMenuItem menuItem2 : menuItems) {
			JMenuItem menuItem = menuItem2;
			editMenu.add(menuItem);
		}

		menuBar.add(editMenu, 1);

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, menuBar);
	}

	private void addExtraPreferenceMenuElements(JMenuBar superMenuBar) {
		JMenu prefMenu = superMenuBar.getMenu(2);
		if (prefMenu != null) {
			prefMenu.addSeparator();
			prefMenu.add(new JMenuItem(ScreenManager
					.getOpenActorListSelectorViewerAction()));

			// Bug 17638
    		JMenuItem layoutMenuItem = prefMenu.getItem(0);
    		layoutMenuItem.setText("Parameters Layout");
    		// End bug 17638
		}
	}

	private void addExtraFileMenuElements(JMenuBar superMenuBar) {
		JMenu fileMenu = superMenuBar.getMenu(0);
		// NEW
		JMenuItem fileNewMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_NEW), HMIMessages.getString(
				HMIMessages.MENU_NEW + HMIMessages.KEY).charAt(0));
		fileNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_MASK));
		fileNewMenuItem.addActionListener(ScreenManager.getNewBatchAction());
		fileNewMenuItem.setIcon(Icons.getIcon("bossanova.new"));
		// OPEN
		JMenuItem fileOpenMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_OPEN), HMIMessages.getString(
				HMIMessages.MENU_OPEN + HMIMessages.KEY).charAt(0));
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		fileOpenMenuItem.addActionListener(ScreenManager.getLoadBatchAction());
		fileOpenMenuItem.setIcon(Icons.getIcon("bossanova.open"));
		// SAVE
		JMenuItem fileSaveMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_SAVE), HMIMessages.getString(
				HMIMessages.MENU_SAVE + HMIMessages.KEY).charAt(0));
		fileSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		fileSaveMenuItem.addActionListener(ScreenManager.getSaveBatchAction());
		fileSaveMenuItem.setIcon(Icons.getIcon("bossanova.save"));
		// SAVE AS
		JMenuItem fileSaveAsMenuItem = new JMenuItem(HMIMessages
				.getString(HMIMessages.MENU_SAVEAS), HMIMessages.getString(
				HMIMessages.MENU_SAVEAS + HMIMessages.KEY).charAt(0));
		fileSaveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		fileSaveAsMenuItem.addActionListener(ScreenManager
				.getSaveBatchAsAction());
		fileSaveAsMenuItem.setIcon(Icons.getIcon("bossanova.saveas"));

		// IMPORT FILE
		JMenuItem fileImportMenuItem = new JMenuItem(ScreenManager
				.getImportBatchAfterAction(sequencer));
		fileImportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				InputEvent.CTRL_MASK));

		// REFRESH REPO
		JMenuItem refreshRepoMenuItem = new JMenuItem(ScreenManager
				.getRefreshSequenceRepositoryAction());
		refreshRepoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, InputEvent.CTRL_MASK));
		refreshRepoMenuItem.setIcon(Icons.getIcon("bossanova.reload"));

		// INSERT INTO MENU
		JMenuItem quitMenuItem = fileMenu.getItem(fileMenu.getItemCount() - 1);
		quitMenuItem.setIcon(Icons.getIcon("bossanova.quit"));
		// Bug 17628
		quitMenuItem.addActionListener(ScreenManager.getQuitAction());

		fileMenu.add(fileNewMenuItem, 0);
		fileMenu.add(fileOpenMenuItem, 1);
		fileMenu.add(fileSaveMenuItem, 2);
		fileMenu.add(fileSaveAsMenuItem, 3);
		fileMenu.addSeparator();
		fileMenu.add(fileImportMenuItem, 4);
		fileMenu.add(refreshRepoMenuItem, 5);

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVE,
				fileSaveMenuItem);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVEAS,
				fileSaveAsMenuItem);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_OPEN,
				fileOpenMenuItem);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_NEW, fileNewMenuItem);
		StateMachine.getInstance().registerActionForState(StateMachine.READY,
				HMIMessages.MENU_OPEN, fileOpenMenuItem);
		StateMachine.getInstance().registerActionForState(StateMachine.READY,
				HMIMessages.MENU_NEW, fileNewMenuItem);
	}

	// --------------------------------------------------------------------------
	// ----
	// TOOLBAR STUFF
	// --------------------------------------------------------------------------
	// ----
	private void initToolbar() {
	  //Bug 23752
        toolBar = BossaNovaData.getSingleton().getApplication().createToolbarWithoutSave();
        // End Bug 23752
		getContentPane().add(toolBar, BorderLayout.NORTH);

        // Bug 17629
        // Removed Synthesis report generation button

		toolBar.addSeparator();
		toolBar.add(ScreenManager.getAddStepAction(sequencer));
		deleteStepBt = toolBar.add(ScreenManager.getDeleteStepAction(sequencer,
				batchViewerPanel.getTable()));
		insertStepUpBt = toolBar.add(ScreenManager.getInsertStepUpAction(
				sequencer, batchViewerPanel.getTable()));
		moveUpStepBt = toolBar.add(ScreenManager.getMoveUpStepAction(sequencer,
				batchViewerPanel.getTable()));
		moveDownStepBt = toolBar.add(ScreenManager.getMoveDownStepAction(
				sequencer, batchViewerPanel.getTable()));
		toolBar.addSeparator();
		selectDeselectAllStepBt = toolBar.add(ScreenManager
				.getSelectDeselectAllStepAction(sequencer, batchViewerPanel
						.getTable()));
		toolBar.addSeparator();

		// useBatchDirectorCheckBox = new JCheckBox("Use BossaNova Director");
		// useBatchDirectorCheckBox.addItemListener(this);
		// toolBar.add(useBatchDirectorCheckBox);

		useDataStorageCheckBox = new JCheckBox("NeXuS Storage");
		useDataStorageCheckBox.addItemListener(this);
		// toolBar.add(useDataStorageCheckBox);

		JButton buttonSave = new JButton(ScreenManager.getSaveBatchAction());
		buttonSave.setText("");
		JButton buttonOpen = new JButton(ScreenManager.getLoadBatchAction());
		buttonOpen.setText("");
		int i = 0;
		toolBar.add(buttonOpen, i++);
		toolBar.add(buttonSave, i++);
		toolBar.add(new JToolBar.Separator(), i++);
		disableStepButton();
	}

	public void enableStepButton() {
		insertStepUpBt.setEnabled(true);
		deleteStepBt.setEnabled(true);
		moveUpStepBt.setEnabled(true);
		moveDownStepBt.setEnabled(true);
		selectDeselectAllStepBt.setEnabled(true);
	}

	public void disableStepButton() {
		insertStepUpBt.setEnabled(false);
		deleteStepBt.setEnabled(false);
		moveUpStepBt.setEnabled(false);
		moveDownStepBt.setEnabled(false);
		selectDeselectAllStepBt.setEnabled(false);
	}

	// --------------------------------------------------------------------------
	// -------------------------------
	// STEP Management
	// --------------------------------------------------------------------------
	// -------------------------------
	public void changeStep() {
		BatchViewer viewer = getBatchViewerPanel();
		if (viewer != null) {

			int selectedIndex = viewer.getSelectedRowIndex();
			if (selectedIndex >= 0) {
				Step currentStep = sequencer.getBatch().getStep(selectedIndex);
				BossaNovaData.getSingleton().getApplication().setSelectedStep(selectedIndex, currentStep);

				// if a batch is running and the selected step were already
				// executed
				// we disable the parameters panels
				int runningIndex = sequencer.getCurrentRunningStepIndex();
				if (selectedIndex <= runningIndex) {
					StateMachine.getInstance().transitionTo(
							sequencer.getModelExecutor().getSuccessState());
					if (selectedIndex == runningIndex) {
						getDirectorPanel().setVisible(false);
					} else {
						getDirectorPanel().setVisible(true);
					}
				} else {
					getDirectorPanel().setVisible(true);
				}

				// we enable insert button
				enableStepButton();
			}
			addMouseListenerToAllComponents(BossaNovaData.getSingleton().getApplication().getConfigPanel());
		}
	}

	public void stopCellEditing() {
		// we need to stop the current editing before launching batch
		CellEditor cellEditor = getBatchViewerPanel().getTable()
				.getCellEditor();
		if (cellEditor != null) {
			cellEditor.stopCellEditing();
		}
	}

	// --------------------------------------------------------------------------
	// -------------------------------------------------------
	// Item Listener implementation
	// --------------------------------------------------------------------------
	// -------------------------------------------------------
	public void itemStateChanged(ItemEvent e) {
		/*if (e.getSource() == useBatchDirectorCheckBox) {
			//sequencer.setUseBatchDirector(!sequencer.useBatchDirector());
		} else*/ if (e.getSource() == useDataStorageCheckBox) {
			sequencer.setUseNexusStorage(!sequencer.useNexusStorage());
		}

	}

	// --------------------------------------------------------------------------
	// -------------------------------------------------------
	// ACCESSORS
	// --------------------------------------------------------------------------
	// -------------------------------------------------------
	public JPanel getParametersPanel() {
		return parametersPanel;
	}

	public void setParametersPanel(JPanel parametersPanel) {
		this.parametersPanel = parametersPanel;
	}

	public JPanel getDirectorPanel() {
		return directorPanel;
	}

	public void setDirectorPanel(JPanel directorPanel) {
		this.directorPanel = directorPanel;
	}

	public BatchViewer getBatchViewerPanel() {
		return batchViewerPanel;
	}

	public void update(Observable o, Object arg) {
		changeStep();

		/***
        if (o instanceof BossaNovaSequencerImpl)
        {
            BossaNovaSequencerImpl.setCurrentBatchSaved(false);
        }
        ***/
	}

	public RootWindow getRootWindow() {
		return this.rootWindow;
	}

	public byte[] getDefaultLayout() {
		return this.defaultLayout.clone();
	}

	public JToolBar getToolBar() {
		return this.toolBar;
	}

	public void disableSaveButton()
	{
        JButton passSaveButton = (JButton)toolBar.getComponent(12);
        passSaveButton.setEnabled(false);
	}

    // Bug 18267 Displaying current batch name
	public void displayCurrentBatchName()
	{
	    batchViewerPanel.displayCurrentBatchName();
	}

}
