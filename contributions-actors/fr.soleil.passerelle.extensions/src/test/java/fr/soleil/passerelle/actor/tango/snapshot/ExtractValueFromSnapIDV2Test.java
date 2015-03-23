package fr.soleil.passerelle.actor.tango.snapshot;

import static fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT;
import static fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.READ_PORT_LABEL;
import static fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT;
import static fr.soleil.passerelle.actor.tango.snapshot.ExtractValueFromSnapIDV2.WRITE_PORT_LABEL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.tango.utils.DevFailedUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.testUtils.MessageListener;
import fr.soleil.passerelle.util.PasserelleUtil;

public class ExtractValueFromSnapIDV2Test {

    private Flow flow;
    private FlowManager flowMgr;
    private ExtractValueFromSnapIDV2 actor;

    @BeforeMethod
    public void setUp() throws IllegalActionException, NameDuplicationException {
        flow = new Flow("unit test", null);
        flow.setDirector(new Director(flow, "director"));
        actor = new ExtractValueFromSnapIDV2(flow, "ExtractValueFromSnapIDV2");
        flowMgr = new FlowManager();
    }

    @Test(expectedExceptions = IllegalActionException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + ExtractValueFromSnapIDV2.ERROR_ATTR_NAME_PARAM_EMPTY + ".*")
    public void when_attributeName_param_is_empty_then_attributeChanged_raises_an_exception()
            throws IllegalActionException {

        actor.attributeNameParam.setToken("");
        actor.attributeChanged(actor.attributeNameParam);
    }

    @Test(expectedExceptions = PasserelleException.class, expectedExceptionsMessageRegExp = "(?s).*"
            + ExtractValueFromSnapIDV2.ERROR_ATTR_NAME_PARAM_EMPTY + ".*")
    public void when_attributeName_parameter_is_empty_then_validateInitialization_raises_an_exception()
            throws PasserelleException, NameDuplicationException, IllegalActionException {

        SnapExtractorProxy dummyProxy = mock(SnapExtractorProxy.class);
        actor.setGetSnapExtractor(dummyProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL, "");

        actor.extractionTypeParam.setToken(ExtractionType.READ.getName());

        flowMgr.executeBlockingErrorLocally(flow, props);
    }

    @Test(expectedExceptions = IllegalActionException.class, expectedExceptionsMessageRegExp = "(?s).*Unknown extraction description: \"foo bar\".*")
    public void when_extrationType_parameter_is_unknown_then_throw_exception()
            throws IllegalActionException {

        actor.extractionTypeParam.setToken("foo bar");
        actor.attributeChanged(actor.extractionTypeParam);

    }

    @Test
    public void should_have_readPort_by_default() throws IllegalActionException {
        // pre-condition, we check the actor is well configured by default
        assertThat(
                ExtractionType.fromDescription(PasserelleUtil
                        .getParameterValue(actor.extractionTypeParam))).isEqualTo(
                ExtractionType.READ);

        assertThat(actor.outputPorts[ExtractValueFromSnapIDV2.READ_PORT].getContainer()).isSameAs(
                actor);
        assertThat(actor.outputPorts[ExtractValueFromSnapIDV2.READ_PORT].getName()).isEqualTo(
                READ_PORT_LABEL);
    }

    @Test
    public void when_extrationType_parameter_change_to_write_then_readPort_isDeleted_and_writePort_created()
            throws IllegalActionException {
        // Given an actor with extraction type to read (we check the actor is well configured by
        // default)
        assertThat(
                ExtractionType.fromDescription(PasserelleUtil
                        .getParameterValue(actor.extractionTypeParam))).isEqualTo(
                ExtractionType.READ);

        // When we change the extraction type to write
        actor.extractionTypeParam.setToken(ExtractionType.WRITE.getName());

        // then read port is removed from the actor and write port is added
        assertThat(actor.outputPorts[READ_PORT].getContainer()).isNull();
        assertThat(actor.outputPorts[WRITE_PORT].getContainer()).isSameAs(actor);
        assertThat(actor.outputPorts[WRITE_PORT].getName()).isEqualTo(WRITE_PORT_LABEL);
    }

    @Test
    public void when_extrationType_parameter_change_to_readAndWrite_then_readPort_isDeleted_and_writePort_created()
            throws IllegalActionException {
        // Given an actor with extraction type to read (we check the actor is well configured by
        // default)
        assertThat(
                ExtractionType.fromDescription(PasserelleUtil
                        .getParameterValue(actor.extractionTypeParam))).isEqualTo(
                ExtractionType.READ);

        // When we change the extraction type to read_write
        actor.extractionTypeParam.setToken(ExtractionType.READ_WRITE.getName());

        // then read port and write port are added
        assertThat(actor.outputPorts[READ_PORT].getContainer()).isSameAs(actor);
        assertThat(actor.outputPorts[READ_PORT].getName()).isEqualTo(READ_PORT_LABEL);
        assertThat(actor.outputPorts[WRITE_PORT].getContainer()).isSameAs(actor);
        assertThat(actor.outputPorts[WRITE_PORT].getName()).isEqualTo(WRITE_PORT_LABEL);
    }

