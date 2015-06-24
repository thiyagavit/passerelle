package fr.soleil.passerelle.cdma.actor;

import java.util.List;

import org.cdma.engine.sql.navigation.SqlQueryDataset;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.IGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
  
  public static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
  public static final String ORACLE_DATASOURCE = "oracle.jdbc.pool.OracleDataSource";
  public static final String DB_HOST = "DB host";
  public static final String DB_NAME = "DB name";
  public static final String DB_SCHEMA = "DB schema";
  public static final String DB_QUERY = "query";
  public static final String DB_DRIVER = "driver";
  public static final String USER = "user";
  public static final String PASSWORD = "password";
  public static final String CATEGORY_COLUMN = "category column";
  public static final String MAX_RESULT_COUNT = "max result count";
  public static final String ORACLE_RACK = "oracle rack";
  public StringParameter queryParam;
  public StringParameter dbHostParam;
  public StringParameter dbNameParam;
  public StringParameter dbSchemaParam;
  public StringParameter userParam;
  public StringParameter passwordParam;
  public StringParameter driverParam;
  public StringParameter categoryColumnParam;
  public Parameter isOracleRackParam;
  public Parameter maxResultCountParam;

  public CDMASqlQueryActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    queryParam = new StringParameter(this, DB_QUERY);
    dbHostParam = new StringParameter(this, DB_HOST);
    dbNameParam = new StringParameter(this, DB_NAME);
    dbSchemaParam = new StringParameter(this, DB_SCHEMA);
    driverParam = new StringParameter(this,DB_DRIVER);
    driverParam.setExpression(MYSQL_JDBC_DRIVER);
    driverParam.addChoice(MYSQL_JDBC_DRIVER);
    driverParam.addChoice(ORACLE_DATASOURCE);
    new ChoiceStyle(driverParam, "choice");
    
    userParam = new StringParameter(this, USER);
    passwordParam = new StringParameter(this, PASSWORD);
    
    isOracleRackParam = new Parameter(this,ORACLE_RACK, BooleanToken.FALSE);
    new CheckBoxStyle(isOracleRackParam, "check box");
    
    categoryColumnParam = new StringParameter(this, CATEGORY_COLUMN);
    maxResultCountParam = new Parameter(this, MAX_RESULT_COUNT, new IntToken(100));
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    SqlQueryDataset sqlDS = null;
    try {
      boolean isOracleRack = ((BooleanToken)isOracleRackParam.getToken()).booleanValue();
      sqlDS = new SqlQueryDataset("SQL", dbHostParam.stringValue(), 
          userParam.stringValue(), passwordParam.stringValue(), 
          driverParam.stringValue(), dbNameParam.stringValue(),
          dbSchemaParam.stringValue(), isOracleRack, queryParam.stringValue());
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
