package fr.soleil.passerelle.actor.flow;

import java.math.BigDecimal;
import java.math.MathContext;

public class Range {
	private double from = 0;

	private double to = 0;

	private double delta = 0;

	private int nbSteps = 0;

	private double[] trajectory = { 0 };
	
	private double currentValue = 0;
	private int currentIdx = 0;

	private String name = "";
	public void initialize(){
		boolean up = false;
		if(to-from >=0)
			up = true;
		trajectory = new double[nbSteps];
		BigDecimal deltaBd = new BigDecimal(delta);
		trajectory[0] = from;
		if(up){
			for (int i = 1; i < nbSteps; i++) {
				BigDecimal result = deltaBd.add(new BigDecimal(trajectory[i-1]), MathContext.DECIMAL32);
				trajectory[i] = result.doubleValue();
			}
		}else{
			for (int i = 1; i < nbSteps; i++) {
				BigDecimal traji = new BigDecimal(trajectory[i-1]);
				BigDecimal result = traji.subtract(deltaBd, MathContext.DECIMAL32);
				trajectory[i] = result.doubleValue();
			}
		}
		currentValue = 0;
		currentIdx = 0;
	}
	
	public double next(){
		if(currentIdx < trajectory.length){
			currentValue = trajectory[currentIdx];
			currentIdx++;
			return currentValue;
		}else{
			throw new IndexOutOfBoundsException("there is no more elements");
		}
	}
	
	public boolean hasNext(){
		if(currentIdx < trajectory.length)
			return true;
		else 
			return false;
	}
	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
		BigDecimal start = new BigDecimal(from);
		BigDecimal end = new BigDecimal(to);
		BigDecimal totalWidth = end.subtract(start, MathContext.DECIMAL32 ).abs();	
		BigDecimal div = totalWidth.divide(new BigDecimal(this.delta), MathContext.DECIMAL32);
		nbSteps = (int)(Math.floor(div.doubleValue())) + 1;
	}

	public double getFrom() {
		return from;
	}

	public void setFrom(double from) {
		this.from = from;
	}

	public int getNbSteps() {
		return nbSteps;
	}

	public void setNbSteps(int nbSteps) {
		this.nbSteps = nbSteps;
		BigDecimal start = new BigDecimal(from);
		BigDecimal end = new BigDecimal(to);
		BigDecimal totalWidth = end.subtract(start, MathContext.DECIMAL32 ).abs();	
		BigDecimal div = totalWidth.divide(new BigDecimal(this.nbSteps), MathContext.DECIMAL32);
		this.delta = div.doubleValue();
	}

	public double getTo() {
		return to;
	}

	public void setTo(double to) {
		this.to = to;
	}

	public double[] getTrajectory() {
		return trajectory;
	}

	public String toString()
	{	
		StringBuffer buffer = new StringBuffer("");
		buffer.append("from " + from+ "\r\n");
		buffer.append("to " + to+ "\r\n");
		buffer.append("delta " + delta+ "\r\n");
		buffer.append("nbSteps " + nbSteps+ "\r\n");
		buffer.append("trajectory " );
		for (int i = 0; i < trajectory.length; i++) {
			buffer.append(trajectory[i]+ ",");
		}
		buffer.append("\r\n");
	
		return buffer.toString(); 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
