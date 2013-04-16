package fr.soleil.passerelle.actor.tango.acquisition.ccd;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class CCDConfiguration {
	private String acqMode = "OneShot";
	private double exposureTime = 1;
	private boolean useRoi = true;
	private int roiXmin =0;
	private int roiXmax = 1300;
	private int roiYmin = 0;
	private int roiYmax = 399;
	private int numFrames = 100;
	private String triggerMode = "None";
	private double pausingTime = 1;
	private int xBinning = 1;
	private int yBinning = 1;
	private boolean recordAllSequence;
	private HashMap<String, Integer> acqModeMap;
	private HashMap<Integer, String> acqModeMapInverted;
	private HashMap<String, Integer> trigModeMap;
	private HashMap<Integer, String> trigModeMapInverted;
	
	@Override
	public String toString()
	{	
		StringBuffer buffer = new StringBuffer("");
		buffer.append("acqMode " + acqMode+ "\r\n");
		buffer.append("exposureTime " + exposureTime+ "\r\n");
		buffer.append("useRoi " + useRoi+ "\r\n");
		buffer.append("roiXmin " + roiXmin+ "\r\n");
		buffer.append("roiXmax " + roiXmax+ "\r\n");
		buffer.append("roiYmin " + roiYmin+ "\r\n");
		buffer.append("roiYmax " + roiYmax+ "\r\n");
		buffer.append("numFrames " + numFrames+ "\r\n");
		buffer.append("triggerMode " + triggerMode+ "\r\n");
		buffer.append("pausingTime " + pausingTime+ "\r\n");
		buffer.append("xBinning " + xBinning+ "\r\n");
		buffer.append("yBinning " + yBinning+ "\r\n");
		buffer.append("recordAllSequence " + recordAllSequence+ "\r\n");
		return buffer.toString(); 
	}
	
	public Integer getAcqModeValue(){
		if(acqModeMap == null)
			return null;
		else
			return acqModeMap.get(acqMode);
	}
	
	public String getAcqModeForValue(int value){
		if(acqModeMapInverted == null)
			return null;
		else
			return acqModeMapInverted.get(value);
	}
	
	public String getAcqMode() {
		return acqMode;
	}
	public void setAcqMode(String acqMode) {
		this.acqMode = acqMode;
	}
	public double getExposureTime() {
		return exposureTime;
	}
	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}
	public int getNumFrames() {
		return numFrames;
	}
	public void setNumFrames(int numFrames) {
		this.numFrames = numFrames;
	}
	public double getPausingTime() {
		return pausingTime;
	}
	public void setPausingTime(double pausingTime) {
		this.pausingTime = pausingTime;
	}
	public boolean isRecordAllSequence() {
		return recordAllSequence;
	}
	public void setRecordAllSequence(boolean recordAllSequence) {
		this.recordAllSequence = recordAllSequence;
	}
	public int getRoiXmax() {
		return roiXmax;
	}
	public void setRoiXmax(int roiXmax) {
		this.roiXmax = roiXmax;
	}
	public int getRoiXmin() {
		return roiXmin;
	}
	public void setRoiXmin(int roiXmin) {
		this.roiXmin = roiXmin;
	}
	public int getRoiYmax() {
		return roiYmax;
	}
	public void setRoiYmax(int roiYmax) {
		this.roiYmax = roiYmax;
	}
	public int getRoiYmin() {
		return roiYmin;
	}
	public void setRoiYmin(int roiYmin) {
		this.roiYmin = roiYmin;
	}
	
	public Integer getTriggerModeValue(){
		if(trigModeMap == null)
			return null;
		else
			return trigModeMap.get(triggerMode);
	}
	
	public String getTriggerModeForValue(int value){
		if(trigModeMapInverted == null)
			return null;
		else
			return trigModeMapInverted.get(value);
	}
	public String getTriggerMode() {
		return triggerMode;
	}
	public void setTriggerMode(String triggerMode) {
		this.triggerMode = triggerMode;
	}
	public boolean isUseRoi() {
		return useRoi;
	}
	public void setUseRoi(boolean useRoi) {
		this.useRoi = useRoi;
	}
	public int getXBinning() {
		return xBinning;
	}
	public void setXBinning(int binning) {
		xBinning = binning;
	}
	public int getYBinning() {
		return yBinning;
	}
	public void setYBinning(int binning) {
		yBinning = binning;
	}

	public HashMap<String, Integer> getAcqModeMap() {
		return acqModeMap;
	}

	public void setAcqModeMap(HashMap<String, Integer> acqModeMap) {
		this.acqModeMap = acqModeMap;
		this.acqModeMapInverted = new HashMap<Integer, String>();
		Set<Entry<String, Integer>> entrySet = this.acqModeMap.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			acqModeMapInverted.put(value, key);
		}
	}

	public HashMap<String, Integer> getTrigModeMap() {
		return trigModeMap;
	}

	public void setTrigModeMap(HashMap<String, Integer> trigModeMap) {
		this.trigModeMap = trigModeMap;
		this.trigModeMapInverted = new HashMap<Integer, String>();
		Set<Entry<String, Integer>> entrySet = this.trigModeMap.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			trigModeMapInverted.put(value, key);
		}
	}
}
