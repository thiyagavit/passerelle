/**
 * 
 */
package fr.soleil.bossanova.model;

/**
 * @author viguier
 *
 */
public enum DirectorType {
	RECORDING("Recording Director"), SOLEILSTD("Soleil Standard");
	protected String value;
	
	DirectorType(String value){
		this.value = value;
	}
	public String getValue(){
		return this.value;
	}
	public String toString(){
		return this.value;
	}
}

