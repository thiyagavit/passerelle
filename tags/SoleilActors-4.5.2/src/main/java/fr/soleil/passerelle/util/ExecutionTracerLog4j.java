package fr.soleil.passerelle.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ExecutionTracer;

@SuppressWarnings("serial")
public class ExecutionTracerLog4j implements ExecutionTracer {

	private final static Logger traceLogger = LoggerFactory.getLogger("trace");

	public void trace(final Actor source, final String message) {
		traceLogger.info(PasserelleUtil.getFullNameButWithoutModelName(source)
				+ " - " + message);
	}

	public void trace(final Director source, final String message) {
		traceLogger.info(source.getName() + " - " + message);
	}
}
