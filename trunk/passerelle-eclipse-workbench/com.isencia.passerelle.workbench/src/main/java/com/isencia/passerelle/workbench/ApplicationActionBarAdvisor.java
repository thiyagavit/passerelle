package com.isencia.passerelle.workbench;

import java.util.HashSet;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
	
	// File Actions
    private IContributionItem newWizardShortList;
    private IWorkbenchAction closeAction;
    private IWorkbenchAction createSubmodelAction;
    private IWorkbenchAction closeAllAction;
    private IWorkbenchAction saveAction;
    private IWorkbenchAction saveAsAction;
    private IWorkbenchAction saveAllAction;
    private IWorkbenchAction exitAction;
    // Edit Actions
    private IWorkbenchAction undoAction;
    private IWorkbenchAction redoAction;
    
    // Import/Export
    private IWorkbenchAction importAction;  
    private IWorkbenchAction exportAction;
    
    
    private IWorkbenchAction cutAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction closeEditorAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction deleteAction;
    
    // Run Actions
    private IAction runConfigurationsAction;
    
    // Window Actions
    private IContributionItem perspectiveList;
    private IContributionItem viewList;
    private IAction preferencesAction;
    
	// Help Actions
	private IWorkbenchAction showHelpAction; // NEW
	private IWorkbenchAction searchHelpAction; // NEW
	private IWorkbenchAction dynamicHelpAction; // NEW    

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    /**
     * Creates the actions and registers them.
     * Registering is needed to ensure that key bindings work.
     * The corresponding commands keybindings are defined in the plugin.xml file.
     * Registering also provides automatic disposal of the actions when
     * the window is closed.
     * 
     */
    protected void makeActions(final IWorkbenchWindow window) {
    	// File
    	closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);
        createSubmodelAction = ActionFactory.EXPORT.create(window);
        register(createSubmodelAction);
        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);
        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);
        saveAsAction = ActionFactory.SAVE_AS.create(window);
        register(saveAsAction);
        saveAllAction = ActionFactory.SAVE_ALL.create(window);
        register(saveAllAction);
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        //Import - Export
        importAction = ActionFactory.IMPORT.create(window);
        register(importAction);
        exportAction = ActionFactory.EXPORT.create(window);
        register(exportAction);
        // Edit
    	undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);
    	redoAction = ActionFactory.REDO.create(window);
        register(redoAction);
    	cutAction = ActionFactory.CUT.create(window);
        register(cutAction);
    	copyAction = ActionFactory.COPY.create(window);
        register(copyAction);
    	pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);
    	deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);

        closeEditorAction = ActionFactory.CLOSE.create(window);
        register(closeEditorAction);

        // Run
//        runConfigurationsAction = new org.eclipse.debug.ui.actions.internal.ui.actions.OpenRunConfigurations();
//        register(runConfigurationsAction);
        
        // Window
        perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
        newWizardShortList = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(window);
        preferencesAction = ActionFactory.PREFERENCES.create(window);
        register(preferencesAction);
        
		// Help
		showHelpAction = ActionFactory.HELP_CONTENTS.create(window); // NEW
		register(showHelpAction); // NEW

		searchHelpAction = ActionFactory.HELP_SEARCH.create(window); // NEW
		register(searchHelpAction); // NEW

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window); // NEW
		register(dynamicHelpAction); // NEW
        
        // Remove unwanted actions
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor actionSets[] = reg.getActionSets();
        HashSet<String> actionsToBeRemoved = new HashSet<String>();
        actionsToBeRemoved.add("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");
        actionsToBeRemoved.add("org.eclipse.ui.edit.text.actionSet.openExternalFile");
        actionsToBeRemoved.add("org.eclipse.ui.actionSet.openFiles");
