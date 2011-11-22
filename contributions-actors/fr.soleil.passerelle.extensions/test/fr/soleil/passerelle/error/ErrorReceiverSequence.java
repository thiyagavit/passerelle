package fr.soleil.passerelle.error;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import be.isencia.passerelle.core.PasserelleException;
import be.isencia.passerelle.domain.cap.Director;
import be.isencia.passerelle.model.Flow;
import be.isencia.passerelle.model.FlowAlreadyExecutingException;
import be.isencia.passerelle.model.FlowManager;
import fr.soleil.passerelle.domain.BasicDirector;

public class ErrorReceiverSequence {

	private Reader in;
	private Director dir;
	private Flow topLevel;
	private FlowManager flowMgr;

	@Test(expected = PasserelleException.class)
	public void testWithError() throws FlowAlreadyExecutingException,
			PasserelleException, IllegalActionException,
			NameDuplicationException {
		in = new InputStreamReader(getClass().getResourceAsStream(
				"/fr/soleil/passerelle/resources/errorreceiver.moml"));
		flowMgr = new FlowManager();
		try {
			topLevel = FlowManager.readMoml(in);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		dir = new BasicDirector(topLevel, "Dir");
		topLevel.setDirector(dir);

		final Map<String, String> props = new HashMap<String, String>();
		props.put("Dir.Mock Mode", "true");
		props.put("BooleanSelection.output true", "true");
		props.put("ErrorGenerator.error type", "fire");
		props.put("ErrorGenerator.severity", "NON_FATAL");
		flowMgr.executeBlockingErrorLocally(topLevel, props);
	}

	@Test
	public void testWithOutError() throws FlowAlreadyExecutingException,
			PasserelleException, IllegalActionException,
			NameDuplicationException {
		in = new InputStreamReader(getClass().getResourceAsStream(
				"/fr/soleil/passerelle/resources/errorreceiver.moml"));
		flowMgr = new FlowManager();
		try {
			topLevel = FlowManager.readMoml(in);
		} catch (final Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
		dir = new BasicDirector(topLevel, "Dir");
		topLevel.setDirector(dir);

		final Map<String, String> props = new HashMap<String, String>();
		props.put("Dir.Mock Mode", "true");
		props.put("BooleanSelection.output true", "false");
		props.put("ErrorGenerator.error type", "fire");
		props.put("ErrorGenerator.severity", "NON_FATAL");
		flowMgr.executeBlockingErrorLocally(topLevel, props);
	}

	@Test(expected = PasserelleException.class)
	public void testWithErrorAndRetry() throws FlowAlreadyExecutingException,
			PasserelleException, IllegalActionException,
			NameDuplicationException {
		in = new InputStreamReader(getClass().getResourceAsStream(
				"/fr/soleil/passerelle/resources/errorreceiver.moml"));
		flowMgr = new FlowManager();
		try {
			topLevel = FlowManager.readMoml(in);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		dir = new BasicDirector(topLevel, "Dir");
		topLevel.setDirector(dir);

		final Map<String, String> props = new HashMap<String, String>();
		props.put("Dir.Mock Mode", "true");
		props.put("Dir.Error Control", "retry");
		props.put("BooleanSelection.output true", "true");
		props.put("ErrorGenerator.error type", "fire");
		props.put("ErrorGenerator.severity", "NON_FATAL");
		flowMgr.executeBlockingErrorLocally(topLevel, props);
	}
}
