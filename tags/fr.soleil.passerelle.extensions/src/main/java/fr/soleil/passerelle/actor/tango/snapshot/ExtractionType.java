package fr.soleil.passerelle.actor.tango.snapshot;

public enum ExtractionType {
    READ("Read part", "STORED_READ_VALUE"), WRITE("Write part", "STORED_WRITE_VALUE"), READ_WRITE(
	    "Read and Write part", "");

    private String name;
    private String arginName;

    private ExtractionType(final String name, final String arginName) {
	this.name = name;
	this.arginName = arginName;
    }

    @Override
    public String toString() {
	return name;
    }

    public String getArginName() {
	return arginName;
    }
};
