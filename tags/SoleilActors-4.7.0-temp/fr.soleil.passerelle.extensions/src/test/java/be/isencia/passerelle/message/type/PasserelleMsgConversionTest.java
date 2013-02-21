/*
 * (c) Copyright 2001-2005, iSencia Belgium NV All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV. Use is
 * subject to license terms.
 */
package be.isencia.passerelle.message.type;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.type.TypeConversionChain;

import fr.soleil.tango.clientapi.TangoAttribute;

public class PasserelleMsgConversionTest {

    PasserelleToken origToken;

    @BeforeClass
    protected void setUp() throws Exception {
        System.setProperty("TANGO_HOST", "localhost:20000");
        final TangoAttribute attr = new TangoAttribute("test/mouse/1/positionY");
        // attr.insert("5");
        final ManagedMessage msg = MessageFactory.getInstance().createMessage();
        msg.setBodyContent(attr, ManagedMessage.objectContentType);
        origToken = new PasserelleToken(msg);
    }

    /*
     * Test method for
     * 'be.isencia.passerelle.message.type.TypeConversionChain.convertPasserelleMessageContent(PasserelleToken,
     * Class)'
     */
    @Test(enabled = false)
    public void testConvertPasserelleMessageContent() throws UnsupportedOperationException,
            PasserelleException {

        final PasserelleToken result = TypeConversionChain.getInstance()
                .convertPasserelleMessageContent(origToken, Double.class);
        assertThat(result).isNotNull();
        assertThat(Double.class.isAssignableFrom(result.getMessageContentType())).isTrue();
        // assertEquals(new Double(5),
        // result.getMessage().getBodyContent());
        assertThat(result.getMessage().getID()).isEqualTo(origToken.getMessage().getID());
        assertThat(result.getMessage().getVersion()).isEqualTo(origToken.getMessage().getVersion());

    }

}
