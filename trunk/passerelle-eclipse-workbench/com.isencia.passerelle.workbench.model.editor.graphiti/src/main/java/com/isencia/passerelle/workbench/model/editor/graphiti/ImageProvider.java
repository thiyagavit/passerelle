package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.ui.platform.AbstractImageProvider;

public class ImageProvider extends AbstractImageProvider {

  private static final String ROOT_FOLDER_FOR_IMG = "icons/";

  @Override
  protected void addAvailableImages() {
    // outline
    addImageFilePath(ImageConstants.IMG_OUTLINE_TREE, ROOT_FOLDER_FOR_IMG + "tree.gif");
    addImageFilePath(ImageConstants.IMG_OUTLINE_THUMBNAIL, ROOT_FOLDER_FOR_IMG + "thumbnail.gif");
    
    addImageFilePath(ImageConstants.IMG_ACTOR, ROOT_FOLDER_FOR_IMG + "actor.gif");
    addImageFilePath(ImageConstants.IMG_COMPOSITE, ROOT_FOLDER_FOR_IMG + "composite.gif");
    addImageFilePath(ImageConstants.IMG_DIRECTOR, ROOT_FOLDER_FOR_IMG + "director.gif");
    addImageFilePath(ImageConstants.IMG_INPUTPORT, ROOT_FOLDER_FOR_IMG + "input.gif");
    addImageFilePath(ImageConstants.IMG_OUTPUTPORT, ROOT_FOLDER_FOR_IMG + "output.gif");
    addImageFilePath(ImageConstants.IMG_PARAMETER, ROOT_FOLDER_FOR_IMG + "parameter.gif");
  }
}