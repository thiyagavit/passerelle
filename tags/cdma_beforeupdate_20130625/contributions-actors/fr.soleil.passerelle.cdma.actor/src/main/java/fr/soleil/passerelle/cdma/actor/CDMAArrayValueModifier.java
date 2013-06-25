/* Copyright 2013 - Synchrotron Soleil

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package fr.soleil.passerelle.cdma.actor;

import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IArrayIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;

/**
 * Simple trial to check all numerical values in an iterator,
 * and apply lower/upper boundaries.
 * 
 * I.e. each value that is lower than the lowerBoundary is set to the lowerBoundary,
 * and each value that is higher than the upperBoundary is set to the upperBoundary.
 * 
 * In this simple test, we assume values are doubles.
 * @author delerw
 */
public class CDMAArrayValueModifier extends CDMAArrayTransformer {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAArrayValueModifier.class);

  public Parameter lowerBoundaryValueParameter;
  public Parameter upperBoundaryValueParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMAArrayValueModifier(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    
    lowerBoundaryValueParameter = new Parameter(this,"Lower boundary",new DoubleToken(0.005));
    upperBoundaryValueParameter = new Parameter(this,"Upper boundary",new DoubleToken(5.0));
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected IArray transformArray(IArray rcvdArray) throws ProcessingException {
    try {
      double lowerBoundary = ((DoubleToken)lowerBoundaryValueParameter.getToken()).doubleValue();
      double upperBoundary = ((DoubleToken)upperBoundaryValueParameter.getToken()).doubleValue();
      IArrayIterator itr = rcvdArray.getIterator();
      while(itr.hasNext()) {
        double d = itr.getDoubleNext();
        if(d<lowerBoundary) {
          itr.setDouble(lowerBoundary);
        } else if(d>upperBoundary) {
          itr.setDouble(upperBoundary);
        }
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error setting modified value", this, e);
    }
    return rcvdArray;
  }
}
