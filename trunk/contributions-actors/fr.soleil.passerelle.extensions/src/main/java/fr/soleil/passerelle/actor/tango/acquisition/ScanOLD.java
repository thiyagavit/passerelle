/*	Synchrotron Soleil
 *
 *   File          :  ScanFromFile.java
 *
 *   Project       :  passerelle-soleil
 *
 *   Description   :
 *
 *   Author        :  ABEILLE
 *
 *   Original      :  17 mai 2005
 *
 *   Revision:  					Author:
 *   Date: 							State:
 *
 *   Log: ScanFromFile.java,v
 *
 */
/*
 * Created on 17 mai 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.acquisition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.ptolemy.FileParameter;

import com.isencia.passerelle.doc.generator.ParameterName;

import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.TransformerV3;
import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanApi;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Do scans using Salsa config files
 *
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class ScanOLD extends TransformerV3 implements IActorFinalizer {

    private static final String SCAN_CONFIG = "Scan Config";

    private final static Logger logger = LoggerFactory.getLogger(ScanOLD.class);

    // public Port contextOutput;
    /**
     * The name of the Salsa config file. Can be the entire path to the config
     * or the relative path from the scan repository. It is not mandatory to add
     * the extension (.salsa)
     */
    @ParameterName(name = SCAN_CONFIG)
    public Parameter scanConfigParam;
    private String fileName = "";

    // protected ScanServer scanServer;
    // protected SalsaModel model;

    // protected ScanConfiguration config = null;
    // private File file ;

    // protected Device dev;
    protected ScanApi scanApi;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.IllegalActionException
     * @throws ptolemy.kernel.util.NameDuplicationException
     */
    public ScanOLD(final CompositeEntity container, final String name) throws IllegalActionException,
	    NameDuplicationException {
	super(container, name);
	scanConfigParam = new FileParameter(this, SCAN_CONFIG, "salsa", "salsa");
	scanConfigParam.setExpression(fileName);

	input.setName("Trigger");
	output.setName("TriggerOut");

	scanApi = new ScanApi(this);
	try {
	    ((FileParameter) scanConfigParam)
		    .setBaseDirectory(new File(scanApi.getScanRepository()).toURI());
	} catch (final InitializationException e) {
	    throw new IllegalActionException(scanConfigParam, e, "");
	}
	final URL url = this.getClass().getResource("/fr/soleil/salsa/salsa.png");

	_attachText("_iconDescription", "<svg>\n"
		+ " <image x=\"0\" y=\"0\" width =\"75\" height=\"51\" xlink:href=\"" + url
		+ "\"/>\n" + "</svg>\n");

    }

    /**
     * @throws InitializationException
     *             9 sept. 2005
     */
    @Override
    public void doInitialize() throws InitializationException {
	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doInitialize() - entry");
	}

	if (!isMockMode()) {
	    final Director dir = getDirector();
	    if (dir instanceof BasicDirector) {
		((BasicDirector) dir).registerFinalizer(this);
	    }
	    if (logger.isTraceEnabled()) {
		logger.trace(getName() + "load salsa config");
	    }
	    File file = null;
	    boolean isURL = false;
	    URL url = null;
	    // 1. The string fileName is an URL (file:/usr...)
	    try {
		url = new URL(fileName);
		file = new File(url.toURI());
		isURL = true;
	    } catch (final MalformedURLException e) {
		// ignore
	    } catch (final URISyntaxException e) {
	        ExceptionUtil.throwInitializationException("salsa file not loaded", fileName, e);
	    }
	    // 2. The string fileName is not an URL (/usr/..)
	    if (!isURL) {
		file = new File(fileName);

		if (!file.exists()) {
		    // allow to not specify the all scan repository path
		    final String repo = scanApi.getScanRepository();
		    final File repoDir = new File(repo);
		    try {
			if (!file.getCanonicalPath().contains(repoDir.getCanonicalPath())) {
			    fileName = repo + File.separator + fileName;
			    file = new File(fileName);
			}
		    } catch (final IOException e1) {
		        ExceptionUtil.throwInitializationException("salsa file not loaded", fileName, e1);
		    }

		    // allow to not specify file extention
		    if (!fileName.endsWith(scanApi.getSalsaExtension())) {
			fileName = fileName + "." + scanApi.getSalsaExtension();
			file = new File(fileName);
		    }
		}

	    }

	    if (!file.exists() || !file.isFile()) {
	        ExceptionUtil.throwInitializationException("file not loaded (does not exist)", fileName);
	    }

	    logger.debug("Loading " + file.getAbsolutePath());
	    scanApi.loadScanFile(file);
	}
	super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
	    final ProcessResponse response) throws ProcessingException {

	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - entry");
	}
	if (isMockMode()) {
	    ExecutionTracerService.trace(this, "MOCK - Scan started with config: " + fileName);
	    ExecutionTracerService.trace(this, "MOCK - Scan finished");
	} else {
	    ExecutionTracerService.trace(this, "Scan started with config: " + fileName);
	    scanApi.scan();
	    ExecutionTracerService.trace(this, "Scan finished");
	}
	// sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
	response.addOutputMessage(0, output, PasserelleUtil.createTriggerMessage());

	if (logger.isTraceEnabled()) {
	    logger.trace(getName() + " doFire() - exit");
	}
    }

    @Override
    protected void doStop() {
	scanApi.cancelWaitEndScan();
	super.doStop();
    }

    public void doFinalAction() {
	try {
	    if (!isMockMode()) {
		scanApi.stop();
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	    // ignore error since it is impossible to throw it
	}

    }

    /**
     * @param attribute
     * @throws IllegalActionException
     *             9 sept. 2005
     */
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
	if (attribute == scanConfigParam && scanConfigParam instanceof FileParameter) {
	    fileName = PasserelleUtil.getParameterValue(scanConfigParam);
	    // System.out.println("file name "
	    // + PasserelleUtil.getParameterValue(scanConfigParam));
	} else {
	    super.attributeChanged(attribute);
	}
    }

    /**
     * @return 9 sept. 2005
     */
    @Override
    protected String getExtendedInfo() {
	return this.getName();
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
	final ScanOLD copy = (ScanOLD) super.clone(workspace);
	copy.scanApi = new ScanApi(copy);
	return copy;
    }

}
