/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.actor.v5;

import java.io.BufferedReader;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.EntityFactory;
import com.isencia.passerelle.process.model.factory.EntityManager;
import com.isencia.passerelle.process.model.service.ServiceRegistry;
import com.isencia.passerelle.testsupport.actor.AsynchDelay;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * A mock impl of an asynch task actor with configurable result items.
 * 
 * @author erwin
 *
 */
public class TaskResultActor extends AsynchDelay {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskResultActor.class);

  public StringParameter resultTypeParam; // NOSONAR
  public StringParameter resultItemsParameter; // NOSONAR

  public TaskResultActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    resultTypeParam = new StringParameter(this, "Result type");
    resultTypeParam.setExpression(name);
    resultItemsParameter = new StringParameter(this, "Result items");
    new TextStyle(resultItemsParameter, "paramsTextArea");
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doProcess(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    String resultType = resultTypeParam.getExpression();
    if (message != null) {
      try {
        EntityFactory entityFactory = ServiceRegistry.getInstance().getEntityFactory();
        EntityManager entityManager = ServiceRegistry.getInstance().getEntityManager();
        
        Context processContext = (Context) message.getBodyContent();
        Task task = entityFactory.createTask(processContext, FlowUtils.getFullNameWithoutFlow(this), resultType);
        
        task = (Task) entityManager.persistRequest(task);
        
        ResultBlock rb = entityFactory.createResultBlock(task, resultType);

        String paramDefs = ((StringToken) resultItemsParameter.getToken()).stringValue();
        BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
        String paramDef = null;
        while ((paramDef = reader.readLine()) != null) {
          String[] paramKeyValue = paramDef.split("=");
          if (paramKeyValue.length == 2) {
            entityFactory.createResultItem(rb, paramKeyValue[0], paramKeyValue[1], null);
          } else {
            ExecutionTracerService.trace(this, "Invalid mapping definition: " + paramDef);
          }
        }
        response.addOutputMessage(output, message);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating dummy results for " + resultType, this, message, e);
      }
    }
  }

}
