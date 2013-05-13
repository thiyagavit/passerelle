package fr.soleil.passerelle.actor.tango.archiving;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.tango.utils.DevFailedUtils;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.command.MockCommand;

@PrepareForTest(TangoCommand.class)
public class HdbExtractorProxyTest extends PowerMockTestCase {

    @Test
    public void should_throw_devFailed_when_extractor_return_a_wrong_format_to_getLastScalarAttrValue()
            throws DevFailed {
        HdbExtractorProxy proxy = new HdbExtractorProxy(false);
        try {
            proxy.setGetNewestValueCommad(new TangoCommand(new MockCommand("GetNewestValue",
                    "tango/tangotes/titan/double_scalar", "invalid_String_Format")));
        }
        catch (DevFailed devFailed) {
            fail("can not init tango Command", devFailed);
        }

        try {
            proxy.getLastScalarAttrValue("tango/tangotest/titan", "double_scalar");
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(HdbExtractorProxy.WRONG_FORMAT);
        }
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void test_extractor_return_a_correct_format_to_getLastScalarAttrValue() throws DevFailed {
        String expectedValue = " -10.686";
        HdbExtractorProxy proxy = new HdbExtractorProxy(false);

        TangoCommand command = mock(TangoCommand.class);
        when(command.execute(eq(String.class), eq("tango/tangotest/titan/double_scalar")))
                .thenReturn("123;" + expectedValue);

        proxy.setGetNewestValueCommad(command);
        String value = proxy.getLastScalarAttrValue("tango/tangotest/titan", "double_scalar");

        assertThat(value).isEqualTo(expectedValue.trim());
    }
}