//        actionsToBeRemoved.add("org.eclipse.ui.WorkingSetActionSet");
//        actionsToBeRemoved.add("org.eclipse.update.ui.softwareUpdates");
        for(int i = 0; i < actionSets.length; i++) {
            if(actionsToBeRemoved.contains(actionSets[i].getId()))
            {
                org.eclipse.core.runtime.IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
                reg.removeExtension(ext, new Object[] {
                    actionSets[i]
                });
            }
        }
        // Remove unwanted views
		HashSet<String> viewsToBeRemoved = new HashSet<String>();
		viewsToBeRemoved.add("org.eclipse.ui.views.TaskList");
		viewsToBeRemoved.add("org.eclipse.ui.views.ProblemView");
		viewsToBeRemoved.add("org.eclipse.ui.views.BookmarkView");
		viewsToBeRemoved.add("org.eclipse.ui.views.ProgressView");
		viewsToBeRemoved.add("org.eclipse.ui.views.TaskList");
		ViewRegistry viewReg = (ViewRegistry) WorkbenchPlugin.getDefault()
				.getViewRegistry();
		IViewDescriptor viewDescriptors[] = viewReg.getViews();
		for (int i = 0; i < viewDescriptors.length; i++) {
			if (viewsToBeRemoved.contains(viewDescriptors[i].getId())
					&& (viewDescriptors[i] instanceof ViewDescriptor)) {
				ViewDescriptor descriptor = (ViewDescriptor) viewDescriptors[i];
				org.eclipse.core.runtime.IExtension ext = descriptor
						.getConfigurationElement().getDeclaringExtension();
				viewReg.removeExtension(ext,
						new Object[] { viewDescriptors[i] });
			}
		}
	}
    
    protected void fillMenuBar(IMenuManager menuBar) {
    	// TODO replace constants with Intl Strings Messages.getString("ApplicationActionBarAdvisor.window")
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager fileNewWizardMenu = new MenuManager("New");
		MenuManager editMenu = new MenuManager("Edit" , IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$        
		MenuManager runMenu = new MenuManager("&Run" , IWorkbenchActionConstants.M_LAUNCH); //$NON-NLS-1$        
		MenuManager windowMenu = new MenuManager("&Window" , IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$        
        MenuManager windowPerspectiveMenu = new MenuManager("Open Perspective");
        MenuManager windowShowViewMenu = new MenuManager("Show View");
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(runMenu);
		menuBar.add(windowMenu);        
        menuBar.add(helpMenu);
        
        // File
        fileNewWizardMenu.add(newWizardShortList);
        fileMenu.add(fileNewWizardMenu);
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(new Separator());
        fileMenu.add(importAction);
        fileMenu.add(exportAction);
		fileMenu.add(new Separator());
        fileMenu.add(exitAction);
		// Edit
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.add(new Separator());
        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(new Separator());
        editMenu.add(deleteAction);
        
		// Window
        windowPerspectiveMenu.add(perspectiveList);
        windowMenu.add(windowPerspectiveMenu);
        windowShowViewMenu.add(viewList);
        windowMenu.add(windowShowViewMenu);
		windowMenu.add(new Separator());
		windowMenu.add(preferencesAction);
        
		// Help
		helpMenu.add(showHelpAction); // NEW
		helpMenu.add(searchHelpAction); // NEW
		helpMenu.add(dynamicHelpAction); // NEW
        
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        // File
    	IToolBarManager fileToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    	fileToolbar.add(saveAction);
    	fileToolbar.add(saveAllAction);
        coolBar.add(new ToolBarContributionItem(fileToolbar, "file"));
    	
        // Edit
    	IToolBarManager editToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    	editToolbar.add(undoAction);
    	editToolbar.add(redoAction);
    	editToolbar.add(new Separator());
    	editToolbar.add(cutAction);
    	editToolbar.add(copyAction);
    	editToolbar.add(pasteAction);
    	editToolbar.add(new Separator());
    	editToolbar.add(deleteAction);
        coolBar.add(new ToolBarContributionItem(editToolbar, "edit"));
    	
        // Run
    	IToolBarManager runToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
//    	runToolbar.add(undoAction);
//    	runToolbar.add(redoAction);
        coolBar.add(new ToolBarContributionItem(runToolbar, "run"));
    	
        // Additions
        coolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

    }
}
