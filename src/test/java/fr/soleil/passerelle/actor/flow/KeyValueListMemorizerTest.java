package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class KeyValueListMemorizerTest {

    //private static final String CLEAR_LIST_REGEX = "FATAL - list Name :\"[^\"]+\" is unknown";
    private static final String CLEAR_LIST_REGEX = "list Name :\"[^\"]+\" is unknown";
    //private static final String TOO_MANY_ITT_REGEX = "FATAL - There are no more element in list name :\"[^\"]+\"";
    private static final String TOO_MANY_ITT_REGEX = "There are no more element in list name :\"[^\"]+\"";
    //private static final String GOTO_REGEX = "FATAL - list: \"[^\"]+\" invalid position value: [0-9]+. The list have [0-9]+ element\\(s\\)";
    private static final String GOTO_REGEX = "list: \"[^\"]+\" invalid position value: [0-9]+. The list have [0-9]+ element\\(s\\)";

    private static final String MEMORIZER_NAME1 = "MemorizeList1";

    private static final String[] EXPECTED_OUTPUT_VALUE1 = {
            "==============================================", "",//
            "Line-List1                : 7-List1",//
            "Location-List1            : P1-B-01-List1",//
            "Sample Information-List1  :",//
            ": 789-List1",//
            "Method Name-List1         : D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List1",//
            "",//
            "==============================================" };

    private static final String[] EXPECTED_OUTPUT_VALUE2 = {
            "==============================================", "",//
            "Line-List2                : 7-List2",//
            "Location-List2            : P1-B-01-List2",//
            "Sample Information-List2  :",//
            ": 789-List2",//
            "Method Name-List2         : D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2",//
            "",//
            "==============================================" };

    public final MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "DualKeyValue.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    @DataProvider(name = "provider")
    public static Object[][] getParametres() {

        return new Object[][] {
                {// normal case without goto and filtered keys
                        "0",// position value 1
                        "0",
                        "3",// endLoop1
                        "3",
                        "",// filtered Key1
                        "",
                        new String[] { "Line-List1", "Location-List1", "Sample Information-List1",
                                "Method Name-List1" },// expected key1

                        new String[] { "Line-List2", "Location-List2", "Sample Information-List2",
                                "Method Name-List2" },// expected key2

                        new String[] { "7-List1", "P1-B-01-List1", "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List1" },// values1

                        new String[] { "7-List2", "P1-B-01-List2", "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" }, // values2

                        new String[] { "0", "1", "2", "3" },// index1
                        new String[] { "0", "1", "2", "3" } },// index2

                {// test position value !=0
                        "0",// position value 1
                        "2",
                        "3",// endLoop1
                        "1",
                        "",// filtered Key1
                        "",
                        new String[] { "Line-List1", "Location-List1", "Sample Information-List1",
                                "Method Name-List1" }, // expected key1

                        new String[] { "Sample Information-List2", "Method Name-List2" },// expected
                        // key2

                        new String[] { "7-List1", "P1-B-01-List1", "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List1" },// Value1

                        new String[] { "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" }, // Value2
                        new String[] { "0", "1", "2", "3" },// index1
                        new String[] { "2", "3" } },// index2

                {
                        // test filtered keys 1
                        "0",// position value 1
                        "0",
                        "1",// endLoop1
                        "3",
                        "Line-List1;Sample Information-List1",// filtered Key1
                        "",
                        new String[] { "Line-List1", "Sample Information-List1" },// expected
                                                                                  // key1

                        new String[] { "Line-List2", "Location-List2", "Sample Information-List2",
                                "Method Name-List2" },// expected key2

                        new String[] { "7-List1", "" },// values1

                        new String[] { "7-List2", "P1-B-01-List2", "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" }, // values2

                        new String[] { "0", "2" },// index1
                        new String[] { "0", "1", "2", "3" } },// index2

                {
                        // test filtered keys 1 and Key2 with space between keys
                        // and separator
                        "0",// position value 1
                        "1",
                        "1",// endLoop1
                        "1",
                        ";Line-List1;\nSample Information-List1",// filtered
                                                                 // Key1
                        " Location-List2 ;; Method Name-List2 ",
                        new String[] { "Line-List1", "Sample Information-List1" },// expected
                                                                                  // key1

                        new String[] { "Location-List2", "Method Name-List2" },// expected
                                                                               // key2

                        new String[] { "7-List1", "" },// values1

                        new String[] { "P1-B-01-List2",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" }, // values2

                        new String[] { "0", "2" },// index1
                        new String[] { "1", "3" },// index2
                } };

    }

    @Test(dataProvider = "provider")
    public void testKeysValuesAreCorrect(final String gotoPosList1, final String gotoPosList2,
            final String endLoop1, final String endLoop2, final String filteredKeysList1,
            final String filteredKeysList2, final String[] exceptedKeyList1,
            final String[] exceptedKeyList2, final String[] exceptedValueList1,
            final String[] exceptedValueList2, final String[] expectedIndex1,
            final String[] expectedIndex2) throws Exception {

        // create list which will received messages sent by actors

        final ArrayBlockingQueue<String> keyMessagesList1 = new ArrayBlockingQueue<String>(
                exceptedKeyList1.length, true);
        final ArrayBlockingQueue<String> valueMessagesList1 = new ArrayBlockingQueue<String>(
                exceptedValueList1.length, true);
        final ArrayBlockingQueue<String> outputValueList1 = new ArrayBlockingQueue<String>(
                EXPECTED_OUTPUT_VALUE1.length, true);
        final ArrayBlockingQueue<String> currentOutputIndexMessagesList1 = new ArrayBlockingQueue<String>(
                expectedIndex1.length, true);

        final ArrayBlockingQueue<String> keyMessagesList2 = new ArrayBlockingQueue<String>(
                exceptedKeyList2.length, true);
        final ArrayBlockingQueue<String> valueMessagesList2 = new ArrayBlockingQueue<String>(
                exceptedValueList2.length, true);
        final ArrayBlockingQueue<String> outputValueList2 = new ArrayBlockingQueue<String>(
                EXPECTED_OUTPUT_VALUE2.length, true);
        final ArrayBlockingQueue<String> currentOutputIndexMessagesList2 = new ArrayBlockingQueue<String>(
                expectedIndex2.length, true);

        // Set parameters of actors
        final Map<String, String> props = new HashMap<String, String>();
        props.put("gotoList1." + KeyValueListMemorizer.POSITION_VALUE_LABEL, gotoPosList1);
        props.put("gotoList2." + KeyValueListMemorizer.POSITION_VALUE_LABEL, gotoPosList2);
        props.put("ForLoopList1.End Value", endLoop1);
        props.put("ForLoopList2.End Value", endLoop2);
        props.put("outputList1." + KeyValueListMemorizer.FILTERED_KEYS_LABEL, filteredKeysList1);
        props.put("outputList2." + KeyValueListMemorizer.FILTERED_KEYS_LABEL, filteredKeysList2);

        // add message intercepter
        moml.addMessageReceiver("outputList1", "key", keyMessagesList1);
        moml.addMessageReceiver("outputList1", "value", valueMessagesList1);
        moml.addMessageReceiver("MemorizeList1", "output", outputValueList1);
        moml.addMessageReceiver("outputList1", "current index", currentOutputIndexMessagesList1);

        moml.addMessageReceiver("outputList2", "key", keyMessagesList2);
        moml.addMessageReceiver("outputList2", "value", valueMessagesList2);
        moml.addMessageReceiver("MemorizeList2", "output", outputValueList2);
        moml.addMessageReceiver("outputList2", "current index", currentOutputIndexMessagesList2);

        // run sequence
        moml.executeBlockingErrorLocally(props);

        assertSameContent(outputValueList1, EXPECTED_OUTPUT_VALUE1);
        assertSameContent(outputValueList2, EXPECTED_OUTPUT_VALUE2);

        // check value are correct
        assertSameContent(keyMessagesList1, exceptedKeyList1);
        assertSameContent(valueMessagesList1, exceptedValueList1);
        assertSameContent(currentOutputIndexMessagesList1, expectedIndex1);

        assertSameContent(keyMessagesList2, exceptedKeyList2);
        assertSameContent(valueMessagesList2, exceptedValueList2);
        assertSameContent(currentOutputIndexMessagesList2, expectedIndex2);

    }

    /**
     * check the "actual" ArrayBlockingQueue contains the same value as
     * "expected" array in the same order
     * 
     * @param actual
     *            ArrayBlockingQueue which contains all message from a port
     * 
     * @param expected
     *            String which contains all expected messages from a port
     * 
     * @throws InterruptedException
     *             if the extraction of an element of the list takes more than 1
     *             second
     */
    private void assertSameContent(final ArrayBlockingQueue<String> actual, final String[] expected)
            throws InterruptedException {

        // check size
        assertThat(actual).hasSize(expected.length);

        // check elements
        int i = 0;
        while (!actual.isEmpty()) {
            assertThat(actual.poll(1, TimeUnit.SECONDS)).isEqualTo(expected[i++]);
        }
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void emptyNameListTest() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.listNameParam.setToken("");
        actor.attributeChanged(actor.listNameParam);
    }

    @Test(expectedExceptions = IllegalActionException.class)
    public void shouldThrowExceptionWhenPositionValueIsNegative() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("-1");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenPositionValueIsEmpty() throws IllegalActionException {
        final KeyValueListMemorizer actor = (KeyValueListMemorizer) moml.getEntity(MEMORIZER_NAME1);

        actor.positionValueParam.setToken("");
        actor.attributeChanged(actor.positionValueParam);
    }

    @Test(expectedExceptions = IllegalActionException.class)
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

            failBecauseExceptionWasNotThrown(PasserelleException.class);
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
            failBecauseExceptionWasNotThrown(PasserelleException.class);
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
        final ArrayBlockingQueue<String> indexMessagesList1 = new ArrayBlockingQueue<String>(5,
                true);

        moml.addMessageReceiver("outputList1", "key", keyMessagesList1);
        moml.addMessageReceiver("outputList1", "value", valueMessagesList1);
        moml.addMessageReceiver("outputList1", "current index", indexMessagesList1);

        // execute sequence
        moml.executeBlockingErrorLocally(props);

        assertThat(keyMessagesList1).hasSize(5);
        assertThat(valueMessagesList1).hasSize(5);
        assertThat(indexMessagesList1).hasSize(5);

        // we want to check last element is empty so we add to remove the
        // previous elements
        for (int i = 0; i < 4; i++) {
            keyMessagesList1.poll(1, TimeUnit.SECONDS);
            valueMessagesList1.poll(1, TimeUnit.SECONDS);
            indexMessagesList1.poll(1, TimeUnit.SECONDS);
        }

        assertThat(keyMessagesList1.poll()).isEmpty();
        assertThat(valueMessagesList1.poll()).isEmpty();
        assertThat(indexMessagesList1.poll()).isEmpty();
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
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        } catch (final PasserelleException e) {
            System.out.println("==>>>>>>>>>>>>>>>>clearListTest : " + e.getMessage());
            final Pattern pattern = Pattern.compile(CLEAR_LIST_REGEX);
            assertThat(pattern.matcher(e.getMessage()).find()).isTrue();
        }
    }
}
