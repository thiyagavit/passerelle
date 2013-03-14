package fr.soleil.math.test;


public class BeamProfiles {

	public double[] createSigmoid(int size, double lambda) {
		double[] result = new double[size];
		for(int i=0;i<size;i++) {
			double x = i-(size/2);
			result[i]=1/(1+Math.exp(-x*lambda));
		}
		return result;
	}
	
	public double[] createLorentzian(int size, double fwhm, double max) {
		double[] result = new double[size];
		for(int i=0;i<size;i++) {
			result[i]=(fwhm/(2*Math.PI))*(1/(Math.pow(0.5*fwhm,2)+Math.pow(i-max,2)));
		}
		return result;
	}
	
	public double[] createLine(int size) {
		double[] result = new double[size];
		for(int i=0;i<size;i++) {
			result[i]=i;
		}
		return result;
	}
}
