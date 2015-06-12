package fr.soleil.passerelle.actor.tango.archiving;

import java.text.SimpleDateFormat;

import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoAccess;
import fr.soleil.tango.clientapi.TangoCommand;

public class HdbExtractorProxy {
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final String HDBEXTRACTOR = "HdbExtractor";
    public static final String NEWEST_CMD = "GetNewestValue";
    public static final String NEAREST_CMD = "GetNearestValue";
    private static final String VALUE_SEP = ";";
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
    public HdbExtractorProxy() throws DevFailed {
    	
        String hdbExtractorName =  TangoAccess.getFirstDeviceExportedForClass(HDBEXTRACTOR);
        if (hdbExtractorName != null ) {
            newestValueCommand = new TangoCommand(hdbExtractorName, NEWEST_CMD);
            nearestValueCommand = new TangoCommand(hdbExtractorName, NEAREST_CMD);
        } else {
            DevFailedUtils.throwDevFailed("No " + HDBEXTRACTOR + " device found !");
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

        String result = null;
        if (newestValueCommand != null) {
            // res should contains the timestamp and the read part of the attribute separated by ;
            // eg 1363947098786; -10.176605465035054
            String res = newestValueCommand.execute(String.class, completeAttributeName);
            if(res != null){
                String[] attrWithTimeStamp = res.split(VALUE_SEP);
                if (attrWithTimeStamp != null && attrWithTimeStamp.length == 2) {
                    result = attrWithTimeStamp[1].trim();
                }
            }
        }
        if(result == null || result.isEmpty()){
            DevFailedUtils.throwDevFailed("No result found executing " + hdbExtractorName + "/" + NEWEST_CMD + " for " + completeAttributeName);
        }

        return result;
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
    public String getNearestScalarAttrValue(String completeAttributeName, String date) throws DevFailed {

        String result = null;
        if (nearestValueCommand != null) {
            // res should contains the timestamp and the read part of the attribute separated by ;
            // eg 1363947098786; -10.176605465035054
            String res = nearestValueCommand.execute(String.class, completeAttributeName, date);
            if(res != null){
                String[] attrWithTimeStamp = res.split(VALUE_SEP);
                if (attrWithTimeStamp != null && attrWithTimeStamp.length == 2) {
                    result = attrWithTimeStamp[1].trim();
                }
            }
        }
        
        if(result == null || result.isEmpty()){
            DevFailedUtils.throwDevFailed("No result found executing " + hdbExtractorName + "/" + NEAREST_CMD + " for " + completeAttributeName + " at " + date );
        }
        
        return result;

    }

    public String getHdbExtractorName() {
        return hdbExtractorName;
    }
}
