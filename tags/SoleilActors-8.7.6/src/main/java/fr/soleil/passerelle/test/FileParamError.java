package fr.soleil.passerelle.test;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;

@SuppressWarnings("serial")
public class FileParamError extends Transformer {

    FileParameter param;

    public FileParamError(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        param = new FileParameter(this, "test");

    }

    @Override
    protected void doFire(final ManagedMessage arg0) throws ProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        // if (attribute == param) {
        // System.out.println("exp " + param.getExpression());
        // System.out
        // .println("eval" + PasserelleUtil.getParameterValue(param));
        // }
        super.attributeChanged(attribute);
    }

}
