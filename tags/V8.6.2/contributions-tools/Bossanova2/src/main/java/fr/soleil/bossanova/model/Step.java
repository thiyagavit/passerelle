/**
 *
 */
package fr.soleil.bossanova.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ptolemy.data.expr.Parameter;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.model.Flow;

/**
 * @author HARDION
 * 
 */

// Fixed FindBugs alert JCP Feb 2011
// Made class final as clone no longer calls super.clone() (see bug 17644)
final public class Step implements Cloneable {

	private StepType type = StepType.ACTOR;
	private String name = "";
	private int iterationCount = 1;
	private boolean enable = true;
	private String comment = "";
	private Map<String, String> parameters = new HashMap<String, String>();
	private Map<String, Object> contexts = new HashMap<String, Object>();
	private Integer id;
	
	// Bug 17570
	private int blockId = 0;
	private int blockIterationCount = 1;
	
	// This is not always available and should not be serialized !
	private transient Flow flow;
	private transient boolean onFault;

	public Step(StepType type, String name, int iterationCount, Integer id) {
		this.type = type;
		this.name = name;
		this.iterationCount = iterationCount;
		this.id = id;
	}

	public Step(StepType type, String name, int iterationCount) {
		this.type = type;
		this.name = name;
		this.iterationCount = iterationCount;
	}

	public Step(StepType type, String name) {
		this.type = type;
		this.name = name;
	}

	public Step()
	{
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StepType getType() {
		return type;
	}

	public void setType(StepType type) {
		this.type = type;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setBlockId(Integer id)
	{
		blockId = id;
	}

	public int getBlockId()
	{
		return blockId;
	}
	
	public void setBlockIterationCount(Integer count)
	{
		blockIterationCount = count;
	}

	public int getBlockIterationCount()
	{
		return blockIterationCount;
	}
	
	/**
	 * Apply parameters as found in the flow to the parameters map maintained by
	 * the Step instance. <br>
	 * If no flow is known yet, the parameters map is cleared.
	 */
	public void applyFlowParametersToStep() {
		parameters.clear();
		if (flow != null) {
			Collection<Parameter> flowParameters = flow.getAllParameters();
			for (Parameter parameter : flowParameters) {
				String nameWeShouldUse = ModelUtils
						.getFullNameButWithoutModelName(getFlow(), parameter);
				parameters.put(nameWeShouldUse, parameter.getExpression());
			}
		}
	}

	/**
	 * Probably not needed, but one never knows
	 */
	public void applyStepParametersToFlow() {
		// NOTHIN TO DO
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns <code>true</code> if this <code>Step</code> is the same as the o
	 * argument.
	 * 
	 * @return <code>true</code> if this <code>Step</code> is the same as the o
	 *         argument.
	 */
	@Override
	public boolean equals(Object object) {
		boolean result = false;
		if (this == object) {
			result = true;
		} else if (object == null) {
			result = false;
		} else if (object.getClass() != getClass()) {
			result = false;
		} else {
			Step castedObj = (Step) object;
			if ((this.name == null ? castedObj.name == null : this.name
					.equals(castedObj.name))
					&& (this.iterationCount == castedObj.iterationCount)) {
				result = true;
			}
		}
		return result;
	}

	public Map<String, Object> getContexts() {
		return contexts;
	}

	public void addContext(String name, String value) {
		contexts.put(name, value);
	}

	public void setParameters(Map<String, String> paramsToSet) {
		this.parameters = paramsToSet;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setOnFault(boolean value) {
		this.onFault = value;
	}

	public boolean isOnFault() {
		return this.onFault;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		//Step inst = null;
	
	// See bug 26369 :
	    // During copy a clone is called and during the past too
	    // At past step the parameters was lose. 
	    // Before realize the step copy its is important to be sure that the this.parameters is up to date with the 
	    // memory information.
	    // The distinguish between the step copy and past copy is that the flow is null (see more far in the code)
            if(this.flow != null) {
                applyFlowParametersToStep();
            }
            // end bug 26369
		//inst = (Step) super.clone();
	    
	    Step inst = new Step();
		inst.setType(this.type);
		// Step inst = new Step(type, name);
		inst.name = this.name == null ? null : this.name;
		inst.iterationCount = this.iterationCount;
		inst.enable = this.enable;
		inst.comment = this.comment == null ? null : this.comment;
		inst.parameters = this.parameters == null ? null
				: (Map<String, String>) new HashMap<String, String>(parameters);
		inst.contexts = this.contexts == null ? null
				: (Map<String, Object>) new HashMap<String, Object>(contexts);
		
		// Fix Sonar critical alert JC Pret Jan 2011
		// Setting flow to null is not needed as flow has never been assigned
		inst.flow = null;
		// End fix Sonar critical alert 
		return inst;
	}

	public String getDetails() {
		String result = "Step Details:\n";
		result += "\n- Name:  " + getName();
		result += "\n- Type :"
				+ ((getType().equals(StepType.ACTOR)) ? "Actor" : "Sequence");
		result += "\n- Iterations " + getIterationCount();
		result += "\n- Comment: " + getComment();
		return result;
	}

	public void resetParameters() {
		this.parameters = new HashMap<String, String>();
	}
	
	// Added to avoid FindBugs bug JC Pret Jan 2011
	public int hashCode()
	{
		  assert false : "hashCode not designed";
		  return 42; // any arbitrary constant will do 
	}
	// End JC Pret
	
	public boolean isBlockDelimiter()
	{
		return name.equals("Block");
	}

}
