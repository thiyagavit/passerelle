package com.isencia.passerelle.process.actor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.edm.actor.common.SynchronousTaskBasedActor;
import com.isencia.passerelle.edm.engine.api.service.ServicesRegistry;
import com.isencia.passerelle.process.actor.activator.Activator;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.util.ptolemy.ParameterGroup;

public class DbQueryActor extends SynchronousTaskBasedActor {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DbQueryActor.class);

	public StringParameter resultTypeParam;			// NOSONAR

	public StringParameter queryParam;				// NOSONAR
	public StringParameter schemaParam;				// NOSONAR
	public StringParameter categoryColumnParam;		// NOSONAR

	public StringParameter urlParam;				// NOSONAR
	public StringParameter userParam;				// NOSONAR
	public StringParameter passwordParam;			// NOSONAR	
	public StringParameter driverParam;				// NOSONAR
	public ptolemy.data.expr.Parameter maxResultCountParam;	// NOSONAR

	public DbQueryActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);

		resultTypeParam = new StringParameter(this, RESULT_TYPE);
		resultTypeParam.setExpression(name);

		queryParam = new StringParameter(this, "query");
		categoryColumnParam = new StringParameter(this, "category column");
		maxResultCountParam = new ptolemy.data.expr.Parameter(this, "max result count", new IntToken(100));

		ParameterGroup connParamGrp = new ParameterGroup(this, "Connection info");
		urlParam = new StringParameter(connParamGrp, "DB URL");
		schemaParam = new StringParameter(connParamGrp, "schema");
		userParam = new StringParameter(connParamGrp, "user");
		passwordParam = new StringParameter(connParamGrp, "password");
		driverParam = new StringParameter(connParamGrp, "jdbc driver");

		_attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:orange;stroke:orange\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"0\" cy=\"0\" r=\"10\""
				+ "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
				+ "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" "
				+ "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
	}

//	@Override
//	protected Context doProcess(Context taskContext, Context diagnosisContext, ManagedMessage message) throws ProcessingException {
//		try {
//			// TODO: this probably will no longer work ... taskContext doesn't know about the blocks
//			Task task = (Task)taskContext.getRequest();
//			ResultBlock resultBlock = executeQuery(task);
//			task.addResultBlock(resultBlock);
//			
//			return (Context)ServicesRegistry.getInstance().getLifeCycleEntityEventPublisher().notifyFinished(taskContext);
//		} catch (Exception e) {
//			String msg = "Error obtaining DB query results";
//			throw new ProcessingException(msg, message, e);
//		}
//	}


	/* (non-Javadoc)
	 * @see com.isencia.passerelle.edm.actor.common.AbstractTaskBasedActor#handle(java.lang.Class, com.isencia.passerelle.diagnosis.Context, java.util.Map)
	 */
	@Override
	protected Context handle(Context taskContext, Context flowContext, Map<String, String> actorAttributes) throws ProcessingException {
		try {
			Task task = (Task)taskContext.getRequest();
			ResultBlock resultBlock = executeQuery(task);
			task.addResultBlock(resultBlock);
	
			return (Context)ContextManagerProxy.notifyFinished(taskContext);
		} catch (Exception e) {
			String msg = "Error obtaining DB query results";
			throw new ProcessingException(msg, taskContext, e);
		}
	}
	
	protected ResultBlock executeQuery(Task task) throws Exception {
		String query = queryParam.getExpression();
		String url = urlParam.getExpression();
		String user = userParam.getExpression();
		String password = passwordParam.getExpression();
		String driver = driverParam.getExpression();
		String categoryColumn = categoryColumnParam.getExpression();

		Connection connection = getDBConnection(url, user, password, driver);
		ResultSet resultSet = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery(query);

		ResultSetMetaData metaData = resultSet.getMetaData();
		String[] columnNames = new String[metaData.getColumnCount()];
		for (int column = 0; column < metaData.getColumnCount(); column++) {
			if (!metaData.getColumnLabel(column + 1).equalsIgnoreCase(categoryColumn)) {
				columnNames[column] = metaData.getColumnLabel(column + 1);
			}
		}

		ResultBlock resultBlock = ServicesRegistry.getInstance().getDiagnosisEntityFactory().createResultBlock(resultTypeParam.getExpression(), task);

		try {
			int maxResultCount = ((IntToken) maxResultCountParam.getToken()).intValue();
			int rowCtr = 0;
			while (resultSet.next() && rowCtr++ < maxResultCount) {
				String itemNamePrefix = "";
				String itemNamePostFix = "";
				try {
					itemNamePrefix = resultSet.getObject(categoryColumn).toString() + ".";
				} catch (Exception t) {
					// then we don't prefix the results but put an array index
					// as postfix
					itemNamePostFix = "[" + rowCtr + "]";
				}
				for (int i = 0; i < columnNames.length; i++) {
					if (columnNames[i] != null) {
						String columnName = columnNames[i];
						String itemName = itemNamePrefix + columnName + itemNamePostFix;
						Object itemValueRaw = resultSet.getObject(columnName);
						String itemValue = itemValueRaw != null ? itemValueRaw.toString() : null;
						if (itemValue != null) {
							ServicesRegistry.getInstance().getDiagnosisEntityFactory().createResultItem(itemName, itemValue, null, resultBlock);
						}
					}
				}
			}
			return resultBlock;
		} catch (Exception e) {
			throw e;
		}

	}

	protected static Connection getDBConnection(String url, String user, String password, String driver) throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(url, user, password);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.edm.actor.common.AbstractTaskBasedActor#initActivatorOutsideOSGi()
	 */
	@Override
	protected void initActivatorOutsideOSGi() {
		Activator.initOutsideOSGi();
	}
}
