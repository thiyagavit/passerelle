/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rpc.flattening;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.ExceptionHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.ListHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.MapHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.NoneFlatteningHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.ObjectArrayHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.PassThroughFlatteningHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.PrimitiveBoolArrayHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.PrimitiveDoubleArrayHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.PrimitiveIntArrayHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.SelfFlattensHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.StackTraceElementHelper;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers.UUIDHelper;

/**
 * {@link IRootFlattener} implementation to flatten/unflatten objects for transportation over XMLRPC.
 */
public class RootFlattener implements IRootFlattener {

	private List<IFlattener<?>> flatteningHelpers;
	private File tempLocation;

	/**
	 * Create a new {@link RootFlattener}.
	 * <p>
	 * NOTE: If you are using AnalysisRpc client/server you probably want to use the singleton version of this class
	 * provided by {@link FlatteningService#getFlattener()}. 
	 */
	public RootFlattener() {
		flatteningHelpers = new LinkedList<IFlattener<?>>();

		// These are used in order by flatten() so how they are added implies
		// precedence
		flatteningHelpers.add(new SelfFlattensHelper());
		flatteningHelpers.add(new NoneFlatteningHelper());
		flatteningHelpers.add(new UUIDHelper());
		flatteningHelpers.add(new PassThroughFlatteningHelper());
		flatteningHelpers.add(new ListHelper());
		flatteningHelpers.add(new ExceptionHelper());
		flatteningHelpers.add(new StackTraceElementHelper());
		flatteningHelpers.add(new MapHelper());
		flatteningHelpers.add(new PrimitiveIntArrayHelper());
		flatteningHelpers.add(new PrimitiveDoubleArrayHelper());
		flatteningHelpers.add(new PrimitiveBoolArrayHelper());
		flatteningHelpers.add(new ObjectArrayHelper());
	}

	@Override
	public Object flatten(Object obj) throws UnsupportedOperationException {
		for (Iterator<IFlattener<?>> iterator = flatteningHelpers.iterator(); iterator.hasNext();) {
			IFlattener<?> flatteningHelper = iterator.next();
			if (flatteningHelper.canFlatten(obj))
				return flatteningHelper.flatten(obj, this);
		}

		throw new UnsupportedOperationException("Value " + obj.toString() + " is of unknown type");
	}

	/**
	 * Take an object that has been flattened for transmission over XML RPC and try to reconstruct the original object.
	 * Containers are always returned as arrays of unflattened objects. Known types e.g. RectangularROI are
	 * reconstructed to their respective types.
	 * 
	 * @param obj
	 *            a flattened object. This object is not modified.
	 * @return the unflattened version of obj
	 */
	@Override
	public Object unflatten(Object obj) {
		for (Iterator<IFlattener<?>> iterator = flatteningHelpers.iterator(); iterator.hasNext();) {
			IFlattener<?> flatteningHelper = iterator.next();
			if (flatteningHelper.canUnFlatten(obj))
				return flatteningHelper.unflatten(obj, this);
		}
		throw new UnsupportedOperationException("Value " + obj.toString() + " is of unknown type");
	}

	@Override
	public boolean canFlatten(Object obj) {
		for (Iterator<IFlattener<?>> iterator = flatteningHelpers.iterator(); iterator.hasNext();) {
			IFlattener<?> flatteningHelper = iterator.next();
			if (flatteningHelper.canFlatten(obj))
				return true;
		}
		return false;
	}

	@Override
	public boolean canUnFlatten(Object obj) {
		if (obj == null) {
			return false;
		}

		for (Iterator<IFlattener<?>> iterator = flatteningHelpers.iterator(); iterator.hasNext();) {
			IFlattener<?> flatteningHelper = iterator.next();
			if (flatteningHelper.canUnFlatten(obj))
				return true;
		}
		return false;
	}

	@Override
	public void setTempLocation(String tempLocation) {
		if (tempLocation == null) {
			this.tempLocation = null;
		} else {
			this.tempLocation = new File(tempLocation);
		}
	}

	@Override
	public File getTempLocation() {
		return tempLocation;
	}

	@Override
	public void addHelper(IFlattener<?> helper) {
		flatteningHelpers.add(0, helper);
	}

}
