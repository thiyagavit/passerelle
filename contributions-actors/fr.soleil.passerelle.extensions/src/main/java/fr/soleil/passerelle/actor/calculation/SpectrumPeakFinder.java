package fr.soleil.passerelle.actor.calculation;

import java.util.Collections;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;

//TODO: mock mode
@SuppressWarnings("serial")
public class SpectrumPeakFinder extends Actor {

  private final static Logger logger = LoggerFactory.getLogger(SpectrumPeakFinder.class);

  /** The input ports */
  public Port x;
  public Port y;

  /** The output ports */
  public Port positionX;
  public Port maxValue;

  private PortHandler xHandler = null;
  private PortHandler yHandler = null;
  private boolean xReceived = false;
  private boolean yReceived = false;

  boolean tokenIsNull = false;

  private double[] xValues;
  private double[] yValues;

  public Parameter maxPercentParam;
  private Double maxPercent = new Double(100.0);

  public Parameter maxSideParam;
  private String maxSide = "up";

  public SpectrumPeakFinder(final CompositeEntity arg0, final String arg1) throws IllegalActionException, NameDuplicationException {
    super(arg0, arg1);
    // Ports
    x = PortFactory.getInstance().createInputPort(this, "x", String.class);
    y = PortFactory.getInstance().createInputPort(this, "y", String.class);

    positionX = PortFactory.getInstance().createOutputPort(this, "positionX");

    maxValue = PortFactory.getInstance().createOutputPort(this, "maxValue");

    maxPercentParam = new StringParameter(this, "Max Percent");
    maxPercentParam.setExpression(maxPercent.toString());
    registerConfigurableParameter(maxPercentParam);

    maxSideParam = new StringParameter(this, "High Energy");
    maxSideParam.addChoice("up");
    maxSideParam.addChoice("down");
    maxSideParam.setExpression(maxSide);
    registerConfigurableParameter(maxSideParam);

  }

  @Override
  public void doInitialize() throws InitializationException {

    tokenIsNull = false;
    xReceived = false;
    yReceived = false;

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - entry");
    }

