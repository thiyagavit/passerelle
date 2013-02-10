package com.isencia.passerelle.process.actor;

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
public class MockTaskResultActor extends AsynchDelay {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(MockTaskResultActor.class);

  public StringParameter resultTypeParam; // NOSONAR
  public StringParameter resultItemsParameter; // NOSONAR

  public MockTaskResultActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
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
