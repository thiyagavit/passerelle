package fr.soleil.passerelle.actor.tango.snapshot;

import org.apache.commons.lang.ArrayUtils;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoCommand;

public class SnapExtractorProxy {

    private final TangoCommand getSnapValues;
    private final TangoCommand getSnapID;
    private final TangoCommand getSnap;

    public SnapExtractorProxy(final String snapExtractorName) throws DevFailed {
        getSnapValues = new TangoCommand(snapExtractorName, "GetSnapValues");
        getSnapID = new TangoCommand(snapExtractorName, "GetSnapID");
        getSnap = new TangoCommand(snapExtractorName, "GetSnap");
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

}
