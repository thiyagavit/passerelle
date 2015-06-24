/*
 * An actor that uses the ImageJ system for displaying an image. This
 * actor is based on the ImageReader actor.
 * 
 * Dan Higgins - NCEAS
 * 
 * @Copyright (c) 2001-2004 The Regents of the University of California.
 * All rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in all
 * copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION 2
 * COPYRIGHTENDKEY
 */

package fr.soleil.ptolemy;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

//////////////////////////////////////////////////////////////////////////
//// ImageJActor
/**
 * This actor launches the ImageJ package from NIH using the input file. see
 * http://rsb.info.nih.gov/ij/ for information on the ImageJ system for image
 * processing
 * 
 * This actor simply extends Sink; it thus has an 'input' multiport The input is
 * assumed to a file name which ImageJ will try to open. ImageJ will succeed in
 * opening a number of different types of image files.
 * 
 * @author Dan Higgins
 */
@SuppressWarnings("serial")
public class ImageJActor extends Sink {

    /**
     * Construct an actor with the given container and name.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the actor cannot be contained by the proposed
     *                container.
     * @exception NameDuplicationException
     *                If the container already has an actor with this name.
     */
    public ImageJActor(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        fileOrURL = new FileParameter(this, "fileOrURL");
        fileOrURL.setExpression(fileName);
        registerConfigurableParameter(fileOrURL);
    }

    // /////////////////////////////////////////////////////////////////
    // // ports and parameters ////

    /**
     * The file name or URL from which to read. This is a string with any form
     * accepted by File Attribute.
     * 
     * @see FileParameter The file name should refer to an image file that is
     *      one of the many image files that ImageJ can display. These include
     *      tiffs, gifs, jpegs, etc. See the ImageJ Help menu or
     *      http://rsb.info.nih.gov/ij/
     */
    public FileParameter fileOrURL;
    private String fileName = "test";

    // public static ImageJ ij;
    private TangoAttribute ap = null;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * If the specified attribute is <i>URL</i>, then close the current file (if
     * there is one) and open the new one.
     * 
     * @param attribute
     *            The attribute that has changed.
     * @exception IllegalActionException
     *                If the specified attribute is <i>URL</i> and the file
     *                cannot be opened.
     */
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == fileOrURL) {
            fileName = fileOrURL.asFile().getPath();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
   *
   */
    @Override
    protected void sendMessage(final ManagedMessage outgoingMessage) throws ProcessingException {
        String className = "";
        try {
            if (!(outgoingMessage.getBodyContent() instanceof TangoAttribute)) {
                final Exception e = null;
                className = outgoingMessage.getBodyContent().getClass().getName();

                ExceptionUtil.throwProcessingException("Input message must of type AttributeProxy (not " + className
                        + ")", this.getName(), e);

            }
            ap = (TangoAttribute) outgoingMessage.getBodyContent();
        } catch (final MessageException e) {
            // e.printStackTrace();
            ExceptionUtil.throwProcessingException(ErrorCode.FATAL, "Cannot get input message", this.getName(), e);
        }
        AttrDataFormat data_format = null;
        try {
            data_format = ap.getAttributeProxy().get_info().data_format;
        } catch (final DevFailed e2) {
            // e2.printStackTrace();
            ExceptionUtil.throwProcessingException(TangoToPasserelleUtil.getDevFailedString(e2, this), ap
                    .getAttributeProxy().fullName(), e2);
        }
        if (data_format != null && !data_format.equals(AttrDataFormat.IMAGE)) {
            ExceptionUtil.throwProcessingException("Attribute is not an image", ap.getAttributeProxy().fullName(),
                    new Exception());
        }
        Double image_d[] = null;
        short image_s[] = null;
        // int width = 0;
        // int height = 0;
        try {
            image_d = ap.readSpecOrImage(Double.class);
            image_s = new short[image_d.length];
            for (int i = 0; i < image_d.length; i++) {
                image_s[i] = (short) image_d[i].doubleValue();
            }

            // width = ap.getDeviceAttribute().getDimX();
            // height = ap.getDeviceAttribute().getDimY();
        } catch (final DevFailed e2) {
            // e2.printStackTrace();
            ExceptionUtil.throwProcessingException(TangoToPasserelleUtil.getDevFailedString(e2, this), ap
                    .getAttributeProxy().fullName(), e2);
        }
//        System.out.println("get image " + ap.getAttributeProxy().fullName());
        // ImagePlus imp = null;
        // ImageProcessor ip = new ShortProcessor(width, height);
        // ip.setPixels(image_s);
        // ip.setColor(Color.red);
        // ip.fill();
        // imp = new ImagePlus();
        // imp = new
        // ImagePlus("Tango image - "+ap.getAttributeProxy().fullName(), new
        // ShortProcessor(width, height));
        // ImageCanvas cc = new ImageCanvas(imp);
        // new CustomWindow(imp, cc);

        // imp.getProcessor().setPixels(image_s);
        // imp.getProcessor().setRoi(0,50,0,50);
        // imp.getProcessor().d
        System.out.println("show image " + ap.getAttributeProxy().fullName());

        // imp.show("coucou");

        System.out.println("firing ImageJActor");

        /*
         * if (ij == null) { if (IJMacro.ij!=null) {// IJMacro may already have
         * a static instance of an ImageJ class; if so, use it ij = IJMacro.ij;
         * } else { ij = new ImageJ(); } } if (ij!=null && !ij.isShowing()) {
         * ij.show(); System.out.println("show image"); }
         */
        /*
         * if (fileName != null) { // new ImagePlus(fileName).show();
         * imp.show(); System.out.println("show image fileName"); }
         */
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // private members ////

    // The URL as a string.
    //
    // The File
    // private File _file;

    // The URL of the file.
    // private URL _url;
}