package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.INameable;

public class CommentFigure extends RectangleFigure implements INameable{

	public final static int COMMENT_WIDTH = 120;
	public final static int COMMENT_HEIGHT = 40;
	public final static Dimension COMMENT_SIZE = new Dimension(COMMENT_WIDTH,
			COMMENT_HEIGHT);
	public final static Color COMMENT_BACKGROUND_COLOR = new Color(null, 255, 255,
			0);

	protected Label textLabel = new Label();

	public CommentFigure(String name, Image image) {
		super();
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(false);
		setLayoutManager(layout);
		setBackgroundColor(ColorConstants.tooltipBackground);
		setForegroundColor(ColorConstants.black);
		textLabel.setText(name);
		textLabel.setIcon(image);
		textLabel.setOpaque(true);
		textLabel.setBorder(new MarginBorder(20,20,20,20));
		add(textLabel);
		setOutline(true);
  	  	setOpaque(true);
		setBorder(new MarginBorder(1,1,1,1));
	}
	
	public String getName() {
		return this.textLabel.getText();
	}

	public void setName(String name) {
		this.textLabel.setText(name);
	}

}
