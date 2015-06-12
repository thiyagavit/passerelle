package fr.soleil.passerelle.actor.tango.control.motor.configuration;


/**
 * There are 3 types of motor encoder:
 * <ul>
 * <li>NONE: There are no encoder , the motor can move of N steps, but does not know it position</li>
 * <li>INCREMENTAL: The motor can do calculation (- or + N steps), but does not know it START
 * position (which is indicated by the property AxisInitPosition). If the power is cut the motor
 * loose it position</li>
 * <li>ABSOLUTE: the motor know at every moment it position. The position is not loose if the power
 * is cut</li>
 * </ul>
 */
public enum EncoderType {
    NONE, INCREMENTAL, ABSOLUTE;

    public static EncoderType getValueFromOrdinal(final int ordinal){
        EncoderType type = NONE;
        switch (ordinal) {
            case 1:
                type = INCREMENTAL;
                break;
            case 2:
                type = ABSOLUTE;
                break;
            case 0:
                type = NONE;
                break;
            default:
                type = NONE;
        }
        return type;
    }

}
