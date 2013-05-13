package fr.soleil.passerelle.actor.tango.snapshot;

import java.util.HashMap;
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

public enum ExtractionTypeV2 {
    READ("Read part"), WRITE("Write part"), READ_WRITE("Read and Write part");

    private String description;

    private static final Map<String, ExtractionTypeV2> descriptionMap = new HashMap<String, ExtractionTypeV2>();
    static {
        for (final ExtractionTypeV2 extractionType : values()) {
            descriptionMap.put(extractionType.getDescription(), extractionType);
        }
    }

    private ExtractionTypeV2(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ExtractionTypeV2 fromDescription(final String desc) throws IllegalActionException {
        final ExtractionTypeV2 value = descriptionMap.get(desc);
        if (value != null) {
            return value;
        }
        throw new IllegalActionException("Unknown extraction description: \"" + desc + "\"");
    }
};
