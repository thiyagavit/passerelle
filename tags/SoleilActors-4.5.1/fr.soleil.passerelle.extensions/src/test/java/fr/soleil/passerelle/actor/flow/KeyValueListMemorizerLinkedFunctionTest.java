package fr.soleil.passerelle.actor.flow;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;

@RunWith(Parameterized.class)
public class KeyValueListMemorizerLinkedFunctionTest {

    private final FlowManager flowMgr;
    private final Flow topLevel;

    // parameters values
    private final Map<String, String> props;
    private final String[] expectedKeyList1;
    private final String[] exceptedKeyList2;
    private final String[] expectedValueList1;
    private final String[] exceptedValueList2;

    private final ArrayBlockingQueue<String> outputValueList1;
    private final ArrayBlockingQueue<String> outputValueList2;
    private final ArrayBlockingQueue<String> keyMessagesList1;
    private final ArrayBlockingQueue<String> valueMessagesList1;
    private final ArrayBlockingQueue<String> keyMessagesList2;
    private final ArrayBlockingQueue<String> valueMessagesList2;

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

    // private static final String[] expectedOutputValue1 = {
    // "Line-List1                : 7-List1",//
    // "Location-List1            : P1-B-01-List1",//
    // "Sample Information-List1  :",//
    // "Method Name-List1         : D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List1" };
    //
    // private static final String[] expectedOutputValue2 = {
    // "Line-List2                : 7-List2",//
    // "Location-List2            : P1-B-01-List2",//
    // "Sample Information-List2  :",//
    // "Method Name-List2         : D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" };

    @Parameters
    public static List<Object[]> getParametres() {

        return Arrays.asList(new Object[][] {
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
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" } },// values2

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
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" } }, // Value2
                {
                        // test filtered keys 1
                        "0",// position value 1
                        "0",
                        "1",// endLoop1
                        "3",
                        "Line-List1;Sample Information-List1",// filtered Key1
                        "",
                        new String[] { "Line-List1", "Sample Information-List1" },// expected key1

                        new String[] { "Line-List2", "Location-List2", "Sample Information-List2",
                                "Method Name-List2" },// expected key2

                        new String[] { "7-List1", "" },// values1

                        new String[] { "7-List2", "P1-B-01-List2", "",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" } }, // values2
                {
                        // test filtered keys 1 and Key2 with space between keys and separator
                        "0",// position value 1
                        "1",
                        "1",// endLoop1
                        "1",
                        ";Line-List1;\nSample Information-List1",// filtered Key1
                        " Location-List2 ;; Method Name-List2 ",
                        new String[] { "Line-List1", "Sample Information-List1" },// expected key1

                        new String[] { "Location-List2", "Method Name-List2" },// expected key2

                        new String[] { "7-List1", "" },// values1

                        new String[] { "P1-B-01-List2",
                                "D:\\AGILENT\\METHODS\\USERS\\AUTOSAMPLER_SEQUENCE-List2" } } // values2
                });

    }

    public KeyValueListMemorizerLinkedFunctionTest(final String gotoPosList1,
            final String gotoPosList2, final String endLoop1, final String endLoop2,
            final String filteredKeysList1, final String filteredKeysList2,
            final String[] exceptedKeyList1, final String[] exceptedKeyList2,
            final String[] exceptedValueList1, final String[] exceptedValueList2) throws Exception {

        this.expectedKeyList1 = exceptedKeyList1;
        this.exceptedKeyList2 = exceptedKeyList2;
        this.expectedValueList1 = exceptedValueList1;
        this.exceptedValueList2 = exceptedValueList2;

        // Load sequence
        final Reader in = new InputStreamReader(getClass().getResourceAsStream(
                "/fr/soleil/passerelle/resources/DualKeyValue.moml"));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);
        final BasicDirector dir = new BasicDirector(topLevel, "Dir");
        topLevel.setDirector(dir);

        // add listener to catch outputValue, key and value port message
        outputValueList1 = new ArrayBlockingQueue<String>(EXPECTED_OUTPUT_VALUE1.length, true);
        outputValueList2 = new ArrayBlockingQueue<String>(EXPECTED_OUTPUT_VALUE2.length, true);
        keyMessagesList1 = new ArrayBlockingQueue<String>(exceptedKeyList1.length, true);
        valueMessagesList1 = new ArrayBlockingQueue<String>(exceptedValueList1.length, true);
        keyMessagesList2 = new ArrayBlockingQueue<String>(exceptedKeyList2.length, true);
        valueMessagesList2 = new ArrayBlockingQueue<String>(exceptedValueList2.length, true);

        KeyValueListMemorizerSuiteCase.addOutputValueMessageListenerToActor(topLevel,
                "MemorizeList1", outputValueList1);
        KeyValueListMemorizerSuiteCase.addOutputValueMessageListenerToActor(topLevel,
                "MemorizeList2", outputValueList2);
        KeyValueListMemorizerSuiteCase.addKeyValueMessageListenerToActor(topLevel, "outputList1",
                keyMessagesList1, valueMessagesList1);
        KeyValueListMemorizerSuiteCase.addKeyValueMessageListenerToActor(topLevel, "outputList2",
                keyMessagesList2, valueMessagesList2);

        // Set parameters of actors
        props = new HashMap<String, String>();

        props.put("gotoList1." + KeyValueListMemorizer.POSITION_VALUE_LABEL, gotoPosList1);
        props.put("gotoList2." + KeyValueListMemorizer.POSITION_VALUE_LABEL, gotoPosList2);
        props.put("ForLoopList1.End Value", endLoop1);
        props.put("ForLoopList2.End Value", endLoop2);
        props.put("outputList1." + KeyValueListMemorizer.FILTERED_KEYS_LABEL, filteredKeysList1);
        props.put("outputList2." + KeyValueListMemorizer.FILTERED_KEYS_LABEL, filteredKeysList2);
    }

    @Test
    public void testKeysValuesAreCorrect() throws Exception {
        // run sequence
        flowMgr.executeBlockingErrorLocally(topLevel, props);

        assertKeyValueMessageAreCorrect(keyMessagesList1, expectedKeyList1, valueMessagesList1,
                expectedValueList1);

        assertKeyValueMessageAreCorrect(keyMessagesList2, exceptedKeyList2, valueMessagesList2,
                exceptedValueList2);

    }

    /**
     * check key and value port message are corrects
     * 
     * @param keys ArrayBlockingQueue which contains all message from key port
     * @param expectedKeys the expecting message of key port
     * @param values ArrayBlockingQueue which contains all message from value port
     * @param expectedValues the expecting message of value port
     * @throws InterruptedException if the extraction of an element of the list takes more than 1
     *             second
     */
    private void assertKeyValueMessageAreCorrect(final ArrayBlockingQueue<String> keys,
            final String[] expectedKeys, final ArrayBlockingQueue<String> values,
            final String[] expectedValues) throws InterruptedException {

        // check size
        assertEquals(" inavlid number of key messages", expectedKeys.length, keys.size());

        assertEquals(" inavlid number of value messages", values.size(), expectedValues.length);

        // check elements
        int i = 0;
        while (!keys.isEmpty()) {
            // assert key is correct
            assertEquals(expectedKeys[i], KeyValueListMemorizerSuiteCase.extractBodyContent(keys
                    .poll(1, TimeUnit.SECONDS)));

            // assert value is correct
            assertEquals(expectedValues[i++],
                    KeyValueListMemorizerSuiteCase.extractBodyContent(values.poll(1,
                            TimeUnit.SECONDS)));
        }
    }
}
