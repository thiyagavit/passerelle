package fr.soleil.passerelle.actor.flow;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;

import fr.soleil.passerelle.testUtils.Constants;
import fr.soleil.passerelle.testUtils.MomlRule;

public class DoWhileLoopTest {

    private static final String ACTOR_NAME = "DoWhileLoop";

    public MomlRule moml = new MomlRule(Constants.SEQUENCES_PATH + "DoWhileLoop.moml");

    @BeforeMethod
    public void setUp() throws Throwable {
        moml.before();
    }

    @AfterMethod
    public void clean() {
        moml.after();
    }

    // XXX comparison with an empty can not be tested easily because
    // ValueListGenerator can not generate empty value
    @DataProvider(name = "provider")
    public static Object[][] getParametres() {
        return new Object[][] { { "1.1,2.2,3.3,4.4,5.5", "!=", "4.4", 4 },// 0
                { "4.4 ,4.4, 4.4, 4.9, 4.4", "==", "4.4", 4 },// 1
                { "5.6, 7.9, 4.9, 4.4, 10", ">", "4.4", 4 },// 2
                { "5.6, 7.9, 4.4, 3.9, 4.9", ">=", "4.4", 4 },// 3
                { "1.1 ,2.2, 3.3, 4.4, 3.6", "<", "4.4", 4 },// 4
                { "1.1, 2.2, 4.4, 4.9,  3.8", "<=", "4.4", 4 },// 5

                // test String
                { "bbb,aaa,ddd,ccc,bbb", "!=", "ccc", 4 },// 6
                { "bbb,aaa,ddd,ccc,bbb", "!=", "cCc", 4 },// 7 test ignore case

                { "ccc,ccc,ccc,bbb,ccc", "==", "ccc", 4 },// 8
                { "ccc,ccc,ccc,bbb,ccc", "==", "cCc", 4 },// 9 test ignore case

                { "ddd,eee,fff,ccc,ddd", ">", "ccc", 4 },// 10
                { "ddd,eee,fff,ccc,ddd", ">", "cCc", 4 },// 11 test ignore case

                { "ddd,eee,ccc,bbb,eee", ">=", "ccc", 4 },// 12
                { "ddd,eee,ccc,bbb,eee", ">=", "cCc", 4 },// 13 test ignore case

                { "aaa,bbb,aaa,ccc,aaa", "<", "ccc", 4 },// 14
                { "aaa,bbb,aaa,ccc,aaa", "<", "cCc", 4 },// 15 test ignore case

                { "aaa,bbb,ccc,eee,aaa", "<=", "ccc", 4 },// 16
                { "aaa,bbb,ccc,eee,aaa", "<=", "cCc", 4 },// 17 test ignorecase

                // test String -- number
                { "aaa,bbb,ccc,1,aaa", "!=", "1.0", 4 },// 18
                { "aaa,bbb,ccc,1.2,aaa", "!=", "1.2", 4 },// 19 test decimal

                { "1,1,1,aaa,aaa", "==", "1.0", 4 },// 20
                { "1.2,1.2,1.2,aaa,aaa", "==", "1.2", 4 },// 21 test decimal

                { "aaa,bbb,ccc,1,aaa", ">", "1.0", 4 },// 22
                { "aaa,bbb,ccc,1.2,aaa", ">", "1.2", 4 },// 23 test decimal

                { "aaa,bbb,1,0,aaa", ">=", "1.0", 4 },// 24
                { "aaa,bbb,1.2,0,aaa", ">=", "1.2", 4 },// 25 test decimal

                { "--,+,...,1.0,aaa", "<", "1.0", 4 },// 26
                { "--,+,...,1.2,aaa", "<", "1.2", 4 },// 27 test decimal

                { "--,+,1.0,bbb,aaa", "<=", "1.0", 4 },// 28
                { "--,+,1.2,bbb,aaa", "<=", "1.2", 4 },// 29 test decimal
        };

    }

    // to avoid infinity loop
    @Test(timeOut = 3000, dataProvider = "provider")
    public void normalCase(final String valueList, final String comparison, final String rValue,
            final int itteration) throws Exception {

        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put(ACTOR_NAME + "." + DoWhileLoop.COMPARISON_PARAM_NAME, comparison);
        props.put(ACTOR_NAME + "." + DoWhileLoop.RIGTH_VALUE_PARAM_NAME, rValue);
        props.put("ValuesListGenerator.Values List (sep by commas)", valueList);
        props.put("generate_error.Values", "0");

        final ArrayBlockingQueue<String> continuingReceiver = new ArrayBlockingQueue<String>(
                itteration);

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, DoWhileLoop.CONTINUE_PORT_NAME, continuingReceiver);
        moml.addMessageReceiver(ACTOR_NAME, DoWhileLoop.OUTPUT_PORT_NAME, outputReceiver);

        moml.executeBlockingErrorLocally(props);

        // check number of messages
        assertThat(continuingReceiver).hasSize(itteration);
        assertThat(outputReceiver).hasSize(1);
    }

    // FIXME: the sequence is never stop and the exception is not forwarded to
    // moml.executeBlockingErrorLocally
    @Test(timeOut = 3000, enabled = false)
    public void should_stop_loop_and_sequence_when_exception_is_thrown() {
        final Map<String, String> props = new HashMap<String, String>();
        props.put("DirBasic.Mock Mode", "false");
        props.put("DoWhileLoop.comparison", "<=");
        props.put("DoWhileLoop.Rigth Value", "1.2");
        props.put("ValuesListGenerator.Values List (sep by commas)", "--,+,1.2,bbb,aaa");

        // throw exception thanks to errorGenerator
        props.put("generate_error.Values", "1");

        final ArrayBlockingQueue<String> continuingReceiver = new ArrayBlockingQueue<String>(4);

        final ArrayBlockingQueue<String> outputReceiver = new ArrayBlockingQueue<String>(1);
        moml.addMessageReceiver(ACTOR_NAME, DoWhileLoop.CONTINUE_PORT_NAME, continuingReceiver);
        moml.addMessageReceiver(ACTOR_NAME, DoWhileLoop.OUTPUT_PORT_NAME, outputReceiver);

        try {
            moml.executeBlockingErrorLocally(props);
            failBecauseExceptionWasNotThrown(PasserelleException.class);
        } catch (final FlowAlreadyExecutingException e) {
            fail("this expcetion should not be thown : " + e.getMessage());
        } catch (final PasserelleException e) {
            // TODO assert is the exception thrown by errorGenerator
        }
    }
}
