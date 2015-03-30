/*
 * Synchrotron Soleil
 * 
 * File : ScanFromFile.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 17 mai 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: ScanFromFile.java,v
 */
/*
 * Created on 17 mai 2005
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.acquisition;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.TangoApi.DeviceProxyFactory;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.actor.TransformerV5;
import fr.soleil.passerelle.actor.tango.acquisition.scan.ScanUtil;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.ScanTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.api.SalsaAPI;
import fr.soleil.salsa.entity.IConfig;
import fr.soleil.salsa.exception.SalsaDeviceException;
import fr.soleil.salsa.exception.ScanNotFoundException;

/**
 * Do scans using Salsa config V3
 * 
 * @author GRAMER
 */
@SuppressWarnings("serial")
public class Scan extends TransformerV5 implements IActorFinalizer {

    private static final String SCAN_CONFIG = "Scan Config";
    private final static Logger logger = LoggerFactory.getLogger(Scan.class);

    /**
     * The name of the Salsa config V3. must be the entire path to the config
     * exemple : root/config1 or root/folder1/config2
     */
    @ParameterName(name = SCAN_CONFIG)
    public Parameter scanConfigParam;
    private String confName = "root/";

    protected IConfig<?> conf;
    protected ScanTask scanTask;

    /**
     * Create a Scan V3 actor
     * 
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public Scan(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        scanConfigParam = new StringParameter(this, SCAN_CONFIG);
        scanConfigParam.setExpression(confName);

        input.setName("Trigger");
        output.setName("TriggerOut");

        final URL url = this.getClass().getResource("/fr/soleil/salsa/salsa.png");

        _attachText("_iconDescription", "<svg>\n" + " <image x=\"0\" y=\"0\" width =\"75\" height=\"51\" xlink:href=\""
                + url + "\"/>\n" + "</svg>\n");

    }

    /**
     * Initialize actor
     */
    @Override
    public void validateInitialization() throws ValidationException {
        super.validateInitialization();

        if (!isMockMode()) {
            final Director dir = getDirector();
            if (dir instanceof BasicDirector) {
                ((BasicDirector) dir).registerFinalizer(this);
            }
            
            try {
                logger.debug("load salsa config {}", confName);
                conf = SalsaAPI.getConfigByPath(confName);

            } catch (final ScanNotFoundException e) {
                ExecutionTracerService.trace(this, "Error: Unknown scan configuration " + confName);
                throw new ValidationException(ErrorCode.ERROR, "Unknown scan configuration " + confName, this, e) ;
            }

            try {
                configureRecordingSession();
            } catch (SalsaDeviceException e) {
                ExecutionTracerService.trace(this, "Error: Recording session configuration error");
                throw new ValidationException(ErrorCode.ERROR, "Error: Recording session configuration error" + confName, this, e) ;
            }

        }
    }

    /**
     * Actor action
     */
    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {       
        if (isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - Scan started with config: " + confName);
            ExecutionTracerService.trace(this, "MOCK - Scan finished");
        } else {
            ExecutionTracerService.trace(this, "Scan started with config: " + confName);

            scanTask = new ScanTask(conf, 1000);
            scanTask.run();
            if (scanTask.hasFailed()) {
                ExceptionUtil.throwProcessingExceptionWithLog(this, ErrorCode.FATAL, scanTask.getError().getMessage(),this, scanTask.getError());
            } else {
                ExecutionTracerService.trace(this, "Scan finished");
            }

        }

        sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
    }

    private void configureRecordingSession() throws SalsaDeviceException {
        // FIXME : http://jira.synchrotron-soleil.fr/jira/browse/TANGOCORE-5
        DeviceProxyFactory.remove(SalsaAPI.getDevicePreferences().getScanServer().toLowerCase());

        if (DataRecorder.getInstance().isSaveActive(this)) {
            ScanUtil.getCurrentSalsaApi().setDataRecorderPartialMode(true);
            // System.out.println("setDataRecorderPartialMode(true)");
        } else {

            ScanUtil.getCurrentSalsaApi().setDataRecorderPartialMode(false);
            // System.out.println("setDataRecorderPartialMode(false)");
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        stopScan();
    }

    @Override
    public void doFinalAction() {
        // System.out.println("SCAN : dofinal action is requiered");
        // stopScan();
    }

    @Override
    protected void doPauseFire() {
        super.doPauseFire();
        if (!isMockMode()) {
            try {
                if (scanTask != null) {
                    if (scanTask.isRunning()) {
                        ExecutionTracerService.trace(this, "Scan is paused");
                        ScanUtil.getCurrentSalsaApi().pauseScan();
                    }
                }
            } catch (final Exception e) {
                logger.warn(e.getMessage());
                // ignore error since it is impossible to throw it
            }
        }
    }

    @Override
    protected void doResumeFire() {
        super.doResumeFire();
        if (!isMockMode()) {
            try {
                if (scanTask != null) {
                    if (scanTask.isRunning()) {
                        ScanUtil.getCurrentSalsaApi().resumeScan();
                        ExecutionTracerService.trace(this, "Scan is resumed");
                    }
                }
            } catch (final Exception e) {
                logger.warn(e.getMessage());
                // ignore error since it is impossible to throw it
            }
        }
    }

    private void stopScan() {
        if (!isMockMode()) {
            try {
                if (scanTask != null) {
                    if (scanTask.isRunning()) {
                        ScanUtil.getCurrentSalsaApi().stopScan();
                        ExecutionTracerService.trace(this, "Scan aborted");
                    }
                }
            } catch (final Exception e) {
                logger.warn(e.getMessage());
                // ignore error since it is impossible to throw it
            }
        }
    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == scanConfigParam) {
            confName = PasserelleUtil.getParameterValue(scanConfigParam);
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final Scan copy = (Scan) super.clone(workspace);
        // TODO clone Scan V3
        // copy.scanApi = new ScanApi(copy);
        return copy;
    }

}
