package fr.soleil.passerelle.actor.tango.snapshot;

import ptolemy.kernel.util.IllegalActionException;

/**
 * This enum contains the property need to extract a value from snap and set an equipment with
 * command
 */
public enum ExtractionType {
    READ("Read part", "STORED_READ_VALUE"), WRITE("Write part", "STORED_WRITE_VALUE"), READ_WRITE(
            "Read and Write part", "");

    /**
     * the description of the Enum. this field is used to define the parameter values in actors
     * (like @link java.fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapID)
     */
    private String name;

    /**
     * the argin of the command SetEquipmentsWithCommand of the snapManager.
     */
    // TODO explain what do SetEquipmentsWithCommand do ?
    private String arginName;

    private ExtractionType(final String name, String arginName) {
        this.name = name;
        this.arginName = arginName;
    }

    public String getName() {
        return name;
    }

    public String getArginName() {
        return arginName;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * give the Enum instance from the name. If the name is not valid (not assicated to an Enum)
     * then an IllegalActionException is thrown
     * 
     * @param name the name of the Enum {@link ExtractionTypeV2#name()}
     * @return the instance associated to the name
     * @throws IllegalActionException if the name is not associated to an Enum instance
     */
    public static ExtractionType fromDescription(final String name) throws IllegalActionException {
        for (ExtractionType value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        throw new IllegalActionException("Unknown extraction description: \"" + name + "\"");
    }
}
