package com.isencia.passerelle.derby;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This bundle activator is used to control the lifecycle of derby network server.
 * 
 */
public class Activator implements BundleActivator {

  private static final String DEFAULT_USER = "passerelle";
  private static final String DEFAULT_PASSWORD = "no12see";
  /**
   * constant for localhost
   */
  private static final String LOCALHOST = "localhost";
  /**
   * constant for default port
   */
  private static final int DEFAULT_PORT = 1527;

  /**
   * property to configure network server port
   */
  private static final String PORT_PROPERTY = "com.isencia.passerelle.edm.derby.port";
  /**
   * NetworkServerControl
   */
  private static NetworkServerControl mServerControl;

  /**
   * This method is used to start the network server. The network server port is could be configured using the system
   * property com.isencia.maestro.derby.port.
   * 
   * @param context
   * @throws java.lang.Exception
   */
  public void start(BundleContext context) throws Exception {
    System.setProperty("derby.connection.requireAuthentication", "true");
    System.setProperty("derby.authentication.provider", "BUILTIN");
    Properties properties = System.getProperties();
    if (properties == null || properties.isEmpty()) {
      setDefaultAuthenticationUser();
    }

    boolean authenticationUserFound = false;
    Enumeration<?> propertyNames = properties.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String propertyName = (String) propertyNames.nextElement();
      if (propertyName != null) {
        if (propertyName.startsWith("derby.user.")) {
          authenticationUserFound = true;
          break;
        }
      }
    }

    if (!authenticationUserFound) {
      setDefaultAuthenticationUser();
    }

    int port = DEFAULT_PORT;
    String portString = context.getProperty(PORT_PROPERTY);
    if (portString != null) {
      port = Integer.parseInt(portString);
    }
    mServerControl = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), port);

    mServerControl.start(new PrintWriter(System.out));

  }

  public void stop(BundleContext context) throws Exception {
    try {
      mServerControl.shutdown();
    } catch (Exception e) {

    }

  }

  private void setDefaultAuthenticationUser() {
    System.setProperty("derby.user." + DEFAULT_USER, DEFAULT_PASSWORD);
  }

}
