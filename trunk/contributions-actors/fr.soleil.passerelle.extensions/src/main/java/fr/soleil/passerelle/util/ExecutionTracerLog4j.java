package fr.soleil.passerelle.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.util.Level;

public class ExecutionTracerLog4j implements ExecutionTracer {

	private final static Logger traceLogger = LoggerFactory.getLogger("trace");

	public void trace(final Actor source, final String message) {
		traceLogger.info(PasserelleUtil.getFullNameButWithoutModelName((NamedObj)source)
				+ " - " + message);
	}

	public void trace(final Director source, final String message) {
		traceLogger.info(source.getName() + " - " + message);
	}

  @Override
  public void trace(Actor source, String message, Level level) {
    trace(source, message);
  }

  @Override
  public void trace(Director source, String message, Level level) {
    trace(source, message);
  }
}
