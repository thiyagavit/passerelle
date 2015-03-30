package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.HashMap;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

@SuppressWarnings("serial")
public class CCDConfigurator extends Transformer {

    public Parameter deviceNameParam;
    private String deviceName = "CX/EX/CCD.1";
    // Princeton specific
    // private Parameter attributeExposureTimeParam; //pulseWidth on
    // continuousPulseGen
    // private String attributeExposureTime = "CX/EX/CPT.1/pulseWidthCounter0";
    // private Parameter attributePausingTimeParam; //delay on
    // continuousPulseGen
    // private String attributePausingTime = "CX/EX/CPT.1/delayCounter0";

    // private Parameter acquisitionTypeParam;
    // private String acquisitionType = "Intensity";
    public Parameter exposureTimeParam;
    public Parameter useRoiParam;
    public Parameter roiXminParam;
    public Parameter roiXmaxParam;
    public Parameter roiYminParam;
    public Parameter roiYmaxParam;
    public Parameter numFramesParam;
    public Parameter pausingTimeParam;
    public Parameter xBinningParam;
    public Parameter yBinningParam;
    public Parameter triggerModeParam;
    private double exposureTime = 1;
    private boolean useRoi = true;
    private int roiXmin = 0;
    private int roiXmax = 1300;
    private int roiYmin = 0;
    private int roiYmax = 399;
    private int numFrames = 100;
    private String triggerMode = "None";
    private double pausingTime = 1;
    private int xBinning = 1;
    private int yBinning = 1;

    protected CCDManager ccd;
    HashMap<String, Integer> trigModeMap;

