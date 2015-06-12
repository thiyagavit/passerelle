package fr.soleil.passerelle.tango.util;

import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbDatum;
import fr.soleil.comete.tango.data.service.helper.TangoDeviceHelper;
import fr.soleil.tango.clientapi.TangoCommand;

public final class TangoAccess {

    /**
     * Return true is the current state of the device deviceName is equal to a
     * particular State.
     * 
     * @param deviceName
     * @param stateRequired
     * @return
     * @throws DevFailed
     */
    public static boolean isCurrentStateEqualStateRequired(final String deviceName, final DevState stateRequired)
            throws DevFailed {
        final TangoCommand cmd = new TangoCommand(deviceName, "State");
        final DevState currentState = (DevState) cmd.executeExtract(null);

        return currentState.equals(stateRequired);
    }

    /**
     * Return the current State by the help of a TangoCommand if this one exist
     * 
     * @param deviceName
     * @param cmd
     * @return
     * @throws DevFailed
     */
    public static DevState getCurrentState(final TangoCommand cmd) throws DevFailed {
        return getCurrentStateInternal("",cmd);
    }

    public static DevState getCurrentState(final String deviceName) throws DevFailed {
        return getCurrentStateInternal(deviceName,null);
    }
    
    private static DevState getCurrentStateInternal(final String deviceName, TangoCommand cmd) throws DevFailed {
        DevState state = DevState.UNKNOWN;
        
        if (!deviceName.isEmpty() && cmd == null) {
            cmd = new TangoCommand(deviceName, "State");
        }        
        if(cmd != null){
            state = (DevState) cmd.executeExtract(null);
        }
        return state;
          
    }
    
    
    public static String getDeviceProperty(String deviceName, String propertyName) {
        String value = null;
        Database database = TangoDeviceHelper.getDatabase();
        if (database != null && !isNullOrEmpty(deviceName) && !isNullOrEmpty(propertyName)) {
            try {
                DbDatum dbDatum = database.get_device_property(deviceName, propertyName);
                if (dbDatum != null && !dbDatum.is_empty()) {
                    value = dbDatum.extractString();
                }
            } catch (DevFailed e) {
                DevFailedUtils.printDevFailed(e);
            }
        }
        return value;
    }
    
    private static boolean isNullOrEmpty(String stringValue) {
        return (stringValue == null || stringValue.isEmpty());
    }
    /**
     * Execute a Tango Command if the device is in a particular state. Return
     * true if the command has been executed
     * 
     * @param deviceName
     * @param cmdToExecute
     * @param displayMessage
     * @throws DevFailed
     */
    public static boolean executeCmdAccordingState(final String deviceName, final DevState stateRequired,
            final String cmdToExecute) throws DevFailed {
        boolean cmdExecuted = false;
        if (isCurrentStateEqualStateRequired(deviceName,stateRequired)) {
            new TangoCommand(deviceName, cmdToExecute).execute();
            cmdExecuted = true;
        }
        return cmdExecuted;
    }
    
    public static String[] getDeviceExportedForClass(String className) throws DevFailed {
    	String[] deviceList = null;
        if (className != null) {
        	Database database = TangoDeviceHelper.getDatabase();
        	deviceList = database.get_device_exported_for_class(className);
        }
        return deviceList;
    }
    
    public static String getFirstDeviceExportedForClass(String className) throws DevFailed {
    	String[] deviceList =getDeviceExportedForClass( className);
    	String deviceName = null;
        if (deviceList != null && deviceList.length > 0) {
        	deviceName = deviceList[0];
        }
        return deviceName;
    }
}
