package fr.soleil.passerelle.cdma.actor;

import java.util.List;
import org.cdma.engine.sql.navigation.SqlQueryDataset;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.IGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

public class CDMASqlQueryActor extends Actor {
  private static final long serialVersionUID = -2568609907932694337L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMASqlQueryActor.class);
  public Port output;
  public static final String DB_URL = "DB URL";
  public static final String USER = "user";
  public static final String PASSWORD = "password";
  public static final String DB_QUERY = "query";
  public static final String CATEGORY_COLUMN = "category column";
  public static final String MAX_RESULT_COUNT = "max result count";
  public StringParameter queryParam;
  public StringParameter categoryColumnParam;
  public StringParameter urlParam;
  public StringParameter userParam;
  public StringParameter passwordParam;
  public Parameter maxResultCountParam;

  public CDMASqlQueryActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    queryParam = new StringParameter(this, DB_QUERY);
    categoryColumnParam = new StringParameter(this, CATEGORY_COLUMN);
    maxResultCountParam = new Parameter(this, MAX_RESULT_COUNT, new IntToken(100));
    urlParam = new StringParameter(this, DB_URL);
    userParam = new StringParameter(this, USER);
    passwordParam = new StringParameter(this, PASSWORD);
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "MySQL driver not found", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    SqlQueryDataset sqlDS = null;
    try {
      sqlDS = new SqlQueryDataset("SQL", urlParam.stringValue(), userParam.stringValue(), passwordParam.stringValue(), queryParam.stringValue());
      IGroup root = sqlDS.getRootGroup();
      if (root == null) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "No data set found for " + queryParam.stringValue(), this, null);
      }
      List<IGroup> groups = root.getGroupList();
      for (IGroup iGroup : groups) {
        List<IDataItem> items = iGroup.getDataItemList();
        for (IDataItem sqlDataItem : items) {
          System.out.println(sqlDataItem.getName());
        }
      }
      ManagedMessage message = createMessage();
      try {
        message.setBodyContent(sqlDS, ManagedMessage.objectContentType);
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "", this, e);
      }
      response.addOutputMessage(output, message);
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    } finally {
      requestFinish();
    }
  }

  // when should a SqlDataSet be closed?
  // if (sqlDS != null) {
  // try {
  // sqlDS.close();
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // }
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
