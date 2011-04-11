/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.passerelle.actor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.graph.EditorGraphController;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.actor.gui.graph.ViewOpener;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PortConfigurerDialog;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.CustomizeDocumentationAction;
import ptolemy.vergil.basic.GetDocumentationAction;
import ptolemy.vergil.basic.IconController;
import ptolemy.vergil.basic.RemoveCustomDocumentationAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.MoveAction;
import ptolemy.vergil.toolbox.PortSite;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeInteractor;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JContextMenu;

/**
 * PasserelleActorController
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PasserelleActorController extends IconController {
    private final static Logger logger = LoggerFactory.getLogger(PasserelleActorController.class);

    /** The action that handles look inside.  This is accessed by
     *  by ActorViewerController to create a hot key for the editor.
     */
    protected LookInsideAction _lookInsideAction;
    protected SaveInLibraryAction _saveInLibraryAction;
    
    private static Font _portLabelFont = new Font("SansSerif", Font.PLAIN, 8);


    /**
     * @param controller
     */
    public PasserelleActorController(GraphController controller) {
        this(controller,FULL);
    }

    /**
     * @param controller
     * @param access
     */
    public PasserelleActorController(GraphController controller, Access access) {
        super(controller);
        
        Action[] actions = { _getDocumentationAction,
                new CustomizeDocumentationAction(),
                new RemoveCustomDocumentationAction() };
        _menuFactory.addMenuItemFactory(new MenuActionFactory(actions,
                "Documentation"));

        // Note that we also have "Send to Back" and "Bring to Front" in
        // vergil/basic/BasicGraphFrame.java
        Action[] appearanceActions = {
                new MoveAction("Send to Back", MoveAction.TO_FIRST),
                new MoveAction("Bring to Front", MoveAction.TO_LAST) };
        _appearanceMenuActionFactory = new MenuActionFactory(
                appearanceActions, "Appearance");
        _menuFactory.addMenuItemFactory(_appearanceMenuActionFactory);

        
        _lookInsideAction = new LookInsideAction(this);
        _saveInLibraryAction = new SaveInLibraryAction();
        ((NodeInteractor) getNodeInteractor()).setDragInteractor(new LocatableNodeDragInteractor(this));
        if (access == FULL) {
            _menuFactory.addMenuItemFactory(
            		new PortDialogFactory());

            _menuFactory.addMenuItemFactory(
                    new CompositeActorMenuItemFactory(this));
        }
    }

    /**
     * Facade method
     * 
     * @param editMsg
     */
    public void postUndoableEdit(String editMsg) {
        ((EditorGraphController)getController()).postUndoableEdit(editMsg);
    }

    /**
     * Snapshot taken from Ptolemy 4.0.1, adapted to disallow port configuration
     * for Passerelle actors.
     * Adding/removing ports etc in an uncontrolled fashion can break the actor's operation.
     * Passerelle actors that support this, will explicitly contain config parameters to set
     * the nr of ports.
     * 
     * We only allow this uncontrolled port configuration for plain Ptolemy actors.
     * 
     * @author erwin
     */
    private static class PortDialogFactory implements MenuItemFactory {

        ///////////////////////////////////////////////////////////////////
        //// public methods ////

        /**
         * Add an item to the given context menu that will open a dialog to add or
         * remove ports from an object.
         *
         * @param menu
         *            The context menu.
         * @param object
         *            The object whose ports are being manipulated.
         */
        public JMenuItem create(final JContextMenu menu, NamedObj object) {
            JMenuItem retv = null;
            // Removed this method since it was never used. EAL
            // final NamedObj target = _getItemTargetFromMenuTarget(object);
            final NamedObj target = object;

            // Ensure that we actually have a target, and that it's an Entity,
            // but not a Passerelle actor.
            if (target==null || !(target instanceof Entity) || (target instanceof Actor))
                return null;
            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            // Normally, this is a Frame, but just in case, we check.
            // If it isn't a Frame, then the edit parameters dialog
            // will not have the appropriate parent, and will disappear
            // when put in the background.
            // Note, this uses the "new" way of doing dialogs.
            Action configPortsAction = new AbstractAction(_configPorts) {

                public void actionPerformed(ActionEvent e) {
                    Component parent = menu.getInvoker();
                    ModelGraphPanel passerelleGraphPanel = null;
                    while (parent.getParent() != null) {
                    	if(parent instanceof ModelGraphPanel) {
                    		passerelleGraphPanel = (ModelGraphPanel) parent;
                    	}
                        parent = parent.getParent();
                    }
                    // this if for the Passerelle IDE
                    if (passerelleGraphPanel != null) {
                        DialogTableau dialogTableau =
                            DialogTableau.createDialog(
                                (Frame) parent,
                                _configuration,
                                passerelleGraphPanel.getEffigy(),
                                PortConfigurerDialog.class,
                                (Entity) target);
                        if (dialogTableau != null) {
                            dialogTableau.show();
                        }
                    }
                    // this should keep it working in Vergil
                    else if (parent instanceof TableauFrame) {
                        DialogTableau dialogTableau =
                            DialogTableau.createDialog(
                                (Frame) parent,
                                _configuration,
                                ((TableauFrame) parent).getEffigy(),
                                PortConfigurerDialog.class,
                                (Entity) target);
                        if (dialogTableau != null) {
                            dialogTableau.show();
                        }
                    }
                }
            };
            retv = menu.add(configPortsAction, _configPorts);

            return retv;
        }

        /**
         * Set the configuration for use by the help screen.
         *
         * @param configuration
         *            The configuration.
         */
        public void setConfiguration(Configuration configuration) {
            _configuration = configuration;
        }

        ///////////////////////////////////////////////////////////////////
        //// private variables ////

        /** The configuration. */
        private static String _configPorts = "Configure Ports";

//        private static String _configUnits = "Configure Units";

        private Configuration _configuration;
    }

    /** This layout algorithm is responsible for laying out the ports
     *  within an entity.
     */
    public class EntityLayout extends AbstractGlobalLayout {
        /** Create a new layout manager. */
        public EntityLayout() {
            super(new BasicLayoutTarget(getController()));
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Layout the ports of the specified node.
         *  @param node The node, which is assumed to be an entity.
         */
        public void layout(Object node) {
            GraphModel model = getController().getGraphModel();

            // System.out.println("layout = " + node);
            //        new Exception().printStackTrace();
            Iterator nodes = model.nodes(node);
            Vector westPorts = new Vector();
            Vector eastPorts = new Vector();
            Vector southPorts = new Vector();
            Vector northPorts = new Vector();

            while (nodes.hasNext()) {
                Port port = (Port) nodes.next();
                int portRotation = _getCardinality(port);
                int direction = _getDirection(portRotation);
                if (direction == SwingConstants.WEST) {
                    westPorts.add(port);
                } else if (direction == SwingConstants.NORTH) {
                    northPorts.add(port);
                } else if (direction == SwingConstants.EAST) {
                    eastPorts.add(port);
                } else {
                    southPorts.add(port);
                }
            }

            CompositeFigure figure = (CompositeFigure) getLayoutTarget()
                    .getVisualObject(node);

            _reOrderPorts(westPorts);
            _placePortFigures(figure, westPorts, SwingConstants.WEST);
            _reOrderPorts(eastPorts);
            _placePortFigures(figure, eastPorts, SwingConstants.EAST);
            _reOrderPorts(southPorts);
            _placePortFigures(figure, southPorts, SwingConstants.SOUTH);
            _reOrderPorts(northPorts);
            _placePortFigures(figure, northPorts, SwingConstants.NORTH);
        }

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////
        // re-order the ports according to _ordinal property
        private void _reOrderPorts(Vector ports) {
            int size = ports.size();
            Enumeration enumeration = ports.elements();
            Port port;
            StringAttribute ordinal = null;
            int number = 0;
            int index = 0;

            while (enumeration.hasMoreElements()) {
                port = (Port) enumeration.nextElement();
                ordinal = (StringAttribute) port.getAttribute("_ordinal");

                if (ordinal != null) {
                    number = Integer.parseInt(ordinal.getExpression());

                    if (number >= size) {
                        ports.remove(index);

                        try {
                            ordinal.setExpression(Integer.toString(size - 1));
                        } catch (Exception e) {
                            MessageHandler.error(
                                    "Error setting ordinal property", e);
                        }

                        ports.add(port);
                    } else if (number < 0) {
                        ports.remove(index);

                        try {
                            ordinal.setExpression(Integer.toString(0));
                        } catch (Exception e) {
                            MessageHandler.error(
                                    "Error setting ordinal property", e);
                        }

                        ports.add(0, port);
                    } else if (number != index) {
                        ports.remove(index);
                        ports.add(number, port);
                    }
                }

                index++;
            }
        }

        // Place the ports.
        private void _placePortFigures(CompositeFigure figure, List portList,
                int direction) {
            Iterator ports = portList.iterator();
            int number = 0;
            int count = portList.size();

            Figure background = figure.getBackgroundFigure();

            if (background == null) {
                // This could occur if the icon has a _hide parameter.
                background = figure;
            }

            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                Figure portFigure = getController().getFigure(port);

                // If there is no figure, then ignore this port.  This may
                // happen if the port hasn't been rendered yet.
                if (portFigure == null) {
                    continue;
                }

                Rectangle2D portBounds = portFigure.getShape().getBounds2D();
                PortSite site = new PortSite(background, port, number, count,
                        direction);
                number++;

                // NOTE: previous expression for port location was:
                //    100.0 * number / (count+1)
                // But this leads to squished ports with uneven spacing.
                // Note that we don't use CanvasUtilities.translateTo because
                // we want to only get the bounds of the background of the
                // port figure.
                double x = site.getX() - portBounds.getCenterX();
                double y = site.getY() - portBounds.getCenterY();
                portFigure.translate(x, y);

                // If the actor contains a variable named "_showRate",
                // with value true, then visualize the rate information.
                // NOTE: Showing rates only makes sense for IOPorts.
                Attribute showRateAttribute = port.getAttribute("_showRate");

                if (port instanceof IOPort
                        && showRateAttribute instanceof Variable) {
                    boolean showRate = false;

                    try {
                        showRate = ((Variable) showRateAttribute).getToken()
                                .equals(BooleanToken.TRUE);
                    } catch (Exception ex) {
                        // Ignore.
                    }

                    if (showRate) {
                        // Infer the rate.  See DFUtilities.
                        String rateString = "";
                        Variable rateParameter = null;

                        if (((IOPort) port).isInput()) {
                            rateParameter = (Variable) port
                                    .getAttribute("tokenConsumptionRate");

                            if (rateParameter == null) {
                                String altName = "_tokenConsumptionRate";
                                rateParameter = (Variable) port
                                        .getAttribute(altName);
                            }
                        } else if (((IOPort) port).isOutput()) {
                            rateParameter = (Variable) port
                                    .getAttribute("tokenProductionRate");

                            if (rateParameter == null) {
                                String altName = "_tokenProductionRate";
                                rateParameter = (Variable) port
                                        .getAttribute(altName);
                            }
                        }

                        if (rateParameter != null) {
                            try {
                                rateString = rateParameter.getToken()
                                        .toString();
                            } catch (KernelException ex) {
                                // Ignore.
                            }
                        }

                        LabelFigure labelFigure = _createPortLabelFigure(
                                rateString, _portLabelFont, x, y, direction);
                        labelFigure.setFillPaint(Color.BLUE);
                        figure.add(labelFigure);
                    }
                }

                // If the port contains an attribute named "_showName",
                // then render the name of the port as well. If the
                // attribute is a boolean-valued parameter, then
                // show the name only if the value is true.
                Attribute showAttribute = port.getAttribute("_showName");
                String toShow = null;
                if (showAttribute != null) {
                    boolean show = true;

                    if (showAttribute instanceof Parameter) {
                        try {
                            Token token = ((Parameter) showAttribute)
                                    .getToken();

                            if (token instanceof BooleanToken) {
                                show = ((BooleanToken) token).booleanValue();
                            }
                        } catch (IllegalActionException e) {
                            // Ignore. Presence of the attribute will prevail.
                        }
                    }

                    if (show) {
                        toShow = port.getDisplayName();
                    }
                }
                // In addition, if the port contains an attribute
                // called "_showInfo", then if that attribute is
                // a variable, then its value is shown. Otherwise,
                // if it is a Settable, then its expression is shown.
                Attribute showInfo = port.getAttribute("_showInfo");
                try {
                    if (showInfo instanceof Variable
                            && !((Variable) showInfo).isStringMode()) {
                        String value = ((Variable) showInfo).getToken()
                                .toString();
                        if (toShow != null) {
                            toShow += " (" + value + ")";
                        } else {
                            toShow = value;
                        }
                    } else if (showInfo instanceof Settable) {
                        if (toShow != null) {
                            toShow += " ("
                                    + ((Settable) showInfo).getExpression()
                                    + ")";
                        } else {
                            toShow = ((Settable) showInfo).getExpression();
                        }
                    }
                } catch (IllegalActionException e) {
                    toShow += e.getMessage();
                }

                if (toShow != null) {
                    LabelFigure labelFigure = _createPortLabelFigure(toShow,
                            _portLabelFont, x, y, direction);
                    figure.add(labelFigure);
                }
            }
        }
    }


    private class CompositeActorMenuItemFactory implements MenuItemFactory {
    	private PasserelleActorController ctrlr;

        public CompositeActorMenuItemFactory(
				PasserelleActorController ctrlr) {
            this.ctrlr = ctrlr;
		}

		public JMenuItem create(final JContextMenu menu, NamedObj object) {
            JMenuItem retv = null;
            
            if (object != null && object instanceof CompositeEntity) {
            	_saveInLibraryAction.setConfiguration(ctrlr._configuration);
                retv = menu.add(_lookInsideAction,"Look Inside");
//                retv = menu.add(_saveInLibraryAction,"Save In Library");
            }
            return retv;
        }
        
    }
    
    // An action to look inside a composite.
    private static class LookInsideAction extends FigureAction {

    	private PasserelleActorController ctrlr;


        public LookInsideAction(PasserelleActorController ctrlr) {
            super("Look Inside");
            this.ctrlr = ctrlr;
            // For some inexplicable reason, the I key doesn't work here.
            // Use L, which used to be used for layout.
            // Avoid Control_O, which is open file.
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent event) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            if (ctrlr._configuration == null) {
            	Object topLevel = target.toplevel();
            	if(topLevel instanceof Configuration)
            		ctrlr._configuration = (Configuration) topLevel;
            	else {
            		ctrlr._configuration = new Configuration(target.workspace());
            		try {
						new ModelDirectory(ctrlr._configuration, Configuration._DIRECTORY_NAME);
					} catch (Exception e) {
		                MessageHandler.error("Cannot look inside without a configuration.");
		                return;
					}
            	}
            }

            try {
                GraphController gCtrlr = ctrlr.getController();
                ((ViewOpener)gCtrlr).openView(target, ctrlr._configuration);
            } catch (Exception e) {
                MessageHandler.error(
                        "Cannot open view for looking inside.",e);
            }
        }
    }

    /** Return one of {-270, -180, -90, 0, 90, 180, 270} specifying
     *  the orientation of a port. This depends on whether the port
     *  is an input, output, or both, whether the port has a parameter
     *  named "_cardinal" that specifies a cardinality, and whether the
     *  containing actor has a parameter named "_rotatePorts" that
     *  specifies a rotation of the ports.  In addition, if the
     *  containing actor has a parameter named "_flipPortsHorizonal"
     *  or "_flipPortsVertical" with value true, then any ports that end up on the left
     *  or right (top or bottom) will be reversed.
     *  @param port The port.
     *  @return One of {-270, -180, -90, 0, 90, 180, 270}.
     */
    protected static int _getCardinality(Port port) {
        // Determine whether the port has an attribute that specifies
        // which side of the icon it should be on, and whether the
        // actor has an attribute that rotates the ports. If both
        // are present, the port attribute takes precedence.

        boolean isInput = false;
        boolean isOutput = false;
        boolean isInputOutput = false;

        // Figure out what type of port we're dealing with.
        // If ports are not IOPorts, then draw then as ports with
        // no direction.
        if (port instanceof IOPort) {
            isInput = ((IOPort) port).isInput();
            isOutput = ((IOPort) port).isOutput();
            isInputOutput = isInput && isOutput;
        }

        StringAttribute cardinal = null;
        int portRotation = 0;
        try {
            cardinal = (StringAttribute) port.getAttribute("_cardinal",
                    StringAttribute.class);
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter rotationParameter = (Parameter) container
                        .getAttribute("_rotatePorts", Parameter.class);
                if (rotationParameter != null) {
                    Token rotationValue = rotationParameter.getToken();
                    if (rotationValue instanceof IntToken) {
                        portRotation = ((IntToken) rotationValue).intValue();
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }
        if (cardinal == null) {
            // Port cardinality is not specified in the port.
            if (isInputOutput) {
                portRotation += -90;
            } else if (isOutput) {
                portRotation += 180;
            }
        } else if (cardinal.getExpression().equalsIgnoreCase("NORTH")) {
            portRotation = 90;
        } else if (cardinal.getExpression().equalsIgnoreCase("SOUTH")) {
            portRotation = -90;
        } else if (cardinal.getExpression().equalsIgnoreCase("EAST")) {
            portRotation = 180;
        } else if (cardinal.getExpression().equalsIgnoreCase("WEST")) {
            portRotation = 0;
        } else { // this shouldn't happen either
            portRotation += -90;
        }

        // Ensure that the port rotation is one of
        // {-270, -180, -90, 0, 90, 180, 270}.
        portRotation = 90 * ((portRotation / 90) % 4);

        // Finally, check for horizontal or vertical flipping.
        try {
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter flipHorizontalParameter = (Parameter) container
                        .getAttribute("_flipPortsHorizontal", Parameter.class);
                if (flipHorizontalParameter != null) {
                    Token rotationValue = flipHorizontalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == 0 || portRotation == -180) {
                            portRotation += 180;
                        } else if (portRotation == 180) {
                            portRotation = 0;
                        }
                    }
                }
                Parameter flipVerticalParameter = (Parameter) container
                        .getAttribute("_flipPortsVertical", Parameter.class);
                if (flipVerticalParameter != null) {
                    Token rotationValue = flipVerticalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == -270 || portRotation == -90) {
                            portRotation += 180;
                        } else if (portRotation == 90 || portRotation == 270) {
                            portRotation -= 180;
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }

        return portRotation;
    }

    /** Return the direction associated with the specified angle,
     *  which is assumed to be one of {-270, -180, -90, 0, 90, 180, 270}.
     *  @param portRotation The angle
     *  @return One of SwingUtilities.NORTH, SwingUtilities.EAST,
     *  SwingUtilities.SOUTH, or SwingUtilities.WEST.
     */
    protected static int _getDirection(int portRotation) {
        int direction;
        if (portRotation == 90 || portRotation == -270) {
            direction = SwingConstants.NORTH;
        } else if (portRotation == 180 || portRotation == -180) {
            direction = SwingConstants.EAST;
        } else if (portRotation == 270 || portRotation == -90) {
            direction = SwingConstants.SOUTH;
        } else {
            direction = SwingConstants.WEST;
        }
        return direction;
    }
    private LabelFigure _createPortLabelFigure(String string, Font font,
            double x, double y, int direction) {
        LabelFigure label;

        if (direction == SwingConstants.SOUTH) {
            // The 1.0 argument is the padding.
            label = new LabelFigure(string, font, 1.0,
                    SwingConstants.SOUTH_WEST);

            // Shift the label down so it doesn't
            // collide with ports.
            label.translateTo(x, y + 5);

            // Rotate the label.
            AffineTransform rotate = AffineTransform.getRotateInstance(
                    Math.PI / 2.0, x, y + 5);
            label.transform(rotate);
        } else if (direction == SwingConstants.EAST) {
            // The 1.0 argument is the padding.
            label = new LabelFigure(string, font, 1.0,
                    SwingConstants.SOUTH_WEST);

            // Shift the label right so it doesn't
            // collide with ports.
            label.translateTo(x + 5, y);
        } else if (direction == SwingConstants.WEST) {
            // The 1.0 argument is the padding.
            label = new LabelFigure(string, font, 1.0,
                    SwingConstants.SOUTH_EAST);

            // Shift the label left so it doesn't
            // collide with ports.
            label.translateTo(x - 5, y);
        } else { // Must be north.

            // The 1.0 argument is the padding.
            label = new LabelFigure(string, font, 1.0,
                    SwingConstants.SOUTH_WEST);

            // Shift the label right so it doesn't
            // collide with ports.  It will probably
            // collide with the actor name.
            label.translateTo(x, y - 5);

            // Rotate the label.
            AffineTransform rotate = AffineTransform.getRotateInstance(
                    -Math.PI / 2.0, x, y - 5);
            label.transform(rotate);
        }

        return label;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _getDocumentationAction.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public members                        ////

    /** Indicator to give full access to the attribute. */
    public static final Access FULL = new Access();

    /** Indicator to give partial access to the attribute. */
    public static final Access PARTIAL = new Access();

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The appearance menu factory. */
    protected MenuActionFactory _appearanceMenuActionFactory;

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The "get documentation" action. */
    private GetDocumentationAction _getDocumentationAction = new GetDocumentationAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A static enumerator for constructor arguments. */
    protected static class Access {
    }

}
