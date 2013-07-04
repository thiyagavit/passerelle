package fr.soleil.passerelle.actor.tango.snapshot;

import org.apache.commons.lang.ArrayUtils;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.util.SoleilUtilities;

public class SnapExtractorProxy {

    public static final int ID_MIN = 0;
    public static final String ERROR_SNAP_ID_INF_ID_MIN = "Error: snap id must not be negative";
    public static final String ERROR_SNAP_ID_NAN = "Error: snap id must be a number";

    private TangoCommand getSnapValues;
    private TangoCommand getSnapID;
    private String snapExtractorName = "";

    public SnapExtractorProxy() throws DevFailed {
        snapExtractorName = SoleilUtilities.getDevicesFromClass("SnapExtractor")[0];

        getSnapValues = new TangoCommand(snapExtractorName, "GetSnapValues");
        getSnapID = new TangoCommand(snapExtractorName, "GetSnapID");
    }

    public String getName() {
        return snapExtractorName;
    }

    /**
     * Get read values of a snap
     * 
     * @param snapID
     * @param attributeNames
     * 
     * @return
     * 
     * @throws DevFailed
     */
    public String[] getReadValues(final String snapID, final String... attributeNames)
            throws DevFailed {
        final Object[] array = ArrayUtils.addAll(new String[] { snapID, "true" }, attributeNames);
        return getSnapValues.execute(String[].class, array);
    }

    /**
     * Get write values of a snap
     * 
     * @param snapID
     * @param attributeNames
     * 
     * @return
     * 
     * @throws DevFailed
     */
    public String[] getWriteValues(final String snapID, final String... attributeNames)
            throws DevFailed {
        final Object[] array = ArrayUtils.addAll(new String[] { snapID, "false" }, attributeNames);
        return getSnapValues.execute(String[].class, array);
    }

    /**
     * Get last snap for a context
     * 
     * @param contextID
     * 
     * @return
     * 
     * @throws DevFailed
     */
    public String getLastSnapID(final String contextID) throws DevFailed {
        return getSnapID.execute(String[].class, contextID, "last")[0];
    }

    /**
     * Get snap ID with a filter
     * 
     * @param contextID
     * @param searchFilter ctx_id, "id_snap > | < | = | <= | >= nbr",
     *            "time < | > | >= | <=  yyyy-mm-dd hh:mm:ss | dd-mm-yyyy hh:mm:ss" ,
     *            "comment starts | ends | contains string", first | last
     * 
     * @return
     * 
     * @throws DevFailed
     */
    public String[] getSnapIDs(final String contextID, final String searchFilter) throws DevFailed {
        return getSnapID.execute(String[].class, contextID, searchFilter);
    }

    /**
     * get the read and the write values of an attribute
     * 
     * @param snapID the
     * @param attributeName the name of the attribute to extract
     * @return a String array. The first cell contains the read value and the second the write value
     * @throws DevFailed if the extraction failed (wrong id , attribute not found in snap, any tango
     *             error...)
     */
    public String[] getSnapValue(String snapID, String attributeName) throws DevFailed {
        final Object[] array = new Object[] { snapID, attributeName };
        return getSnapValues.execute(String[].class, array);
    }

}
