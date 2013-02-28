/**
 * 
 */
package com.isencia.passerelle.process.service;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.scheduler.ResourceToken;
import com.isencia.passerelle.process.model.Context;

/**
 * A LifeCycleEntity managed by a TaskScheduler.
 * The scheduler keeps track of the resource token(s) the context occupies.
 * 
 * @author puidir
 *
 */
public class ManagedContext {

	private Context context;
	private ResourceToken[] resourceTokens;
	
	public ManagedContext(Context entity, ResourceToken ... resourceTokens) {
		this.context = entity;
		this.resourceTokens = resourceTokens;	// NOSONAR we need the actual tokens, not some copy.
	}

	public Context getContext() {
		return context;
	}

	public ResourceToken[] getResourceTokens() {
		return resourceTokens;
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		
		if (rhs == null) {
			return false;
		}
		
		ManagedContext rhsEntity = (ManagedContext)rhs;
		
		return new EqualsBuilder().
			append(context, rhsEntity.context).
			append(resourceTokens, rhsEntity.resourceTokens).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(context).append(resourceTokens).hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ManagedContext:");
		buffer.append(" context: ");
		buffer.append(context);
		buffer.append(" resourceTokens: ");
		buffer.append(Arrays.toString(resourceTokens));
		buffer.append("]");
		return buffer.toString();
	}

}
