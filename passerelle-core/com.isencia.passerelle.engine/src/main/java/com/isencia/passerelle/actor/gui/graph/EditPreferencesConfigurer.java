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
package com.isencia.passerelle.actor.gui.graph;

import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

/**
 This class is an editor widget to show/hide port names of all actors in a model.

 @see Configurer
 */
public class EditPreferencesConfigurer extends Query implements ChangeListener,
        QueryListener {

	private static final String PARAMETERS_CHECK_BOX = "parametersCheckBox";
	private static final String PORT_NAMES_CHECK_BOX = "portNamesCheckBox";
	private static String _SINGLETON_PARAMETER = "ptolemy.data.expr.SingletonParameter";

    // The object that this configurer configures.
    private CompositeEntity target;
	private Configuration _configuration;
	private boolean _changed;
    private PtolemyPreferences preferences = null;
    private Parameter showPortNamesParameter;
    private StringParameter showParametersParameter;

	/** Construct a configurer for the specified entity.
     *  @param composite The entity to configure.
     */
    public EditPreferencesConfigurer(Configuration configuration, CompositeEntity composite) {
        super();
        _configuration = configuration;
        this.target = composite;
        
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        try {
            preferences = (PtolemyPreferences) _configuration.getAttribute(
                    PtolemyPreferences.PREFERENCES_WITHIN_CONFIGURATION,
                    PtolemyPreferences.class);
            showPortNamesParameter = (Parameter) preferences.getAttribute("_showPortNames");
            if(showPortNamesParameter==null) {
            	showPortNamesParameter = new Parameter(preferences,"_showPortNames");
            	showPortNamesParameter.setToken(new BooleanToken(false));
            }
            showParametersParameter = (StringParameter) preferences.getAttribute("_showParameters");
            addCheckBox(PORT_NAMES_CHECK_BOX, "Show port names", ((BooleanToken)showPortNamesParameter.getToken()).booleanValue());
            addCheckBox(PARAMETERS_CHECK_BOX, "Show parameters", "All".equals(showParametersParameter.getExpression()));
        } catch (Exception ex) {
            MessageHandler.error("Error reading preferences", ex);
        }
    }

    public void apply() {
        try {
    		boolean showPortNames = getBooleanValue(PORT_NAMES_CHECK_BOX);
    		boolean showParameters = getBooleanValue(PARAMETERS_CHECK_BOX);
            
            if(showParameters)
            	showParametersParameter.setExpression("All");
            else
            	showParametersParameter.setExpression("None");
    		
            // specific for passerelle : also maintain this as a Ptolemy Preference
           	showPortNamesParameter.setToken(new BooleanToken(showPortNames));
           	
			preferences.setAsDefault();
			// show port names is not handled by Ptolemy's preferences update and change listeners etc
			handleShowPortNames(showPortNames);
		} catch (Exception e) {
			MessageHandler.error("Error setting preferences", e);
		}
    }

	private void handleShowPortNames(boolean showPortNames) {
		List<Entity> entities = target.entityList();
		for (Entity entity : entities) {
	        StringBuffer moml = new StringBuffer("<group>");
	        moml.append(getShowNameChangeForPortCollection(showPortNames, entity.portList()));
	        moml.append("</group>");
	        MoMLChangeRequest request = new MoMLChangeRequest(this,
	        		entity, moml.toString(), null);
	        request.setUndoable(true);

	        // NOTE: There is no need to listen for completion or
	        // errors in this change request, since, in theory, it
	        // will just work. Will someone report the error if one
	        // occurs? I hope so...
	        entity.requestChange(request);
		}
	}

	private String getShowNameChangeForPortCollection(boolean showPortNames,
			Collection<Port> ports) {
		StringBuffer momlUpdate = new StringBuffer();
		for (Port port : ports) {
        	momlUpdate.append("<port name=\""
                    + port.getName() + "\">");            
        	boolean isShowSet = _isPropertySet(port, "_showName");
	        if (showPortNames) {
	            if(!isShowSet) {
                    momlUpdate.append(_momlProperty("_showName",
                            _SINGLETON_PARAMETER, "true"));
	            }
	        } else {
	            Attribute attribute = port.getAttribute("_showName");
	    		if(attribute==null) {
                    momlUpdate.append(_momlProperty("_showName",
                            _SINGLETON_PARAMETER, "false"));
	    		} else {
	                momlUpdate.append(_momlDeleteProperty("_showName"));
	            }
            }
	        momlUpdate.append("</port>");
        }
        return momlUpdate.toString();
	}
    
    /** Return true if the property of the specified name is set for
     *  the specified object. A property is specified if the specified
     *  object contains an attribute with the specified name and that
     *  attribute is either not a boolean-valued parameter, or it is a
     *  boolean-valued parameter with value true.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    private boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    private String _momlProperty(String name, String clz, String value) {
        if (clz != null) {
            return "<property name=\"" + name + "\" " + "class = \"" + clz
                    + "\" " + "value = \"" + value + "\"/>";
        }

        return "<property name=\"" + name + "\" " + "value = \"" + value
                + "\"/>";
    }
    private String _momlDeleteProperty(String name) {
        return "<deleteProperty name=\"" + name + "\"/>";
    }



    /** React to the fact that the change has been successfully executed
     *  by doing nothing.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Nothing to do.
    }

    /** React to the fact that the change has failed by reporting it.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if ((change != null) && (change.getSource() != this)) {
            return;
        }

        if ((change != null) && !change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Rename failed: ", exception);
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed = true;
    }

}
