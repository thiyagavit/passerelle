package fr.soleil.bossanova.gui;

import java.util.prefs.Preferences;




/**
 * Manage user pref
 * 
 * @author Administrateur
 * 
 */
public class BossanovaUserPref {
	public final static String LAYOUT = "GUI.LAYOUT";
        public final static String BATCH_DIRECTORY = "BATCH.DIRECTORY";

	/**
	 * Save user preference
	 * 
	 * @param key
	 * @param value
	 */
	public static void putIntPref(String key, int value) {
		Preferences.userRoot().putInt(key, value);
	}

	/**
	 * Get user preference
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getIntPref(String key, int defaultValue) {
		return Preferences.userRoot().getInt(key, defaultValue);
	}
	/**
	 * Save user preference
	 * 
	 * @param key
	 * @param value
	 */
	public static void putByteArrayPref(String key, byte[] value) {
		Preferences.userRoot().putByteArray(key, value);
	}

	/**
	 * Get user preference
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static byte[] getByteArrayPref(String key, byte[] defaultValue) {
		return Preferences.userRoot().getByteArray(key, defaultValue);
	}

        	/**
	 * Save user preference
	 *
	 * @param key
	 * @param value
	 */
	public static void putPref(String key, String value) {
		Preferences.userRoot().put(key, value);
	}

	/**
	 * Get user preference
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getPref(String key, String defaultValue) {
		return Preferences.userRoot().get(key, defaultValue);
	}
}
