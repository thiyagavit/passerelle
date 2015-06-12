package fr.soleil.passerelle.actor.tango.archiving;

import static fr.soleil.passerelle.actor.tango.ATangoActorV5.OUTPUT_PORT_NAME;
import static fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.COMPLETE_ATTR_NAME;
import static fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.DATE_PARAM_NAME;
import static fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.EXTRACTION_TYPE;
import static fr.soleil.passerelle.actor.tango.archiving.ExtractValueFromHDB.THROW_EXCEPTION_ON_ERROR;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.tango.utils.DevFailedUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class ExtractValueFromHDBTest {

    private static final String ACTOR_NAME = "ExtractValueFromHDB";
    private static final String EXTRACTION_TYPE_PARAM = ACTOR_NAME + "." + EXTRACTION_TYPE;
    private static final String COMPLETE_ATTR_NAME_PARAM = ACTOR_NAME + "." + COMPLETE_ATTR_NAME;
    private static final String THROW_EXCEPTION_ON_ERROR_PARAM = ACTOR_NAME + "."
            + THROW_EXCEPTION_ON_ERROR;

    private static final String DATE_PARAM = ACTOR_NAME + "." + DATE_PARAM_NAME;

    private final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH
            + "ExtractValueFromHDB.moml");
    private ExtractValueFromHDB actor;

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
        actor = (ExtractValueFromHDB) moml.getEntity(ACTOR_NAME);
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @Test(expectedExceptions = IllegalActionException.class, expectedExceptionsMessageRegExp = ExtractValueFromHDB.ERROR_COMPLETE_ATTR_NAME_IS_EMPTY)
    public void when_completeAttrName_parameter_is_empty_then_attributeChanged_throws_exception()
            throws IllegalActionException {

        // here we just need a dummy proxy
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);
        actor.setExtractorProxy(mockedProxy);

        actor.completeAttributeNameParam.setToken("");
        actor.attributeChanged(actor.completeAttributeNameParam);
    }

    @Test(expectedExceptions = IllegalActionException.class, expectedExceptionsMessageRegExp = "(?s).*Unknown extraction description: \"foo bar\".*")
    public void when_extrationType_parameter_is_not_valid_then_attributeChanged_throws_exception()
            throws IllegalActionException {
        // here we just need a dummy proxy
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);
        actor.setExtractorProxy(mockedProxy);

        actor.extractionTypeParam.setToken("foo bar");
        actor.attributeChanged(actor.extractionTypeParam);
    }

    @Test(expectedExceptions = PasserelleException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + ExtractValueFromHDB.ERROR_COMPLETE_ATTR_NAME_IS_EMPTY + ".*")
    public void when_completeAttrName_parameter_is_empty_then_validateInitialization_throws_expcetion()
            throws FlowAlreadyExecutingException, PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        actor.setExtractorProxy(mockedProxy);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "");
        props.put(EXTRACTION_TYPE_PARAM, ExtractValueFromHDB.ExtractionType.LASTED.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");

        moml.executeBlockingErrorLocally(props);
    }

    @Test(expectedExceptions = PasserelleException.class, expectedExceptionsMessageRegExp = "(?s).*Unknown extraction description: \"foo bar\".*")
    public void when_extrationType_parameter_is_not_valid_then_validateInitialization_throws_exception()
            throws PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        actor.setExtractorProxy(mockedProxy);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "domain/family/member");
        props.put(EXTRACTION_TYPE_PARAM, "foo bar");
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");

        moml.executeBlockingErrorLocally(props);
    }

    @Test
    public void testExtractLastValue() throws DevFailed, FlowAlreadyExecutingException,
            PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);
        String completeAttrName = "domain/family/member/attr";
        String expectedValue = "123.5";

        when(mockedProxy.getLastScalarAttrValue(eq(completeAttrName))).thenReturn(expectedValue);

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, completeAttrName);
        props.put(EXTRACTION_TYPE_PARAM, ExtractValueFromHDB.ExtractionType.LASTED.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");

        moml.executeBlockingErrorLocally(props);

        assertThat(outportMsg.poll()).isEqualTo(expectedValue);
    }

    @Test(expectedExceptions = ProcessingException.class, expectedExceptionsMessageRegExp = "(?s).*domain/family/member/attr is not in Hdb or can not be read: .*")
    public void when_attr_is_not_in_db_and_throwExceptionOnError_param_is_true_then_lastValue_throws_Exception()
            throws FlowAlreadyExecutingException, PasserelleException, DevFailed {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        when(mockedProxy.getLastScalarAttrValue(eq("domain/family/member/attr")))
                .thenThrow(
                        DevFailedUtils
                                .newDevFailed("domain/family/member/attr is not in Hdb or can not be read: "));

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "domain/family/member/attr");
        props.put(EXTRACTION_TYPE_PARAM, ExtractValueFromHDB.ExtractionType.LASTED.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");

        moml.executeBlockingErrorLocally(props);
    }

    @Test
    public void when_attr_is_not_in_db_and_throwExceptionOnError_param_is_false_then_lastValue_return_empty_msg()
            throws DevFailed, FlowAlreadyExecutingException, PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        when(mockedProxy.getLastScalarAttrValue(eq("domain/family/member/attr")))
                .thenThrow(
                        DevFailedUtils
                                .newDevFailed("domain/family/member/attr is not in Hdb or can not be read: "));

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "domain/family/member/attr");
        props.put(EXTRACTION_TYPE_PARAM, ExtractValueFromHDB.ExtractionType.LASTED.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "false");

        moml.executeBlockingErrorLocally(props);

        assertThat(outportMsg).hasSize(1);
        assertThat(outportMsg.poll()).isEmpty();
    }

    @Test(expectedExceptions = ProcessingException.class, expectedExceptionsMessageRegExp = "(?s).*domain/family/member/attr is not in Hdb or can not be read: .*")
    public void when_attr_is_not_in_db_and_throwExceptionOnError_param_is_true_then_nearestValue_throws_Exception()
            throws FlowAlreadyExecutingException, PasserelleException, DevFailed {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        Date date = new Date();
        String dateParam = HdbExtractorProxy.DATE_FORMAT.format(date);
        String dateProxy = HdbExtractorProxy.DATE_FORMAT.format(date);

        when(mockedProxy.getNearestScalarAttrValue(eq("domain/family/member/attr"), eq(dateProxy)))
                .thenThrow(
                        DevFailedUtils
                                .newDevFailed("domain/family/member/attr is not in Hdb or can not be read: "));

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "domain/family/member/attr");
        props.put(EXTRACTION_TYPE_PARAM,
                ExtractValueFromHDB.ExtractionType.NEAREST.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");
        props.put(DATE_PARAM, dateParam);

        moml.executeBlockingErrorLocally(props);
    }

    @Test
    public void when_attr_is_not_in_db_and_throwExceptionOnError_param_is_false_then_nearestValue_return_empty_msg()
            throws DevFailed, FlowAlreadyExecutingException, PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);

        Date date = new Date();
        String dateParam = HdbExtractorProxy.DATE_FORMAT.format(date);
        String dateProxy = HdbExtractorProxy.DATE_FORMAT.format(date);

        when(mockedProxy.getNearestScalarAttrValue(eq("domain/family/member/attr"), eq(dateProxy)))
                .thenThrow(
                        DevFailedUtils
                                .newDevFailed("domain/family/member/attr is not in Hdb or can not be read: "));

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, "domain/family/member/attr");
        props.put(EXTRACTION_TYPE_PARAM,
                ExtractValueFromHDB.ExtractionType.NEAREST.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "false");
        props.put(DATE_PARAM, dateParam);

        moml.executeBlockingErrorLocally(props);

        assertThat(outportMsg).hasSize(1);
        assertThat(outportMsg.poll()).isEmpty();
    }

    @Test
    public void testExtractNearestValue() throws DevFailed, FlowAlreadyExecutingException,
            PasserelleException {
        HdbExtractorProxy mockedProxy = mock(HdbExtractorProxy.class);
        String completeAttrName = "domain/family/member/attr";
        String expectedValue = "123.5";

        when(mockedProxy.getLastScalarAttrValue(eq(completeAttrName))).thenReturn(expectedValue);

        actor.setExtractorProxy(mockedProxy);

        ArrayBlockingQueue<String> outportMsg = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, OUTPUT_PORT_NAME, outportMsg);

        Map<String, String> props = new HashMap<String, String>();
        props.put(COMPLETE_ATTR_NAME_PARAM, completeAttrName);
        props.put(EXTRACTION_TYPE_PARAM, ExtractValueFromHDB.ExtractionType.LASTED.getDescription());
        props.put(THROW_EXCEPTION_ON_ERROR_PARAM, "true");

        moml.executeBlockingErrorLocally(props);

        assertThat(outportMsg.poll()).isEqualTo(expectedValue);
    }
}
