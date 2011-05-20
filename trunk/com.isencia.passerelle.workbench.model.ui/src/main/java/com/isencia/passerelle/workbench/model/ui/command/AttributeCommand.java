package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;

public class AttributeCommand extends Command {

	private static Logger logger = LoggerFactory.getLogger(AttributeCommand.class);

	private final Variable attribute;
	private final Object   newValue;
	private final Object   previousValue;
	private final ColumnViewer   viewer;

	public AttributeCommand(final ColumnViewer viewer, Object element, Object newValue) throws IllegalActionException {
		
		super("Set value "+((Variable)element).getDisplayName()+" to "+getVisibleValue(newValue));
		this.viewer        = viewer;
		this.attribute     = (Variable)element;
		this.newValue      = newValue;
		this.previousValue = (!attribute.isStringMode() && attribute.getToken()!=null && attribute.getToken() instanceof BooleanToken)
		                   ? new Boolean (((BooleanToken)attribute.getToken()).booleanValue())
		                   : attribute.getExpression();

	}

	private static Object getVisibleValue(Object newValue) {
		if (newValue instanceof String && newValue.toString().length()>32) {
			return newValue.toString().substring(0, 32)+"...";
		}
		return newValue;
	}

	public void execute() {
        setValue(newValue);
	}
	public void undo() {
		setValue(previousValue);
	}

	private void setValue(final Object value) {
		
		try {
			attribute.requestChange(new ModelChangeRequest(this.getClass(), attribute, "attribute"){
				@Override
				protected void _execute() throws Exception {
					if (value!=null) {
						if (value instanceof Boolean) {
							attribute.setToken(new BooleanToken(((Boolean)value).booleanValue()));
						} else if (value instanceof Number) {
							attribute.setExpression(((Number)value).toString());
						} else if (value instanceof String) {
							attribute.setExpression((String)value);
						} else {
							throw new Exception("Unrecognised value sent to Variable "+attribute.getName());
						}
					} else {
						if (attribute.isStringMode()) {
							attribute.setExpression(null);
						}
					}
				}
			});
			
		} catch (Exception ne) {
			logger.error("Cannot set variable value "+value, ne);
		} finally {
			if (!viewer.getControl().isDisposed()) {
				viewer.cancelEditing();
				viewer.refresh(attribute);
			}
		}
	
	}

}
