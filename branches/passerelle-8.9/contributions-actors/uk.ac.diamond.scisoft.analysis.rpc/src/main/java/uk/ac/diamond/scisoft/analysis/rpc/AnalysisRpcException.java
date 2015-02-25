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

package uk.ac.diamond.scisoft.analysis.rpc;

/**
 * Wrapper exception for any one of a number of possible failures. Call
 * {@link #getCause()} for the underlying exception
 */
public class AnalysisRpcException extends Exception {
	private static final long serialVersionUID = 8996421526351837418L;

	public AnalysisRpcException() {
		super();
	}

	public AnalysisRpcException(String s) {
		super(s);
	}

	public AnalysisRpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnalysisRpcException(Throwable cause) {
		super(cause);
	}

}
