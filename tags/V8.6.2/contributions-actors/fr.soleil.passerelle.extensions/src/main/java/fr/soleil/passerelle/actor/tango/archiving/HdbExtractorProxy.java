package fr.soleil.passerelle.actor.tango.archiving;

import java.text.SimpleDateFormat;

import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.util.SoleilUtilities;

public class HdbExtractorProxy {
    public static final String WRONG_FORMAT = "Error call ICA :  method GetNewestValue of hdbExtractor not returns the expected format which is timestamp; attribute_value";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy H:m:s");

    private TangoCommand newestValueCommand;
    private TangoCommand nearestValueCommand;
    private String hdbExtractorName = "";

    /**
     * Create a basic HDB extractor proxy with default tango command. You should use this
     * constructor instead of default (which is used for tests)
     * 
     * @param hdbExtractorName
     * 
     * @throws DevFailed
     */
    public HdbExtractorProxy(boolean defaultConfig) throws DevFailed {
        if (defaultConfig) {
            hdbExtractorName = SoleilUtilities.getDevicesFromClass("HdbExtractor")[0];
            newestValueCommand = new TangoCommand(hdbExtractorName, "GetNewestValue");
            nearestValueCommand = new TangoCommand(hdbExtractorName, "GetNearestValue");
        }
    }

    /**
     * define the tango command command used to get the newest value ( this method is use to mock
     * the hdb extractor proxy)
     * 
     * @param getNewestValue
     */
    public void setNewestValueCommad(TangoCommand getNewestValue) {
        newestValueCommand = getNewestValue;
    }

    public void setNearestValueCommand(TangoCommand nearestValueCommand) {
        this.nearestValueCommand = nearestValueCommand;
    }

    /**
     * return the last read part of a Scalar attribute
     * 
     * @param completeAttributeName the complete attribute name( eg domain/family/member/attr)
     * 
     * @return the last archived value as a String
     * 
     * @throws DevFailed throw an exception if the tango command failed or command result as not the
     *             expected format
     */
    public String getLastScalarAttrValue(String completeAttributeName) throws DevFailed {

        // res should contains the timestamp and the read part of the attribute separated by a
        // semicolon
        // eg 1363947098786; -10.176605465035054
        String res = newestValueCommand.execute(String.class, completeAttributeName);

        String[] attrWithTimeStamp = res.split(";");

        if (attrWithTimeStamp.length != 2) {
            DevFailedUtils.throwDevFailed(WRONG_FORMAT);
        }

        return attrWithTimeStamp[1].trim();
    }

    /**
     * return the nearest read part of a Scalar attribute from the specify date
     * 
     * @param completeAttributeName the complete attribute name( eg domain/family/member/attr)
     * 
     * @param date the specify date. Its must matches the format defined by field
     *            {@link fr.soleil.passerelle.actor.tango.archiving.HdbExtractorProxy.DATE_FORMAT}
     * 
     * @return the nearest archived value as a String
     * 
     * @throws DevFailed is thrown if
     *             <ul>
     *             <li>the date has the the format defined by
     *             {@link fr.soleil.passerelle.actor.tango.archiving.HdbExtractorProxy.DATE_FORMAT}</li>
     *             <li>the tango command failed</li>
     *             <li>command result as not the expected format</li>
     *             <ul>
     */
    public String getNearestScalarAttrValue(String completeAttributeName, String date)
            throws DevFailed {
        // res should contains the timestamp and the read part of the attribute separated by a
        // semicolon
        // eg 1363947098786; -10.176605465035054
        String res = nearestValueCommand.execute(String.class, completeAttributeName, date);

        String[] attrWithTimeStamp = res.split(";");

        if (attrWithTimeStamp.length != 2) {
            DevFailedUtils.throwDevFailed(WRONG_FORMAT);
        }

        return attrWithTimeStamp[1].trim();

    }

    public String getHdbExtractorName() {
        return hdbExtractorName;
    }
}
