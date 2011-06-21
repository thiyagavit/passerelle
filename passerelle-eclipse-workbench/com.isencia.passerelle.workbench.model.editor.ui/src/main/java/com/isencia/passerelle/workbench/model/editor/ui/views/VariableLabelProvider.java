package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.util.ptolemy.IAvailableChoices;
import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CellEditorAttribute;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
import com.isencia.passerelle.workbench.util.ListUtils;

public class VariableLabelProvider extends ColumnLabelProvider {

	private final ActorAttributesView actorAttributesView;

	public VariableLabelProvider(ActorAttributesView actorAttributesView) {
		this.actorAttributesView = actorAttributesView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {

		if (element instanceof GeneralAttribute) {
			return ((GeneralAttribute)element).getValue();
		}
		if (element instanceof CellEditorAttribute) {
			final String text = ((CellEditorAttribute) element)
					.getRendererText();
			if (text != null)
				return text;
		}

		final Attribute attr = (Attribute) element;
		if (attr instanceof Variable) {
			Variable param = (Variable) attr;
			try {
				if (param.getToken() != null
						&& param.getToken() instanceof BooleanToken) {
					return "";
				}
			} catch (Exception ignored) {
				// There is another exception which will show if this happens.
			}

			String label = element == null ? "" : param.getExpression();

			if (param instanceof StringChoiceParameter) {
				final IAvailableChoices choice = ((StringChoiceParameter) param)
						.getAvailableChoices();
				final Map<String, String> vis = choice.getVisibleChoices();
				if (vis != null) {
					final StringBuilder buf = new StringBuilder();
					final List<String> vals = ListUtils.getList(label);
					for (int i = 0; i < vals.size(); i++) {
						buf.append(vis.get(vals.get(i)));
						if (i < vals.size() - 1) {
							buf.append(", ");
						}
					}
					label = buf.toString();
				}
			}
			return label;
		}
		if (attr instanceof StringAttribute) {
			return ((StringAttribute)attr).getExpression();
		}
		return "";

	}

	private Image ticked, unticked;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {

		if (element instanceof String)
			return null;

		if (element instanceof Variable) {
			final Variable param = (Variable) element;
			try {
				if (param.getToken() != null
						&& param.getToken() instanceof BooleanToken) {
					if (((BooleanToken) param.getToken()).booleanValue()) {
						if (ticked == null)
							ticked = Activator.getImageDescriptor(
									"icons/ticked.png").createImage();
						return ticked;
					} else {
						if (unticked == null)
							unticked = Activator.getImageDescriptor(
									"icons/unticked.gif").createImage();
						return unticked;

					}
				}
			} catch (Exception ignored) {
				// There is another exception which will show if this happens.
			}
		}
		return null;
	}

	public void dispose() {
		super.dispose();

	}
}
