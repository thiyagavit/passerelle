/*
 * (c) Copyright 2001-2005, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package be.isencia.passerelle.message.type;

import junit.framework.TestCase;
import be.isencia.passerelle.core.PasserelleToken;
import be.isencia.passerelle.message.ManagedMessage;
import be.isencia.passerelle.message.MessageFactory;
import fr.soleil.tango.clientapi.TangoAttribute;

public class PasserelleMsgConversionTest extends TestCase {

	PasserelleToken origToken;

	protected void setUp() throws Exception {
		System.setProperty("TANGO_HOST","localhost:20000");
		TangoAttribute attr = new TangoAttribute("test/mouse/1/positionY");
//		attr.insert("5");
		ManagedMessage msg = MessageFactory.getInstance().createMessage();
		msg.setBodyContent(attr,ManagedMessage.objectContentType);
		origToken = new PasserelleToken(msg);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'be.isencia.passerelle.message.type.TypeConversionChain.convertPasserelleMessageContent(PasserelleToken, Class)'
	 */
	public void testConvertPasserelleMessageContent() {
		try {
			PasserelleToken result = TypeConversionChain.getInstance().convertPasserelleMessageContent(origToken, Double.class);
			assertNotNull(result);
			assertTrue(Double.class.isAssignableFrom(result.getMessageContentType()));
//			assertEquals(new Double(5), result.getMessage().getBodyContent());
			assertEquals(origToken.getMessage().getID(), result.getMessage().getID());
			assertEquals(origToken.getMessage().getVersion(), result.getMessage().getVersion());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception "+e);
		}

	}

}
