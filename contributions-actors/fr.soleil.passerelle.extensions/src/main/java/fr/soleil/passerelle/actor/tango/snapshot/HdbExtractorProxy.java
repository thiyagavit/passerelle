package fr.soleil.passerelle.actor.tango.snapshot;

import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoCommand;

public class HdbExtractorProxy {
    public static final String WRONG_FORMAT = "Error call ICA :  method GetNewestValue of hdbExtractor not returns the expected format which is timestamp; attribute_value";
    private TangoCommand GetNewestValue;

    /**
     * Create a basic extract proxy with default tango command. You should use this constructor
     * instead of default (which is used for tests)
     * 
     * @param snapExtractorName
     * 
     * @throws DevFailed
     */
    public HdbExtractorProxy(final String snapExtractorName) throws DevFailed {
        GetNewestValue = new TangoCommand(snapExtractorName, "GetNewestValue");
    }

    /**
     * create an empty proxy, it's useful for test to be able to mock Tango command
     */
    public HdbExtractorProxy() {
    }

    public void setGetNewestValueCommad(TangoCommand getNewestValue) {
        GetNewestValue = getNewestValue;
    }

    /**
     * return the last read part of a Scalar attribute
     * 
     * @param deviceName the device name which contains the attribute
     * @param attributeName the name of attribute to be extracted
     * 
     * @return the last archived value as a String
     * 
     * @throws DevFailed throw an exception if the tango command failed or command result as not the
     *             expected format
     */
    public String getLastScalarAttrValue(String deviceName, String attributeName) throws DevFailed {

        // res should contains the timestamp and the read part of the attribute separated by a
        // semicolon
        // eg 1363947098786; -10.176605465035054
        String res = GetNewestValue.execute(String.class, deviceName + "/" + attributeName);

        String[] attrWithTimeStamp = res.split(";");

        if (attrWithTimeStamp.length != 2) {
            DevFailedUtils.throwDevFailed(WRONG_FORMAT);
        }

        return attrWithTimeStamp[1].trim();
    }
}
