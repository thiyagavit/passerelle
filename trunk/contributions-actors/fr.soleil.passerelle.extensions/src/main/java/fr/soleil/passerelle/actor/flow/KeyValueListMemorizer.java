package fr.soleil.passerelle.actor.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.Actor;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.core.PasserelleException.Severity;

import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.domain.BasicDirector;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.passerelle.util.ProcessingExceptionWithLog;

/**
 * Allow to store/restore a list of brace Key/Value.</br> </br>
 * 
 * This Actor provide 4 actions:
 * <ul>
 * <li>store brace Key/Value</li>
 * <li>restore the next brace Key/Value</li>
 * <li>clear a list</li>
 * <li>goto position (define the next brace Key/Value to restore)</li>
 * </ul>
 * 
 * Each actions is triggered by an activation of inputPort. Moreover for each actions you have to
 * fill in the list name parameter which indicate on which list you work.
 * 
 * <p>
 * <b>Store Action:</b> store a brace Key/Value in the list .If the list does not exist, it is
 * automatically created. The actor get a String from his inputPort. This String is cut in two part(
 * Key and Value) thanks to a separator defined thought the separator Parameter.</br> If the
 * separator appear many times then only the first is used to cut the String, others are integrated
 * in value.</br> eg: separator = ":", stringToCut =
 * "just:a:test) => key= "just", value= "a:test").</br></br>
 * 
 * The key and value are trimed.
 * </p>
 * 
 * <p>
 * <b>Restore Action:</b> send the next brace Key/Value of the list on key and value ports.List does
 * not exist then a ProcessingException is raised and sequence is stopped. if there nor more in list
 * and fixedNumberOfValueParam is:
 * <ul>
 * <li>True: a ProcessingException is raised and sequence is stopped.</li>
 * <li>False: a empty brace Key/Value is send on Key/Value Port</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Clear Action:</b> clear the list. If the list does not exist then a ProcessingException is
 * raised and sequence is stopped. Send an empty message on outputPort at the end of action.
 * </p>
 * 
 * <p>
 * <b>Goto position Action:</b> define the next brace Key/Value to restore. The fist element of the
 * list is at position 1 and the last at number_of_element_in_list. A position less than 1 can not
 * be entered. If the position is superior to the number of element a ProcessingException is raised
 * and sequence is stopped. Send an empty message on outputPort at the end of action.
 * </p>
 * 
 * @author GRAMER
 * 
 */
public final class KeyValueListMemorizer extends Actor implements IActorFinalizer {

