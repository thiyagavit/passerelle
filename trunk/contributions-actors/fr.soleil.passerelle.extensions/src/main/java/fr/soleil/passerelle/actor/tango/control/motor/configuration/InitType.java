package fr.soleil.passerelle.actor.tango.control.motor.configuration;

/**
 * As you know there 2 commands to initialize a Motor :
 * <ul>
 * <li>DefinePosition(int new position): which set the position without move the motor</li>
 * <li>InitializeReferencePosition(): which run a complex series of movement to determine the
 * position.</li>
 * </ul>
 * <p/>
 * The InitializeReferencePosition command is not available on every motors.(see doc of InitMotor
 * project for more details) The "AxisInitType" property indicate if the command is available and
 * which "complex series of movement" (ie stategy) is used.
 * 
 * The InitializeReferencePosition command is <b>NOT</b> available is the property is <b>empty</b>
 * or equals to <b>"DP"</b> (means DefinePosition, still on some devices for historical reasons).
 * <b>Otherwise</b> the command is available
 */
public enum InitType {
    // LSBWD, LSFWD, FH, FI, DP;
    DP, OTHER;

    public static InitType getValueIfContains(String compare) {
        InitType initStrategy;
        compare = compare.trim();

        if (compare.isEmpty() || compare.equalsIgnoreCase(DP.toString())) {
            initStrategy = DP;
        } else {
            initStrategy = OTHER;
        }
        return initStrategy;
    }
}
