package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.MomlRule;

public final class KeyValueListMemorizeStandAloneFunctionTest {

    private static final String CLEAR_LIST_REGEX = "FATAL - list Name :\"[^\"]+\" is unknown";
    private static final String TOO_MANY_ITT_REGEX = "FATAL - There are no more element in list name :\"[^\"]+\"";
    private static final String GOTO_REGEX = "FATAL - list: \"[^\"]+\" invalid position value: [0-9]+. The list have [0-9]+ element\\(s\\)";

    private static final String MEMORIZER_NAME1 = "MemorizeList1";

    @Rule
    public final MomlRule moml = new MomlRule("/sequences/DualKeyValue.moml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalActionException.class)
    public void emptyNameListTest() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.listNameParam.setToken("");
        actor.attributeChanged(actor.listNameParam);
    }

    @Test(expected = IllegalActionException.class)
    public void shouldThrowExceptionWhenPositionValueIsNegative() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("-1");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenPositionValueIsEmpty() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expected = IllegalActionException.class)
    public void shouldThrowExceptionWhenPositionValueIsAString() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("azerty");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test
    public void shouldThrowExceptionWhenPositionValueIsTooBig() {
        try {
            final HashMap<String, String> props = new HashMap<String, String>();
            props.put("gotoList1." + KeyValueListMemorizer.POSITION_VALUE_LABEL, "100");
            // execute sequence
            moml.executeBlockingErrorLocally(props);

            fail("exception was not thrown");
        } catch (final PasserelleException e) {
            // we dont do assertThat(e.getMessage()).matches(GOTO_REGEX) because
            // the error message contains others informations(par of the stack
            // trace) on several line
            final Pattern pattern = Pattern.compile(GOTO_REGEX);
            assertThat(pattern.matcher(e.getMessage()).find()).isTrue();
        }
    }

    @Test
    public void tooManyItterationWithExceptionTest() {
        try {
            final HashMap<String, String> props = new HashMap<String, String>();
            props.put("ForLoopList1.End Value", "100");
            // execute sequence
            moml.executeBlockingErrorLocally(props);
            fail("expection should be thrown if the number of itteretion is too big");
        } catch (final PasserelleException e) {
            final Pattern pattern = Pattern.compile(TOO_MANY_ITT_REGEX);
            assertThat(pattern.matcher(e.getMessage()).find()).isTrue();
        }
    }

    @Test
    public void tooManyItterationWithEmptyKeyTest() throws Exception {
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put("outputList1." + KeyValueListMemorizer.FIXED_NUMBER_OF_VALUE_LABEL, "false");
        props.put("ForLoopList1.End Value", "4");

        final ArrayBlockingQueue<String> keyMessagesList1 = new ArrayBlockingQueue<String>(5, true);
        final ArrayBlockingQueue<String> valueMessagesList1 = new ArrayBlockingQueue<String>(5,
                true);

        moml.addMessageReceiver("outputList1", "key", keyMessagesList1);
        moml.addMessageReceiver("outputList1", "value", valueMessagesList1);

        // execute sequence
        moml.executeBlockingErrorLocally(props);

        Assert.assertEquals("key message list has not the goog size", 5, keyMessagesList1.size());
        Assert.assertEquals("value message list has not the goog size", 5,
                valueMessagesList1.size());

        for (int i = 0; i < 4; i++) {
            keyMessagesList1.poll(1, TimeUnit.SECONDS);
            valueMessagesList1.poll(1, TimeUnit.SECONDS);
        }

        assertThat(keyMessagesList1.poll()).isEmpty();
        assertThat(valueMessagesList1.poll()).isEmpty();
    }

    /**
     * test if list is correctly clear.</br></br>
     * 
     * the map which contains list of keys/Values is private and we won't create
     * a getter because is only use for test.</br></br>
     * 
     * So we clear the list and try to access to next brace and check a
     * PasserelleException is thrown.
     * 
     * @throws Exception
     */
    @Test
    public void clearListTest() {
        try {
            final HashMap<String, String> props = new HashMap<String, String>();
            // choose the branch which clear the list
            props.put("Choice1.value", "1");
            // execute sequence
            moml.executeBlockingErrorLocally(props);
            fail("cleanning list does not works");
        } catch (final PasserelleException e) {
            final Pattern pattern = Pattern.compile(CLEAR_LIST_REGEX);
            assertThat(pattern.matcher(e.getMessage()).find()).isTrue();
        }
    }

}