    private static final long serialVersionUID = 2970371209844116199L;
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueListMemorizer.class);

    public static final String LIST_NAME_LABEL = "List name";
    public static final String SEPARATOR_LABEL = "Separator";
    public static final String OPERATION_LABEL = "Operation";
    public static final String FIXED_NUMBER_OF_VALUE_LABEL = "Limited to last list value";
    public static final String GOTO_POSITION_LABEL = "Position value";
    public static final String FILTERED_KEYS_LABEL = "Filtered keys";
    public static final String FILTERED_KEYS_SEPARATOR = ";";
    private static final String DEFAULT_SEPARATOR = ":";
    
    private static Map<String, StringPairList> memorizedKeyValues = new HashMap<String, StringPairList>();

    public Port inputPort;
    public Port keyPort;
    public Port valuePort;
    public Port outputValuePort;

    /**
     * the name of the list on which the actor performs the action
     */
    @ParameterName(name = LIST_NAME_LABEL)
    public Parameter listNameParam;
    private String listName = "my list name";

    /**
     * separator indicate which character(s) is use to delimiter key and value from a String
     */
    @ParameterName(name = SEPARATOR_LABEL)
    public Parameter separatorParam;
    private String separator = DEFAULT_SEPARATOR;

    /**
     * action to be performed by actor. Can only be:
     * <ul>
     * <li>store brace Key/Value</li>
     * <li>restore the next brace Key/Value</li>
     * <li>clear the list</li>
     * <li>goto position(define the next brace Key/Value to restore)</li>
     * </ul>
     * 
     */
    @ParameterName(name = OPERATION_LABEL)
    public Parameter operationParam;
    private Operation operation;

    /**
     * Define if actor must raise an exception or send an empty value on outputValue when there are
     * no more element in list.
     */
    @ParameterName(name = FIXED_NUMBER_OF_VALUE_LABEL)
    public Parameter fixedNumberOfValueParam;
    private boolean fixedNumberOfValue = true;

    /**
     * define the next brace key/value to restore. fist element is 0 and last is
     * number_of_element_in_list -1
     */
    @ParameterName(name = GOTO_POSITION_LABEL)
    public Parameter gotoPositionParam;
    private int gotoPosition = 0;

    /**
     * filter the outputs according to a list of key. Key must be separated by a semicolon .
     */
    @ParameterName(name = FILTERED_KEYS_LABEL)
    public Parameter FilteredKeysParam;
    private final List<String> filteredKeysList = new ArrayList<String>();

    public KeyValueListMemorizer(final CompositeEntity container, final String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        inputPort = PortFactory.getInstance().createInputPort(this, null);
        keyPort = PortFactory.getInstance().createOutputPort(this, "key");
        valuePort = PortFactory.getInstance().createOutputPort(this, "value");
        outputValuePort = PortFactory.getInstance().createOutputPort(this, "output");

        listNameParam = new StringParameter(this, LIST_NAME_LABEL);
        listNameParam.setExpression(listName);

        separatorParam = new StringParameter(this, SEPARATOR_LABEL);
        separatorParam.setExpression(separator);

        operationParam = new StringParameter(this, OPERATION_LABEL);
        for (final Operation ope : Operation.values()) {
            operationParam.addChoice(ope.getDescription());
        }
        operationParam.setExpression(Operation.MEMORIZE.getDescription());

        fixedNumberOfValueParam = new Parameter(this, FIXED_NUMBER_OF_VALUE_LABEL,
                new BooleanToken(fixedNumberOfValue));
        fixedNumberOfValueParam.setTypeEquals(BaseType.BOOLEAN);

        gotoPositionParam = new Parameter(this, GOTO_POSITION_LABEL, new IntToken(gotoPosition));
        gotoPositionParam.setTypeEquals(BaseType.INT);

        FilteredKeysParam = new StringParameter(this, FILTERED_KEYS_LABEL);
        FilteredKeysParam.setExpression("");

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
                + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n" +

                "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n"
                + "<line x1=\"-15\" y1=\"0\" x2=\"15\" y2=\"0\" "
                + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"12\" y1=\"-3\" x2=\"15\" y2=\"0\" "
                + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"12\" y1=\"3\" x2=\"15\" y2=\"0\" "
                + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");

    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == listNameParam) {
            listName = PasserelleUtil.getParameterValue(listNameParam);
            if (listName.isEmpty()) {
                throw new IllegalActionException(LIST_NAME_LABEL + " can not be empty");
            }
        }
        else if (attribute == separatorParam) {
            separator = PasserelleUtil.getParameterValue(separatorParam);
        }
        else if (attribute == operationParam) {
            // throw an IllegalActionException if operation is unknown
            operation = Operation.fromDescription(PasserelleUtil.getParameterValue(operationParam)
                    .trim());
        }
        else if (attribute == FilteredKeysParam) {
            final String filteredKeys = PasserelleUtil.getParameterValue(FilteredKeysParam);

            if (!filteredKeys.isEmpty()) {
                final StringTokenizer tokenizer = new StringTokenizer(filteredKeys,
                        FILTERED_KEYS_SEPARATOR);
                filteredKeysList.clear();

                // key is added to list only if it's not empty
                while (tokenizer.hasMoreTokens()) {
                    final String key = tokenizer.nextToken().trim();
                    if (!key.isEmpty()) {
                        filteredKeysList.add(key);
                    }
                }
            }
        }
        else if (attribute == gotoPositionParam) {
            final int tmpValue = ((IntToken) gotoPositionParam.getToken()).intValue();

            if (tmpValue < 0) {
                throw new IllegalActionException(GOTO_POSITION_LABEL + " can not be negative");
            }
            gotoPosition = tmpValue;
        }
        else if (attribute == fixedNumberOfValueParam) {
            fixedNumberOfValue = ((BooleanToken) fixedNumberOfValueParam.getToken()).booleanValue();
        }
        else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
            final ProcessResponse response) throws ProcessingException {
        switch (operation) {
            case MEMORIZE:
                memorizeAction(request, response);
                break;

            case OUTPUT:
                outPutAction(response);
                break;

            case CLEAR:
                synchronized (memorizedKeyValues) {
                    LOGGER.debug("clear list \"" + listName + "\"");
                    ExecutionTracerService.trace(this, "List " + listName + " has been cleared");
                    if (memorizedKeyValues.remove(listName) == null) {
                        throw new ProcessingExceptionWithLog(this, Severity.FATAL, "list Name :\""
                                + listName + "\" is unknown; => can not be clear", this, null);
                    }
                    response.addOutputMessage(2, outputValuePort, createMessage());
                }
                break;

            case GOTO:
                synchronized (memorizedKeyValues) {
                    LOGGER.debug("set goto position to {} for  list \"{}\"", gotoPosition, listName);

                    if (memorizedKeyValues.containsKey(listName)) {
                        try {
                        	ExecutionTracerService.trace(this, "Goto " + gotoPosition + " for the list " + listName );
                            memorizedKeyValues.get(listName).setCursor(gotoPosition);
                            response.addOutputMessage(2, outputValuePort, createMessage());
                        }
                        catch (final IndexOutOfBoundsException e) {
                            throw new ProcessingExceptionWithLog(this, Severity.FATAL, "list: \""
                                    + listName + "\" " + e.getMessage(), this, e);
                        }
                    }
                    else {
                        throw new ProcessingExceptionWithLog(this, Severity.FATAL, "list Name :\""
                                + listName + "\" is unknown; => can not set goto Position", this,
                                null);
                    }
                }
                break;

            default:
                throw new ProcessingExceptionWithLog(this, Severity.FATAL, "Unknown operation \""
                        + operation.getDescription() + "\"", this, null);
        }
    }

    /**
     * this method is called by director at the end of sequence
     */
    @Override
    public void doFinalAction() {
        synchronized (memorizedKeyValues) {
            if (!memorizedKeyValues.isEmpty()) {
                LOGGER.debug("clear memorizedKeyValues");
                memorizedKeyValues.clear();
            }
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        super.doInitialize();

        final Director dir = getDirector();
        if (dir instanceof BasicDirector) {
            // register this actor as Finalizer to clear memorizedKeyValues at the end of sequence
            ((BasicDirector) dir).registerFinalizer(this);
        }
        else {
            throw new InitializationException(Severity.FATAL,
                    "this actor can only use with Soleil directrors", this, null);
        }
    }

    /**
     * do the Memorize action of actor. ie cut the string on input port in two parts( key, value)
     * and add these parts to the list defined by listName Passerelle parameter.
     * 
     * @param request the ProcessRequest given by process method
     * @param response the ProcessResponse given by process method
     * @throws ProcessingException {@inheritDoc} throws when
     *             <ul>
     *             <li>separator Passerelle parameter is empty.</li>
     *             <li>output message can not be created. @see
     *             fr.soleil.passerelle.util.PasserelleUtil#createContentMessage</li>
     *             </ul>
     */
    private void memorizeAction(final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (separator.trim().isEmpty()) {
            throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                    "separator can not be empty", this, null);

        }
        else {
            final String inputMessage = (String) PasserelleUtil.getInputValue(request
                    .getMessage(inputPort));

            final int index = inputMessage.indexOf(separator);
            // if we found the separator in inputMessage. we don't use split method because we just
            // cut the string in two part. if the separator appear many time, the first occurrence
            // delimiter the key and value (ie value contains other separator occurrence)
            if (index != -1) {

                try {
                    final StringPair keyValue = new StringPair(inputMessage.substring(0, index),
                            inputMessage.substring(index + 1));

                    synchronized (memorizedKeyValues) {
                        // if list exist we add brace Key/value to it
                        // else we create the list and add Key/Value to it

                        if (memorizedKeyValues.containsKey(listName)) {
                            LOGGER.debug("Memorization mode: add new value {} to \"{}\" = {}",
                                    new String[] { keyValue.toString(), listName,
                                            memorizedKeyValues.get(listName).toString() });

                            memorizedKeyValues.get(listName).add(keyValue);

                            ExecutionTracerService.trace(this,
                                    "Memorization mode: add  new brace  " + keyValue + " to list "
                                            + listName);
                        }
                        else {
                            LOGGER.debug(
                                    "Memorization mode: create new list \"{}\" and add new value {} ",
                                    listName, keyValue);

                            final StringPairList temp = new StringPairList();
                            temp.add(keyValue);

                            memorizedKeyValues.put(listName, temp);
                            ExecutionTracerService.trace(this,
                                    "Memorization mode: create new list " + listName
                                            + " and add new brace " + keyValue);
                        }

                        // Finally we send on output ports the key and value.(Like MessageMemorizer
                        // actor)
                        response.addOutputMessage(0, keyPort,
                                PasserelleUtil.createContentMessage(this, keyValue.key));
                        response.addOutputMessage(1, valuePort,
                                PasserelleUtil.createContentMessage(this, keyValue.value));
                    }
                }
                catch (final IllegalArgumentException e) {
                    LOGGER.debug("separator \"{}\" has beed founded but key was empty", separator);
                }
            }
            else {
                LOGGER.debug("separator \"{}\" not found", separator);
            }
            response.addOutputMessage(2, outputValuePort,
                    PasserelleUtil.createContentMessage(this, inputMessage));
        }
    }

    /**
     * do the outputAction of actor. ie send the next key and value contains in listName on KeyPort
     * and ValuePort.
     * <ul>
     * <li>if the list is not register in map(ie user never memorize key/value whith this list Name)
     * a ProcessingException is thrown</li>
     * <li>if there are no more key/ value a ProcessingException is thrown.</li>
     * </ul>
     * 
     * @param the ProcessResponse given by process method
     * @throws ProcessingException {@inheritDoc} it throw if:
     *             <ul>
     *             <li>list list name is unknown</li>
     *             <li>there are no more element in list</li>
     *             <li>outMessage can be created @see
     *             fr.soleil.passerelle.util.PasserelleUtil#createContentMessage</li>
     *             </ul>
     */
    private void outPutAction(final ProcessResponse response) throws ProcessingException {
        synchronized (memorizedKeyValues) {
            if (memorizedKeyValues.containsKey(listName)) {
                try {
                    final StringPair temp = memorizedKeyValues.get(listName).getNextElement(
                            filteredKeysList);

                    LOGGER.debug("output mode: getNextValue in \"{}\" = {} is {}",
                            new String[] { listName, memorizedKeyValues.get(listName).toString(),
                                    temp.toString() });

                    ExecutionTracerService.trace(this, "key: " + temp.key);
                    ExecutionTracerService.trace(this, "value: " + temp.value);
                    response.addOutputMessage(0, keyPort,
                            PasserelleUtil.createContentMessage(this, temp.key));
                    response.addOutputMessage(1, valuePort,
                            PasserelleUtil.createContentMessage(this, temp.value));
                    response.addOutputMessage(2, outputValuePort, createMessage());

                }
                catch (final IndexOutOfBoundsException e) {
                    if (fixedNumberOfValue) {
                        throw new ProcessingExceptionWithLog(this, Severity.FATAL,
                                "There are no more element in list name :\"" + listName + "\"",
                                this, null);
                    }
                    else {
                        response.addOutputMessage(0, keyPort, createMessage());
                        response.addOutputMessage(1, valuePort, createMessage());
                        response.addOutputMessage(2, outputValuePort, createMessage());
                    }
                }
            }
            else {
                throw new ProcessingExceptionWithLog(this, Severity.FATAL, "list Name :\""
                        + listName + "\" is unknown", this, null);
            }
        }
    }

    /**
     * A subclass of ArrayList that can only store StringPair. This class add getNextElement method
     * which return the next element in list. Next element in list is defined by his cursor field
     * that represent the index of next element. the cursor can be setted by setCursor.
     * 
     * @author GRAMER
     * 
     */
    public static final class StringPairList extends ArrayList<StringPair> {
        private static final long serialVersionUID = 3598066928451825634L;

        /**
         * index of element returned by getNextElement method
         */
        private int cursor = 0;

        /**
         * set the cursor to the specified position. (it's dev position ie first element =0 and last
         * size() -1)
         * 
         * @param cursor
         * @throws IndexOutOfBoundsException {@inheritDoc} if the specified position is negative,
         *             superior or equal to size of list
         */
        public void setCursor(final int cursor) {
            if (cursor < 0 || cursor >= size()) {
                throw new IndexOutOfBoundsException("invalid goto position: " + cursor
                        + ". The list have " + size() + " element(s)");
            }
            this.cursor = cursor;
        }

        /**
         * return the next element in list according to the filter list passed by parameter
         * 
         * @param filteredKeys contains the list of keys
         * @return the next element in list
         * @throws IndexOutOfBoundsException {@inheritDoc} if there are no more keys in list
         */
        public StringPair getNextElement(final List<String> filteredKeys) {
            if (cursor == size()) {
                throw new IndexOutOfBoundsException("no more element");
            }

            StringPair result = get(cursor++);

            // if user has defined a filter on key, we return the next key which correspond to this
            // filter
            if (!filteredKeys.isEmpty()) {
                while (!filteredKeys.contains(result.key)) {
                    result = get(cursor++);
                }
            }
            return result;
        }

        @Override
        public void clear() {
            cursor = 0;
            super.clear();
        }

    }

    /**
     * class which store a not null pair key/Value. the both are String which are automatically
     * trimed.
     * 
     * @author GRAMER
     * 
     */
    public static final class StringPair {
        /**
         * can not be null or empty
         */
        public String key = "";

        /**
         * can not be null
         */
        public String value = "";

        public StringPair(final String key, final String value) {
            if (key == null) {
                throw new IllegalArgumentException("Key can not be null");
            }

            if (value == null) {
                throw new IllegalArgumentException("Value can not be null");
            }

            this.key = key.trim();
            this.value = value.trim();

            if (this.key.isEmpty()) {
                throw new IllegalArgumentException("Key can not be empty");
            }

        }

        @Override
        public String toString() {
            return "[" + key + ": " + value + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final StringPair other = (StringPair) o;

            if (!key.equals(other.key) || !value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

    }

    /**
     * Define all operations which are possible with this Actor. @see
     * fr.soleil.passerelle.actor.flow.KeyValueListMemorizer for more details
     * 
     * @author GRAMER
     * 
     */
    public static enum Operation {
        MEMORIZE("do memorization"), OUTPUT("output value"), CLEAR("Clear list"), GOTO(
                "Go to position");

        /**
         * the label which is display in parameter editing window
         */
        private String descprition;

        /**
         * map the description of operation to the correct operation instance.Use for
         * fromDescription(String) method
         */
        private static final Map<String, Operation> DescriptionMap = new HashMap<String, Operation>();
        static {
            for (final Operation operation : values()) {
                DescriptionMap.put(operation.getDescription(), operation);
            }
        }

        private Operation(final String descprition) {
            this.descprition = descprition;
        }

        public String getDescription() {
            return descprition;
        }

        /**
         * get an instance of operation from the description. use to know which operation the use
         * had chosen
         * 
         * @param desc the description of operation
         * @return new instance of operation according to description
         * 
         * @throws IllegalArgumentException {@inheritDoc} if desc is unknown
         */
        public static Operation fromDescription(final String desc) {
            final Operation value = DescriptionMap.get(desc);
            if (value != null) {
                return value;
            }
            throw new IllegalArgumentException("Description incconu : " + desc);
        }
    }
}
