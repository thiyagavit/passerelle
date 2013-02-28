package fr.soleil.passerelle.util;

public final class MiscellaneousUtil {
	public static double[] convertStringsTodoubles(String[] strings){
		double[] result;
		result = new double[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i]= Double.valueOf(strings[i]);
		}
		return result;
	}
	
	public static int[] convertStringsToiInts(String[] strings){
		int[] result;
		result = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i]= Integer.valueOf(strings[i]);
		}
		return result;
	}
	
	public static boolean[] convertStringsToiBooleans(String[] strings){
		boolean[] result;
		result = new boolean[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i]= Boolean.valueOf(strings[i]);
		}
		return result;
	}
}
