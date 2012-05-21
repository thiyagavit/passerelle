package fr.soleil.passerelle.actor.flow;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.MomlRule;

public final class KeyValueListMemorizeStandAloneFunctionTest {

    private static final String CLEAR_LIST_REGEX = "FATAL - list Name :\"[^\"]+\" is unknown";
    private static final String TOO_MANY_ITT_REGEX = "FATAL - There are no more element in list name :\"[^\"]+\"";
    private static final String GOTO_REGEX = "FATAL - list: \"[^\"]+\" invalid position value: [0-9]+. The list have [0-9]+ element\\(s\\)";

    private static final String MEMORIZER_NAME1 = "MemorizeList1";

    @Rule
    public final MomlRule moml = new MomlRule("/fr/soleil/passerelle/resources/DualKeyValue.moml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalActionException.class)
    public void emptyNameListTest() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.topLevel
                .getEntity(MEMORIZER_NAME1);

        actor.listNameParam.setToken("");
        actor.attributeChanged(actor.listNameParam);
    }

    @Test(expected = IllegalActionException.class)
    public void shouldThrowExceptionWhenPositionValueIsNegative() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.topLevel
                .getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("-1");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenPositionValueIsEmpty() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.topLevel
                .getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expected = IllegalActionException.class)
    public void shouldThrowExceptionWhenPositionValueIsAString() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.topLevel
                .getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("azerty");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test
    public void shouldThrowExceptionWhenPositionValueIsTooBig() throws Exception {
        expectedException.expect(PasserelleException.class);
        expectedException.expectMessage(new RegexMathcher(GOTO_REGEX));

        final HashMap<String, String> props = new HashMap<String, String>();
        props.put("gotoList1." + KeyValueListMemorizer.POSITION_VALUE_LABEL, "100");
        // execute sequence
        moml.flowMgr.executeBlockingErrorLocally(moml.topLevel, props);

    }

    @Test
    public void tooManyItterationWithExceptionTest() throws Exception {
        expectedException.expect(PasserelleException.class);
        expectedException.expectMessage(new RegexMathcher(TOO_MANY_ITT_REGEX));

        final HashMap<String, String> props = new HashMap<String, String>();
        props.put("ForLoopList1.End Value", "100");
        // execute sequence
        moml.flowMgr.executeBlockingErrorLocally(moml.topLevel, props);

    }

    @Test
    public void tooManyItterationWithEmptyKeyTest() throws Exception {
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put("outputList1." + KeyValueListMemorizer.FIXED_NUMBER_OF_VALUE_LABEL, "false");
        props.put("ForLoopList1.End Value", "4");

        final ArrayBlockingQueue<String> keyMessagesList1 = new ArrayBlockingQueue<String>(5, true);
        final ArrayBlockingQueue<String> valueMessagesList1 = new ArrayBlockingQueue<String>(5,
                true);

        KeyValueListMemorizerSuiteCase.addKeyValueMessageListenerToActor(moml.topLevel,
                "outputList1", keyMessagesList1, valueMessagesList1);

        // execute sequence
        moml.flowMgr.executeBlockingErrorLocally(moml.topLevel, props);

        Assert.assertEquals("key message list has not the goog size", 5, keyMessagesList1.size());
        Assert.assertEquals("value message list has not the goog size", 5,
                valueMessagesList1.size());

        for (int i = 0; i < 4; i++) {
            keyMessagesList1.poll(1, TimeUnit.SECONDS);
            valueMessagesList1.poll(1, TimeUnit.SECONDS);
        }

        Assert.assertEquals("",
                KeyValueListMemorizerSuiteCase.extractBodyContent(keyMessagesList1.poll()));

        Assert.assertEquals("",
                KeyValueListMemorizerSuiteCase.extractBodyContent(valueMessagesList1.poll()));
    }

    /**
     * test if list is correctly clear.</br></br>
     * 
     * the map which contains list of keys/Values is private and we won't create a getter because is
     * only use for test.</br></br>
     * 
     * So we clear the list and try to access to next brace and check a PasserelleException is
     * thrown.
     * 
     * @throws Exception
     */
    @Test
    public void clearListTest() throws Exception {
        expectedException.expect(PasserelleException.class);
        expectedException.expectMessage(new RegexMathcher(CLEAR_LIST_REGEX));

        final HashMap<String, String> props = new HashMap<String, String>();
        // choose the branch which clear the list
        props.put("Choice1.value", "1");
        // execute sequence
        moml.flowMgr.executeBlockingErrorLocally(moml.topLevel, props);
    }

    public class RegexMathcher extends TypeSafeMatcher<String> {
        private final Pattern pattern;

        public RegexMathcher(final String regex) {
            pattern = Pattern.compile(regex);
        }

        @Override
        public void describeTo(final Description desc) {
            desc.appendText("matches regex");
        }

        @Override
        public boolean matchesSafely(final String item) {
            final Matcher m = pattern.matcher(item);

            // message is contained only once in stack trace
            return m.find() && !m.find();
        }
    }
}
