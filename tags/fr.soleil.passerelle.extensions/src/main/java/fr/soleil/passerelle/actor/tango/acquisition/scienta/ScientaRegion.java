package fr.soleil.passerelle.actor.tango.acquisition.scienta;

public class ScientaRegion {
	
	public final static String ACQ_MODE_FIXED = "Fixed";
	public final static String ACQ_MODE_SWEPT = "Swept";	
	
	private Boolean active = false;
	private String regionName = null;
	private String lensMode = null;
	private String passEnergy = null;
	private String kinBin = null;
	private double low = 0;
	private double high = 0;
	private double fix = 0;
	private double energyStep = 0;
	private double stepTime = 0;
	private String acqMode = null;
	private int numberSweeps = 0;
	
	public String getAcqMode() {
		return acqMode;
	}
	public void setAcqMode(String acqMode) {
		this.acqMode = acqMode;
	}
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public void setActive(String active) {
		this.active = new Boolean(active);
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public void setHigh(String high) {
		this.high = new Double(high);
	}
	public String getKinBin() {
		return kinBin;
	}
	public void setKinBin(String kinBin) {
		this.kinBin = kinBin;
	}
	public String getLensMode() {
		return lensMode;
	}
	public void setLensMode(String lensMode) {
		this.lensMode = lensMode;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public void setLow(String low) {
		this.low = new Double(low);
	}
	public int getNumberSweeps() {
		return numberSweeps;
	}
	public void setNumberSweeps(int numberSweeps) {
		this.numberSweeps = numberSweeps;
	}
	public void setNumberSweeps(String numberSweeps) {
		this.numberSweeps = new Integer(numberSweeps);
	}
	public String getPassEnergy() {
		return passEnergy;
	}
	public void setPassEnergy(String passEnergy) {
		this.passEnergy = passEnergy;
	}
	public double getEnergyStep() {
		return energyStep;
	}
	public void setEnergyStep(double energyStep) {
		this.energyStep = energyStep;
	}
	public void setEnergyStep(String energyStep) {
		this.energyStep = new Double(energyStep);
	}
	public double getStepTime() {
		return stepTime;
	}
	public void setStepTime(double stepTime) {
		this.stepTime = stepTime;
	}
	public void setStepTime(String stepTime) {
		this.stepTime = new Double(stepTime);
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public double getFix() {
		return fix;
	}
	public void setFix(double fix) {
		this.fix = fix;
	}
	public void setFix(String fix) {
		this.fix = new Double(fix);
	}
	@Override
	public Object clone()
	{
		ScientaRegion scientaRegion = new ScientaRegion();
		scientaRegion.setAcqMode(acqMode);
		scientaRegion.setActive(active);
		scientaRegion.setFix(fix);
		scientaRegion.setHigh(high);
		scientaRegion.setKinBin(kinBin);
		scientaRegion.setLensMode(lensMode);
		scientaRegion.setLow(low);
		scientaRegion.setNumberSweeps(numberSweeps);
		scientaRegion.setPassEnergy(passEnergy);
		scientaRegion.setRegionName(regionName);
		scientaRegion.setEnergyStep(energyStep);
		scientaRegion.setStepTime(stepTime);
		return scientaRegion;
	}
	
	public String toString()
	{	
		StringBuffer buffer = new StringBuffer("");
		buffer.append("active " + active+ "\r\n");
		buffer.append("regionName " + regionName+ "\r\n");
		buffer.append("lensMode " + lensMode+ "\r\n");
		buffer.append("passEnergy " + passEnergy+ "\r\n");
		buffer.append("kinBin " + kinBin+ "\r\n");
		buffer.append("low " + low+ "\r\n");
		buffer.append("high " + high+ "\r\n");
		buffer.append("fix " + fix+ "\r\n");
		buffer.append("stepMe " + energyStep+ "\r\n");
		buffer.append("stepTime " + stepTime+ "\r\n");
		buffer.append("acqMode " + acqMode+ "\r\n");
		buffer.append("numberSweeps " + numberSweeps+ "\r\n");
		return buffer.toString(); 
	}
	
}