    public CCDConfigurator(final CompositeEntity arg0, final String arg1, final HashMap<String, Integer> trigModeMap)
            throws NameDuplicationException, IllegalActionException {
        super(arg0, arg1);

        // ccd = new CCDManager(this);
        // ccd.setTrigModeMap(trigModeMap);
        this.trigModeMap = trigModeMap;
        deviceNameParam = new StringParameter(this, "Device Name");
        deviceNameParam.setExpression(deviceName);

        exposureTimeParam = new StringParameter(this, "Exposure Time (ms)");
        exposureTimeParam.setExpression("1");

        useRoiParam = new Parameter(this, "Use ROI", new BooleanToken(true));
        useRoiParam.setTypeEquals(BaseType.BOOLEAN);

        roiXminParam = new StringParameter(this, "ROI x min");
        roiXminParam.setExpression("0");

        roiXmaxParam = new StringParameter(this, "ROI x max");
        roiXmaxParam.setExpression("1300");

        roiYminParam = new StringParameter(this, "ROI y min");
        roiYminParam.setExpression("0");

        roiYmaxParam = new StringParameter(this, "ROI y max");
        roiYmaxParam.setExpression("399");

        xBinningParam = new StringParameter(this, "x binning");
        xBinningParam.setExpression("1");

        yBinningParam = new StringParameter(this, "y binning");
        yBinningParam.setExpression("1");

        numFramesParam = new StringParameter(this, "Number of frames");
        numFramesParam.setExpression("100");

        pausingTimeParam = new StringParameter(this, "Pausing Time (ms)");
        pausingTimeParam.setExpression("1");

        triggerModeParam = new StringParameter(this, "Trigger Mode");
        boolean firstTime = true;
        for (final String string : trigModeMap.keySet()) {
            final String mode = string;
            if (firstTime) {
                triggerModeParam.setExpression(mode);
                firstTime = false;
            }
            triggerModeParam.addChoice(mode);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        ccd = CCDManagerFactory.getInstance().createCCDManager(this, deviceName);
        ccd.getConfig().setTrigModeMap(trigModeMap);

        if (!isMockMode()) {
            try {
                // just test connection to device
                new TangoAttribute(deviceName + "/roi1xmin");
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        super.doInitialize();
    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        ccd.setActor(this);
        ccd.getConfig().setExposureTime(exposureTime);
        ccd.getConfig().setNumFrames(numFrames);
        ccd.getConfig().setPausingTime(pausingTime);
        ccd.getConfig().setUseRoi(useRoi);
        ccd.getConfig().setRoiXmax(roiXmax);
        ccd.getConfig().setRoiXmin(roiXmin);
        ccd.getConfig().setRoiYmax(roiYmax);
        ccd.getConfig().setRoiYmin(roiYmin);
        ccd.getConfig().setTriggerMode(triggerMode);
        ccd.getConfig().setUseRoi(useRoi);
        ccd.getConfig().setXBinning(xBinning);
        ccd.getConfig().setYBinning(yBinning);
        try {
            if (isMockMode()) {
                ExecutionTracerService.trace(this, "MOCK - configuring with parameters: \n" + ccd.getConfig());
            } else {
                ExecutionTracerService.trace(this, "configuring with parameters: \n" + ccd.getConfig());
                ccd.configure();
            }
            sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
    }

    /*
     * protected void doStandardAcquisition() throws DevFailed,
     * IllegalActionException { ExecutionTracerService.trace(this,
     * "starting CCD acquisition");
     * 
     * // set acqMode to sequence mode AttributeCompleteHelper attr; attr = new
     * AttributeCompleteHelper(deviceName+"/acqMode");
     * attr.write(acqModeMap.get(acqMode));
     * 
     * CommandHelper cmd; // save contextual data DataRecorder.saveDevice(this,
     * deviceName);
     * 
     * 
     * // TODO: start trig generation for hardware trigger // execute sequence
     * cmd = new CommandHelper(deviceName, "Start"); cmd.execute();
     * 
     * DeviceProxy dev = cmd.getDeviceProxy(); long currentFrameIdx = 0; long
     * savedFrameIdx = 0; AttributeCompleteHelper framesAttr = new
     * AttributeCompleteHelper(deviceName+"/frames"); AttributeCompleteHelper
     * selectFrameAttr = new AttributeCompleteHelper(deviceName+"/selectFrame");
     * //wait for the end of acquisition do { try {
     * Thread.sleep(Math.round(exposureTime+pausingTime)-
     * Math.round(exposureTime+pausingTime)/10); } catch (InterruptedException
     * e1) { } // get progression of acquisition framesAttr.read();
     * currentFrameIdx = framesAttr.extractAndConvertToLong();
     * 
     * //record all images of the sequence //done in parallel of the acquisition
     * if(recordAllSequence && DataRecorder.isSaveActive(this)) { // save only
     * if device has not already been saved if(currentFrameIdx > 0 &&
     * currentFrameIdx > savedFrameIdx){ // select next image on device (that
     * will be visible in attribute selected image)
     * selectFrameAttr.convertFromDoubleAndInsert(savedFrameIdx);
     * selectFrameAttr.write();
     * System.out.println("current acquired image : "+currentFrameIdx );
     * System.out.println("saving image nb: "+savedFrameIdx+1 ); //save
     * DataRecorder.saveExperimentalData(this, deviceName); savedFrameIdx++; }
     * if( dev.state() == DevState.OPEN && currentFrameIdx!= numFrames){
     * ExecutionTracerService.trace(this, "CCD acquisition stopped by user");
     * break; } }else{ // end actor if acquisition is finished if( dev.state()
     * == DevState.OPEN){ ExecutionTracerService.trace(this,
     * "CCD acquisition finished"); break; } if(dev.state() == DevState.FAULT) {
     * ExecutionTracerService.trace(this,
     * "CCD acquisition finished with ERROR - device is in FAULT"); break; }
     * }//TODO: que ce passe t il si l'acq est stopp�e par l'utisateur?
     * 
     * // System.out.println("looping "+savedFrameIdx+" "+numFrames); } while
     * (savedFrameIdx < numFrames);
     * 
     * // just record data once if(!recordAllSequence){
     * DataRecorder.saveExperimentalData(this, deviceName); }
     * if(DataRecorder.isSaveActive(this)){ if( dev.state() == DevState.OPEN){
     * ExecutionTracerService.trace(this, "CCD acquisition finished and saved");
     * } if(dev.state() == DevState.FAULT) { ExecutionTracerService.trace(this,
     * "CCD acquisition finished with ERROR - device is in FAULT"); } } }
     */

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == deviceNameParam) {
            deviceName = ((StringToken) deviceNameParam.getToken()).stringValue();
            // ccd.setDeviceName(deviceName);
        } else if (arg0 == pausingTimeParam) {
            pausingTime = Double.valueOf(((StringToken) pausingTimeParam.getToken()).stringValue());
            // config.setPausingTime(pausingTime);
        } else if (arg0 == exposureTimeParam) {
            exposureTime = Double.valueOf(((StringToken) exposureTimeParam.getToken()).stringValue());
            // config.setExposureTime(exposureTime);
        } else if (arg0 == useRoiParam) {
            useRoi = Boolean.valueOf(useRoiParam.getExpression());
        } else if (arg0 == roiXminParam) {
            roiXmin = Integer.valueOf(((StringToken) roiXminParam.getToken()).stringValue());
            // config.setRoiXmin(roiXmin);
        } else if (arg0 == roiXmaxParam) {
            roiXmax = Integer.valueOf(((StringToken) roiXmaxParam.getToken()).stringValue());
            // config.setRoiXmax(roiXmax);
        } else if (arg0 == roiYminParam) {
            roiYmin = Integer.valueOf(((StringToken) roiYminParam.getToken()).stringValue());
            // config.setRoiYmin(roiYmin);
        } else if (arg0 == roiYmaxParam) {
            roiYmax = Integer.valueOf(((StringToken) roiYmaxParam.getToken()).stringValue());
            // config.setRoiYmax(roiYmax);
        } else if (arg0 == xBinningParam) {
            xBinning = Integer.valueOf(((StringToken) xBinningParam.getToken()).stringValue());
            // config.setXBinning(xBinning);
        } else if (arg0 == yBinningParam) {
            yBinning = Integer.valueOf(((StringToken) yBinningParam.getToken()).stringValue());
            // config.setYBinning(yBinning);
        } else if (arg0 == numFramesParam) {
            numFrames = Integer.valueOf(((StringToken) numFramesParam.getToken()).stringValue());
            // config.setNumFrames(numFrames);
        } else if (arg0 == triggerModeParam) {
            triggerMode = ((StringToken) triggerModeParam.getToken()).stringValue();
            // config.setTriggerMode(triggerMode);
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