    // If something connected to the set port, install a handler
    if (x.getWidth() > 0) {
      // System.out.println("x.getWidth() > 0");
      xHandler = new PortHandler(x, new PortListener() {
        public void tokenReceived() {
          // System.out.println("xHandler.tokenReceived() ");
          final Token token = xHandler.getToken();
          // System.out.println("leftHandler.tokenReceived: " +
          // token);
          if (token != null && token != Token.NIL) {
            try {
              final ManagedMessage message = MessageHelper.getMessageFromToken(token);
              logger.debug("xHandler.tokenReceived() :" + message.getBodyContent());
              final Object data = message.getBodyContent();
              String[] input;
              if (data.getClass().isArray()) {
                input = (String[]) message.getBodyContent();
                xValues = new double[input.length];
              } else {
                input = ((String) message.getBodyContent()).split(",");

              }
              xValues = new double[input.length];
              for (int i = 0; i < xValues.length; i++) {
                xValues[i] = Double.valueOf(input[i]);
              }
              // final TangoAttribute attr = (TangoAttribute)
              // message
              // .getBodyContent();
              // //xValues =
              // attr.extractSpecOrImage(Double.class);
              // System.out.println("xHandler.tokenReceived() :" +
              // xValues[0]);
            } catch (final MessageException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (final PasserelleException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            // System.out.println(obj.getClass());
            xReceived = true;
            System.out.println("x received");
            performNotify();
          }
        }

        public void noMoreTokens() {
          do {
            performWait(1000);
          } while (xReceived);
          System.out.println("no more tokens x");
          tokenIsNull = true;
          xReceived = true;
          performNotify();
        }
      });
      if (xHandler != null) {
        xHandler.start();
        // System.out.println("xHandler start");
      }
    }
    if (y.getWidth() > 0) {
      // System.out.println("y.getWidth() > 0");
      yHandler = new PortHandler(y, new PortListener() {
        public void tokenReceived() {
          // System.out.println("yHandler.tokenReceived() ");
          final Token token = yHandler.getToken();
          // System.out.println("rightHandler.tokenReceived: " +
          // token);
          if (token != null && token != Token.NIL) {
            try {
              final ManagedMessage message = MessageHelper.getMessageFromToken(token);
              logger.debug("yHandler.tokenReceived() :" + message.getBodyContent());
              final Object data = message.getBodyContent();
              String[] input;
              if (data.getClass().isArray()) {
                input = (String[]) message.getBodyContent();
              } else {
                input = ((String) message.getBodyContent()).split(",");
              }
              yValues = new double[input.length];
              for (int i = 0; i < yValues.length; i++) {
                yValues[i] = Double.valueOf(input[i]);
              }
            } catch (final MessageException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (final PasserelleException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            yReceived = true;
            System.out.println("y received");
            performNotify();
          }
        }

        public void noMoreTokens() {
          do {
            performWait(1000);
          } while (yReceived);
          System.out.println("no more tokens y");
          tokenIsNull = true;
          yReceived = true;
          performNotify();
        }
      });
      if (yHandler != null) {
        yHandler.start();
        // System.out.println("yHandler start");
      }

    }
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - exit");
    }
  }

  private synchronized void performNotify() {
    notify();
  }

  private synchronized void performWait(final int time) {
    try {
      if (time == -1) {
        wait();
      } else {
        wait(time);
      }
    } catch (final InterruptedException e) {
    }
  }

  @Override
  protected String getExtendedInfo() {
    return this.getName();
  }

  @Override
  public void attributeChanged(final Attribute attribute) throws IllegalActionException {
    if (attribute == maxPercentParam) {
      maxPercent = new Double(((StringToken) maxPercentParam.getToken()).stringValue());
    }
    if (attribute == maxSideParam) {
      maxSide = ((StringToken) maxSideParam.getToken()).stringValue();
    } else {
      super.attributeChanged(attribute);
    }

  }

  @Override
  protected void doFire() throws ProcessingException {
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - entry");
    }

    // wait for the 2 inputs
    while (!xReceived || !yReceived) {
      performWait(1000);
      // System.out.println("PeakFinder waiting...");
    }

    if (!tokenIsNull) {
      // System.out.println("PeakFinder performe calc");

      final Vector<Double> xVect = new Vector<Double>(xValues.length);
      for (final Double value : xValues) {
        xVect.add(new Double(value));
      }

      final Vector<Double> yVect = new Vector<Double>(yValues.length);
      for (final Double value : yValues) {
        yVect.add(new Double(value));
      }

      if (xVect.size() != yVect.size()) {
          ExceptionUtil.throwProcessingException("The two input tables must have the same size", this);
      }

      // find the max
      final Double max = Collections.max(yVect);
      final double maxWithPercent = max.doubleValue() * maxPercent.doubleValue() / 100;
      // System.out.println("max is "+ max);
      // System.out.println("maxWithPercent is "+ maxWithPercent);
      final int pos = yVect.indexOf(max);
      int posPercent = 0;
      double realValue = 0;
      double newRealValue = 0;
      double oldDiff = 0;
      double newDiff = 0;
      if (maxSide.equals("up")) {
        for (int i = 0; i <= pos; i++) {
          realValue = yVect.get(i).doubleValue();
          newDiff = Math.abs(realValue - maxWithPercent);
          /*
           * System.out.println("=newDiff "+ newDiff); System.out.println("=realValue "+ realValue);
           * System.out.println("=posPercent "+ posPercent);
           */
          if (i == 0) {
            newRealValue = realValue;
            oldDiff = newDiff;
          } else if (newDiff < oldDiff) {
            oldDiff = newDiff;
            newRealValue = realValue;
            posPercent = i;
            /*
             * System.out.println("***newDiff "+ newDiff); System.out.println("***realValue "+ realValue);
             * System.out.println("***posPercent "+ posPercent);
             */
          }
        }

      } else { // down
        for (int i = pos; i < yVect.size(); i++) {
          realValue = yVect.get(i).doubleValue();
          newDiff = Math.abs(realValue - maxWithPercent);
          /*
           * System.out.println("=newDiff "+ newDiff); System.out.println("=realValue "+ realValue);
           * System.out.println("=posPercent "+ posPercent);
           */
          if (i == pos) {
            newRealValue = realValue;
            oldDiff = newDiff;
          } else if (newDiff < oldDiff) {
            oldDiff = newDiff;
            newRealValue = realValue;
            posPercent = i;
            /*
             * System.out.println("***newDiff "+ newDiff); System.out.println("***realValue "+ realValue);
             * System.out.println("***posPercent "+ posPercent);
             */
          }
        }
      }
      /*
       * System.out.println("------"); System.out.println("index of max is "+ yVect.indexOf(max));
       */
      // Double positionXval = (Double)xVect.get(pos);
      /*
       * System.out.println("position of max is "+ positionXval); System.out.println("-------");
       * System.out.println("index of max with percent is "+ posPercent);
       */
      final Double positionXvalPer = xVect.get(posPercent);
      // System.out.println("position of max with percent is "+
      // positionXvalPer);
      ExecutionTracerService.trace(this, "positionX is " + positionXvalPer);
      ExecutionTracerService.trace(this, "maxValue is " + newRealValue);

      try {
        final ManagedMessage resultMsg = createMessage();
        resultMsg.setBodyContent(positionXvalPer, ManagedMessage.objectContentType);
        sendOutputMsg(positionX, resultMsg);

        final ManagedMessage resultMsg2 = createMessage();
        resultMsg2.setBodyContent(new Double(newRealValue), ManagedMessage.objectContentType);
        sendOutputMsg(maxValue, resultMsg2);

      } catch (final MessageException e) {
          ExceptionUtil.throwProcessingException("Cannot sand output data", this,e);
      }

    } else {
      logger.debug("PeakFinder: request finished");
      requestFinish();
    }

    xReceived = false;
    yReceived = false;

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - exit");
    }
  }

}
