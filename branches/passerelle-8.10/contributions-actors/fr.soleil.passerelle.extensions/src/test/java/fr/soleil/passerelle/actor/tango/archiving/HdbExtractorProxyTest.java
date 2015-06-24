package fr.soleil.passerelle.actor.tango.archiving;

import static org.fest.assertions.api.Assertions.assertThat;
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

@PrepareForTest(TangoCommand.class)
public class HdbExtractorProxyTest extends PowerMockTestCase {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void should_throw_devFailed_when_extractor_return_a_wrong_format_to_getLastScalarAttrValue()
            throws DevFailed {
        HdbExtractorProxy proxy = new HdbExtractorProxy();
        String completeAttrName = "tango/tangotes/titan/double_scalar";

        TangoCommand command = mock(TangoCommand.class);
        when(command.execute(eq(String.class), eq(completeAttrName))).thenReturn("invaid format");

        proxy.setNewestValueCommad(command);

        try {
            proxy.getLastScalarAttrValue(completeAttrName);
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(HdbExtractorProxy.NEWEST_CMD);
        }
    }

    @Test
    public void test_extractorpProxy_return_a_correct_format_to_getLastScalarAttrValue()
            throws DevFailed {
        String completeAttributeName = "tango/tangotest/titan/double_scalar";
        String expectedValue = " -10.686";

        HdbExtractorProxy proxy = new HdbExtractorProxy();

        TangoCommand command = mock(TangoCommand.class);

        when(command.execute(eq(String.class), eq(completeAttributeName))).thenReturn(
                "123;" + expectedValue);

        proxy.setNewestValueCommad(command);
        String value = proxy.getLastScalarAttrValue(completeAttributeName);

        assertThat(value).isEqualTo(expectedValue.trim());
    }

    @Test
    public void test_extractorpProxy_return_a_correct_format_to_getNearestScalarAttrValue()
            throws DevFailed {
        String completeAttributeName = "test/motor/1-1/position";
        String date = "03-07-2013 16:39:00";
        String expectedValue = " -10.686";

        HdbExtractorProxy proxy = new HdbExtractorProxy();

        TangoCommand command = mock(TangoCommand.class);
        when(command.execute(eq(String.class), eq(completeAttributeName), eq(date))).thenReturn(
                "123;" + expectedValue);

        proxy.setNearestValueCommand(command);

        String value = proxy.getNearestScalarAttrValue(completeAttributeName, date);
        assertThat(value).isEqualTo(expectedValue.trim());
    }

    @Test
    public void should_throw_devFailed_when_extractor_return_a_wrong_format_to_getNearestScalarAttrValue()
            throws DevFailed {
        HdbExtractorProxy proxy = new HdbExtractorProxy();
        String completeAttrName = "tango/tangotes/titan/double_scalar";
        String date = "03-07-2013 16:39:00";

        TangoCommand mockedCommand = mock(TangoCommand.class);
        when(mockedCommand.execute(eq(String.class), eq(completeAttrName), eq(date))).thenReturn(
                "invalid format");

        proxy.setNearestValueCommand(mockedCommand);

        try {
            proxy.getNearestScalarAttrValue(completeAttrName, date);
            failBecauseExceptionWasNotThrown(DevFailed.class);
        }
        catch (DevFailed e) {
            assertThat(DevFailedUtils.toString(e)).contains(HdbExtractorProxy.NEAREST_CMD);
        }
    }
}
