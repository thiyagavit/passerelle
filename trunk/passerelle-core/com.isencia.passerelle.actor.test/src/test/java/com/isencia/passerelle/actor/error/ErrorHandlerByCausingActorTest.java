package com.isencia.passerelle.actor.error;

import java.util.HashMap;
import java.util.Map;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import junit.framework.TestCase;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;

public class ErrorHandlerByCausingActorTest extends TestCase {
	private final FlowManager flowMgr = FlowManager.getDefault();

	public void testHandleErrorRegexpStrict() throws Exception {
		Flow flow = new Flow("testflowHandleErrorRegexpStrict", null);
		flow.setDirector(new Director(flow, "director"));

		Const source = new Const(flow, "Constant");
		ErrorHandlerByCausingActor actorUnderTest = new ErrorHandlerByCausingActor(
				flow, "ErrorHandler");
		Actor exceptionThrower = new ExceptionThrower(flow, "exceptionThrower");

		flow.connect(source, exceptionThrower);

		Map<String, String> props = new HashMap<String, String>();
		props.put("Constant.value", "Error-1234");
		// match exceptionThrower, source of ex
		props.put("ErrorHandler.match mode", "regexp strict");
		props.put(
				"ErrorHandler.actor name patterns",
				"exMatchPort=exception\\D{7}"
						+ System.getProperty("line.separator")
						+ "unusedPort=exception\\D{8}");
		flowMgr.executeBlockingLocally(flow, props);

		new FlowStatisticsAssertion()
		.expectMsgSentCount(source, 1L)
		.expectMsgSentCount(actorUnderTest, "exMatchPort", 1L)
		.expectMsgSentCount(actorUnderTest, "unusedPort", 0L)
		.assertFlow(flow);
	}
	
	public void testHandleErrorRegexpContains() throws Exception {
		Flow flow = new Flow("testflowHandleErrorRegexpContains", null);
		flow.setDirector(new Director(flow, "director"));

		Const source = new Const(flow, "Constant");
		ErrorHandlerByCausingActor actorUnderTest = new ErrorHandlerByCausingActor(
				flow, "ErrorHandler");
		Actor exceptionThrower = new ExceptionThrower(flow, "exceptionThrower");

		flow.connect(source, exceptionThrower);

		Map<String, String> props = new HashMap<String, String>();
		props.put("Constant.value", "Error-1234");
		// match exceptionThrower, source of ex
		props.put("ErrorHandler.match mode", "regexp contains");
		props.put(
				"ErrorHandler.actor name patterns",
				"exMatchPort=Thrower");
		flowMgr.executeBlockingLocally(flow, props);

		new FlowStatisticsAssertion()
		.expectMsgSentCount(source, 1L)
		.expectMsgSentCount(actorUnderTest, "exMatchPort", 1L)
		.assertFlow(flow);
	}
	
	public void testHandleErrorCamelCaseMatch() throws Exception {
		Flow flow = new Flow("testHandleErrorCamelCaseMatch", null);
		flow.setDirector(new Director(flow, "director"));

		Const source = new Const(flow, "Constant");
		ErrorHandlerByCausingActor actorUnderTest = new ErrorHandlerByCausingActor(
				flow, "ErrorHandler");
		Actor exceptionThrower = new ExceptionThrower(flow, "RandomExceptionThrower");

		flow.connect(source, exceptionThrower);

		Map<String, String> props = new HashMap<String, String>();
		props.put("Constant.value", "Error-1234");
		// match RandomExceptionThrower, source of ex
		props.put("ErrorHandler.match mode", "CamelCase match");
		props.put("ErrorHandler.actor name patterns", "exMatchPort=RaExThrower");
		flowMgr.executeBlockingLocally(flow, props);

		new FlowStatisticsAssertion()
		.expectMsgSentCount(source, 1L)
		.expectMsgSentCount(actorUnderTest, "exMatchPort", 1L)
		.assertFlow(flow);
	}
	
	public void testHandleErrorRegexpStrictUsingFullName() throws Exception {
		Flow flow = new Flow("testHandleErrorRegexpStrictUsingFullName", null);
		flow.setDirector(new Director(flow, "director"));

		Const source = new Const(flow, "Constant");
		ErrorHandlerByCausingActor actorUnderTest = new ErrorHandlerByCausingActor(
				flow, "ErrorHandler");
		Actor exceptionThrower = new ExceptionThrower(flow, "exceptionThrower");

		flow.connect(source, exceptionThrower);

		Map<String, String> props = new HashMap<String, String>();
		props.put("Constant.value", "Error-1234");
		// match exceptionThrower, source of ex
		props.put("ErrorHandler.match mode", "regexp strict");
		props.put(
				"ErrorHandler.actor name patterns",
				"exMatchPort=\\.testHandleErrorRegexpStrictUsingFullName\\.exception\\D{7}");
		props.put("ErrorHandler.match full name", "true");
		flowMgr.executeBlockingLocally(flow, props);

		new FlowStatisticsAssertion()
		.expectMsgSentCount(source, 1L)
		.expectMsgSentCount(actorUnderTest, "exMatchPort", 1L)
		.assertFlow(flow);
	}

	private class ExceptionThrower extends Transformer {
		public ExceptionThrower(CompositeEntity container, String name)
				throws NameDuplicationException, IllegalActionException {
			super(container, name);
		}

		private static final long serialVersionUID = 1L;

		@Override
		protected void doFire(ManagedMessage message)
				throws ProcessingException {
			throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR,
					"Test exception", this, message, null);
		}
	}
}
