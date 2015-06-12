/*
 * (c) Copyright 2004, iSencia NV Belgium
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia NV, Belgium.  
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.actor.control;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * A Chronometer with 2 blocking ports: set and reset. The actor will always first wait for a message on the set port. Then it will look for a message on the
 * reset port. The time interval (in ms) between the two is sent out in the body of the outgoing message. If a reset message has already arrived before a set
 * message, the reset will be immediately noticed after the set, and the resulting time interval will be approximately 0. If another set message arrives before
 * the reset, it means that immediately after consuming the reset, in the next fire() iteration, the actor will be set again. So, the Chronometer does not drop
 * any messages and it will almost continuously be blocked on one of the 2 input ports.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class Chronometer extends Transformer {

   private static final long serialVersionUID = 15219111900378353L;

   private static Logger logger = LoggerFactory.getLogger(Chronometer.class);

  // public Port setInputPort = null;
  public Port resetInputPort = null;

  // public Port output = null;

  public Chronometer(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // setInputPort = PortFactory.getInstance().createInputPort(this, "set",
    // null);
    input.setName("set");
    resetInputPort = PortFactory.getInstance().createInputPort(this, "reset", null);

    // output = PortFactory.getInstance().createOutputPort(this, "resetOk");
    output.setName("resetOk");
    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"

        + "<circle cx=\"0\" cy=\"0\" r=\"16\"" + "style=\"fill:white\"/>\n" + "<line x1=\"0\" y1=\"-14\" x2=\"0\" y2=\"-12\"/>\n"
        + "<line x1=\"0\" y1=\"12\" x2=\"0\" y2=\"14\"/>\n" + "<line x1=\"-14\" y1=\"0\" x2=\"-12\" y2=\"0\"/>\n"
        + "<line x1=\"12\" y1=\"0\" x2=\"14\" y2=\"0\"/>\n" + "<line x1=\"0\" y1=\"-7\" x2=\"0\" y2=\"0\"/>\n"
        + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\"/>\n" + "</svg>\n");
  }

  @Override
  protected void doFire(final ManagedMessage arg0) throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - entry");
    }

    ManagedMessage message = null;
    long setTime = 0;
    long resetTime = 0;
    setTime = new Date().getTime();
    try {
      MessageHelper.getMessage(resetInputPort);
    } catch (final PasserelleException e1) {
        ExceptionUtil.throwProcessingException("can't get input message", resetInputPort, e1);
    }
    resetTime = new Date().getTime();

    final long totalTime = resetTime - setTime;
    ExecutionTracerService.trace(this, "measured " + totalTime + " ms");
    message = PasserelleUtil.createContentMessage(this, Double.toString(totalTime));

    try {
      sendOutputMsg(output, message);
    } catch (final IllegalArgumentException e) {
        ExceptionUtil.throwProcessingException(getName() + " - doFire() generated exception " + e, message, e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - exit");
    }
  }

  @Override
  protected String getAuditTrailMessage(final ManagedMessage message, final Port port) {
    try {
      return "sent chronometer message with time " + message.getBodyContentAsString();
    } catch (MessageException e) {
      getLogger().error("Error getting msg content",e);
      return "";
    }
  }

  /**
   * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
   */
  @Override
  protected String getExtendedInfo() {
    return "";
  }

}