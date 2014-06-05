package com.isencia.passerelle.process.model.impl.shbl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lookup resources in Jndi
 * 
 * @author Dirk Jacobs
 * 
 */
public class JndiResourceFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(JndiResourceFactory.class);

	public static Object getResource(String name) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getResource() - entry : " + name);
		}
		Object resource = null;
		try {
			resource = findJndiResource(name);
		} catch (Exception e) {
			LOGGER.error("Unable to find resource " + name, e);
			return null;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("getResource() - exit " + resource);
			}
		}

		return resource;
	}

	public static DataSource getDataSource(String name) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getDataSource() - entry : " + name);
		}
		DataSource dataSource = null;
		try {
			dataSource = (DataSource) findJndiResource(name);
		} catch (Exception e) {
			LOGGER.error("Unable to find datasource " + name, e);
			return null;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("getDataSource() - exit " + dataSource);
			}
		}

		return dataSource;
	}

	public Set<String> getDataSourceNames() {
		return new HashSet<String>();
	}

	public static String getJndiFactory() {
		String factory = System.getProperty("com.isencia.naming.factory.initial");
		if (factory == null) {
			throw new IllegalStateException("Missing system property com.isencia.naming.factory.initial");
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Using jndi factory  : " + factory + " for lookup");
		}
		return factory;
	}

	public static Object findJndiResource(String name) throws Exception {
		LOGGER.info("Lookup resource  : " + name + " in jndi environment");
		String factory = getJndiFactory();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Using jndi factory  : " + factory + " for lookup");
		}
		Hashtable<String, String> env = new Hashtable<String, String>();
		// it's possible that the initial context was already created
		// during the startup
		// of the server, in this case it's necessary to reset the
		// environment
		// variables
		env.put("java.naming.factory.initial", factory);
		InitialContext initContext = new InitialContext(env);

		try {
			Object lookup = initContext.lookup(name);
			if (lookup != null) {
				LOGGER.debug("Resource lookup succeeded for name : " + name);
				return lookup;
			} else {
				LOGGER.info("Resource lookup failed for name : " + name);
				throw new Exception("Resource lookup failed for name : " + name);
			}
		} catch (NamingException ne) {
			LOGGER.warn("Resource lookup failed for name : " + name);
			return null;
		} catch (Exception e) {
			LOGGER.error("Resource lookup failed for name : " + name, e);
			return null;
		}

	}

}
