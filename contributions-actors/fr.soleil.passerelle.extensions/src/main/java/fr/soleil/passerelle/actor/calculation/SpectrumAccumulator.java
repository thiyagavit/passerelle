package fr.soleil.passerelle.actor.calculation;

import java.util.Arrays;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.TransformerV3;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class SpectrumAccumulator extends TransformerV3 {

    private double[] values;
    private double[] accumul;

    enum Operation {
	ACCUMULATE, AVERAGE;
    }

    public Parameter operationParam;
    private Operation operation;

    private int accumulations = 0;

    public SpectrumAccumulator(final CompositeEntity container, final String name)
	    throws NameDuplicationException, IllegalActionException {
	super(container, name);
	input.setExpectedMessageContentType(String.class);
	operationParam = new StringParameter(this, "operation");
	operationParam.addChoice(Operation.ACCUMULATE.toString());
	operationParam.addChoice(Operation.AVERAGE.toString());
	operationParam.setExpression(Operation.ACCUMULATE.toString());

    }

    @Override
    protected void doInitialize() throws InitializationException {
	values = null;
	accumul = null;
	accumulations = 0;
	super.doInitialize();
    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request,
	    final ProcessResponse response) throws ProcessingException {
	final ManagedMessage message = request.getMessage(input);
	final Object inputValue = PasserelleUtil.getInputValue(message);

	String[] newValueS = null;

	if (inputValue instanceof String) {
	    newValueS = ((String) inputValue).split(",");
	} else if (inputValue.getClass().isArray()) {
	    newValueS = (String[]) inputValue;
	} else {
	    ExceptionUtil.throwProcessingException("cannot get input data of type " + inputValue.getClass(), this);
	}

	if (values == null) {
	    values = new double[newValueS.length];
	    accumul = new double[newValueS.length];
	    Arrays.fill(values, 0);
	    Arrays.fill(accumul, 0);
	} else if (newValueS.length != values.length) {
	    ExceptionUtil.throwProcessingException("size of input must be " + values.length, this);
	}
	accumulations++;
	System.out.println(Arrays.toString(values));
	for (int i = 0; i < newValueS.length; i++) {
	    accumul[i] = accumul[i] + new Double(newValueS[i]);
	}
	if (operation.equals(Operation.AVERAGE)) {
	    for (int i = 0; i < newValueS.length; i++) {
		values[i] = accumul[i] / accumulations;
	    }
	} else {
	    values = accumul;
	}
	System.out.println(Arrays.toString(values));

	ExecutionTracerService.trace(this, "Accumulating - result is " + Arrays.toString(values));
	response.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, values));

    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
	if (attribute == operationParam) {
	    final String value = PasserelleUtil.getParameterValue(operationParam);
	    if (value.equals(Operation.ACCUMULATE.toString())) {
		operation = Operation.ACCUMULATE;
	    } else {
		operation = Operation.AVERAGE;
	    }
	}
	super.attributeChanged(attribute);
    }
}
