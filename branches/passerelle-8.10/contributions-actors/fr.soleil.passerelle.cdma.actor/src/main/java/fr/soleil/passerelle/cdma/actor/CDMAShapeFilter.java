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
import org.cdma.interfaces.IDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.eip.MessageFilter;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A simple CDMA-related filter, to check if a received <code>IDataItem</code> or <code>IArray</code> has a required shape.
 * <p>
 * If the required shape is left empty, no check is done. I.e. all messages will continue via the OK port.
 * </p>
 * 
 * @author delerw
 */
public class CDMAShapeFilter extends MessageFilter {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAShapeFilter.class);

  /**
   * Specifies the required shape as a comma-separated list of desired dimensions.
   * <p>
   * The nr of elements defines the required rank.
   * </p>
   * <p>
   * Elements may contain the '*' wild-card if a specific dimension may have arbitrary size, while making sure the received rank is ok overall.
   * </p>
   */
  public StringParameter shapeParameter;

  private Integer[] requiredShape;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMAShapeFilter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    shapeParameter = new StringParameter(this, "Required shape");
    shapeParameter.setExpression("5,10,40000");
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (attribute == shapeParameter) {
      String shapeSpec = ((StringToken) shapeParameter.getToken()).stringValue();
      if (shapeSpec.isEmpty()) {
        requiredShape = null;
      } else {
        String[] shapeElements = shapeSpec.split(",");
        requiredShape = new Integer[shapeElements.length];
        for (int i = 0; i < shapeElements.length; ++i) {
          String shapeElement = shapeElements[i];
          if ("*".equals(shapeElement)) {
            requiredShape[i] = null;
          } else {
            try {
              requiredShape[i] = Integer.parseInt(shapeElement);
            } catch (NumberFormatException e) {
              throw new IllegalActionException(this, "Illegal shape definition " + shapeSpec);
            }
          }
        }
      }
    } else {
      super.attributeChanged(attribute);
    }
  }

  @Override
  protected boolean isMatchingFilter(ManagedMessage msg) throws Exception {
    boolean result = true;
    if (requiredShape != null) {
      try {
        Object rcvdDataObject = msg.getBodyContent();
        int[] dataObjectShape = null;
        if (rcvdDataObject instanceof IDataItem) {
          dataObjectShape = ((IDataItem) rcvdDataObject).getShape();
        } else if (rcvdDataObject instanceof IArray) {
          dataObjectShape = ((IArray) rcvdDataObject).getShape();
        }

        if (requiredShape.length != dataObjectShape.length) {
          result = false;
        } else {
          for (int i = 0; i < requiredShape.length; ++i) {
            Integer requiredDim = requiredShape[i];
            if (requiredDim != null && (requiredDim != dataObjectShape[i])) {
              result = false;
              break;
            }
          }
        }
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
      }
    }
    return result;
  }

}
