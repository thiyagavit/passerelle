package fr.soleil.bossanova.configuration;

public final class Configuration {


	private static final String SEQUENCES_DIRECTORY = "sequences.directory";
	private static final String SPECIFIC_ACTORS_DIRECTORY = "actors.specific.directory";
	private static final String COMMON_ACTORS_DIRECTORY = "actors.common.directory";

	
	private Configuration(){
		// DO NOT USE
	}
	
	public static String getSequencesDirectory() {
		return System.getProperty(SEQUENCES_DIRECTORY);
	}
	public static String getCommonActorsDirectory(){
		return System.getProperty(COMMON_ACTORS_DIRECTORY);
	}
	public static String getSpecificActorsDirectory(){
		return System.getProperty(SPECIFIC_ACTORS_DIRECTORY);
	}
	public static String getPasserelleConfDirectory(){
		return System.getenv("PASSERELLE_HOME")+ "/conf/";
	}
}