    @Test
    public void test_extract_read_value() throws DevFailed, IllegalActionException,
            NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
        String[] expectedValue = new String[] { "123.6" };
        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getReadValues(eq("12"), eq("domain/family/member/attr"))).thenReturn(
                expectedValue);

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.EXTRACTION_TYPE_LABEL,
                ExtractionType.READ.getName());

        // FIXME uncomment
        // actor.extractionTypeParam.setToken(ExtractionType.READ.getName());

        ArrayBlockingQueue<String> receiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[READ_PORT].addDebugListener(new MessageListener(receiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(receiver.poll()).isEqualTo(expectedValue[0]);
    }

    @Test
    public void test_extract_write_value() throws DevFailed, IllegalActionException,
            NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
        String[] expectedValue = new String[] { "123.6" };
        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getWriteValues(eq("12"), eq("domain/family/member/attr"))).thenReturn(
                expectedValue);

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        actor.extractionTypeParam.setToken(ExtractionType.WRITE.getName());

        ArrayBlockingQueue<String> receiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[WRITE_PORT].addDebugListener(new MessageListener(receiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(receiver.poll()).isEqualTo(expectedValue[0]);
    }

    @Test
    public void test_extract_read_write_value() throws DevFailed, IllegalActionException,
            NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
        String[] expectedValue = new String[] { "123.6", "50.2" };
        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getSnapValue(eq("12"), eq("domain/family/member/attr"))).thenReturn(
                expectedValue);

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        actor.extractionTypeParam.setToken(ExtractionType.READ_WRITE.getName());

        ArrayBlockingQueue<String> writeReceiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[WRITE_PORT].addDebugListener(new MessageListener(writeReceiver));
        ArrayBlockingQueue<String> readReceiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[READ_PORT].addDebugListener(new MessageListener(readReceiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(readReceiver.poll()).isEqualTo(expectedValue[0]);
        assertThat(writeReceiver.poll()).isEqualTo(expectedValue[1]);
    }

    // TODO TEST ERROR MSG
    @Test(expectedExceptions = ProcessingException.class)
    public void should_throw_exception_when_read_extract_failed_and_throwException_param_is_true()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getReadValues(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        actor.extractionTypeParam.setToken(ExtractionType.READ.getName());

        flowMgr.executeBlockingErrorLocally(flow, props);
    }

    @Test()
    public void should_send_enmpty_msg_when_read_extract_failed_and_throwException_param_is_false()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getReadValues(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");
        props.put("ExtractValueFromSnapIDV2."
                + ExtractValueFromSnapIDV2.THROW_EXCEPTION_ON_ERROR_LABEL, "false");

        actor.extractionTypeParam.setToken(ExtractionType.READ.getName());

        ArrayBlockingQueue<String> receiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[READ_PORT].addDebugListener(new MessageListener(receiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(receiver.poll()).isEmpty();
    }

    @Test()
    public void should_send_enmpty_msg_when_write_extract_failed_and_throwException_param_is_false()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getWriteValues(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");
        props.put("ExtractValueFromSnapIDV2."
                + ExtractValueFromSnapIDV2.THROW_EXCEPTION_ON_ERROR_LABEL, "false");

        actor.extractionTypeParam.setToken(ExtractionType.WRITE.getName());

        ArrayBlockingQueue<String> receiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[WRITE_PORT].addDebugListener(new MessageListener(receiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(receiver.poll()).isEmpty();
    }

    @Test()
    public void should_send_enmpty_msg_when_readWrite_extract_failed_and_throwException_param_is_false()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getSnapValue(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");
        props.put("ExtractValueFromSnapIDV2."
                + ExtractValueFromSnapIDV2.THROW_EXCEPTION_ON_ERROR_LABEL, "false");

        actor.extractionTypeParam.setToken(ExtractionType.READ_WRITE.getName());

        ArrayBlockingQueue<String> writeReceiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[WRITE_PORT].addDebugListener(new MessageListener(writeReceiver));
        ArrayBlockingQueue<String> readReceiver = new ArrayBlockingQueue<String>(1);
        actor.outputPorts[READ_PORT].addDebugListener(new MessageListener(readReceiver));

        flowMgr.executeBlockingErrorLocally(flow, props);
        assertThat(readReceiver.poll()).isEmpty();
        assertThat(writeReceiver.poll()).isEmpty();
    }

    // TODO TEST ERROR MSG
    @Test(expectedExceptions = ProcessingException.class)
    public void should_throw_exception_when_write_extract_failed_and_throwException_param_is_true()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getWriteValues(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        actor.extractionTypeParam.setToken(ExtractionType.WRITE.getName());

        flowMgr.executeBlockingErrorLocally(flow, props);
    }

    // TODO TEST ERROR MSG
    @Test(expectedExceptions = ProcessingException.class)
    public void should_throw_exception_when_read_write_extract_failed_and_throwException_param_is_true()
            throws DevFailed, NameDuplicationException, IllegalActionException,
            FlowAlreadyExecutingException, PasserelleException {

        SnapExtractorProxy mockedProxy = mock(SnapExtractorProxy.class);
        when(mockedProxy.getSnapValue(eq("12"), eq("domain/family/member/attr"))).thenThrow(
                DevFailedUtils.newDevFailed("ERROR"));

        actor.setGetSnapExtractor(mockedProxy);

        Const constant = new Const(flow, "Constant");
        flow.connect(constant.output, actor.inputPort);

        Map<String, String> props = new HashMap<String, String>();
        props.put("Constant.value", "12");
        props.put("ExtractValueFromSnapIDV2." + ExtractValueFromSnapIDV2.ATTRIBUTE_NAME_LABEL,
                "domain/family/member/attr");

        actor.extractionTypeParam.setToken(ExtractionType.READ_WRITE.getName());

        flowMgr.executeBlockingErrorLocally(flow, props);
    }

}
