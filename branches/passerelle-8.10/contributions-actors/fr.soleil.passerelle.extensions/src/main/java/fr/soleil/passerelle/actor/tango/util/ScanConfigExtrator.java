package fr.soleil.passerelle.actor.tango.util;

import java.io.File;
import java.io.IOException;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

import fr.soleil.passerelle.salsa.SalsaFactory;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.salsa.model.scanconfig.Dimension;
import fr.soleil.salsa.model.scanconfig.LinearTrajectory;
import fr.soleil.salsa.model.scanconfig.Range;
import fr.soleil.salsa.model.scanconfig.ScanConfiguration;
import fr.soleil.salsa.util.RangeList;

@SuppressWarnings("serial")
public class ScanConfigExtrator extends Transformer {

    // private final static Logger logger = LoggerFactory.getLogger(ScanConfigExtrator.class);
    // private FileParameter fileNameParam;
    private String fileName = "/usr";

    protected ScanConfiguration config = null;
    private File file;
    private double from;
    private double middle;
    private double to;

    Port middlePort;
    Port toPort;

    public ScanConfigExtrator(CompositeEntity container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);

        input.setExpectedMessageContentType(String.class);
        output.setName("from");
        middlePort = PortFactory.getInstance().createOutputPort(this, "middle");
        toPort = PortFactory.getInstance().createOutputPort(this, "to");

        /*fileNameParam = new FileParameter(this, "Scan Config");
        fileNameParam.setExpression(fileName);
        registerConfigurableParameter(fileNameParam);*/

    }

    @Override
    protected void doFire(ManagedMessage message) throws ProcessingException {

        if (isMockMode()) {
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, 0));
            sendOutputMsg(middlePort, PasserelleUtil.createContentMessage(this, 1));
            sendOutputMsg(toPort, PasserelleUtil.createContentMessage(this, 2));
        } else {
            fileName = (String) PasserelleUtil.getInputValue(message);
            file = new File(fileName);
            try {
                // load the config from the salsa file
                config = SalsaFactory.getInstance().loadConfig(file);
            } catch (IOException e1) {
                ExceptionUtil.throwProcessingException("Cannot load config " + fileName, config, e1);
            }
            Dimension dim = config.getDimensions().getDimension(0);
            RangeList ranges = dim.getRanges();
            Range range0 = (Range) ranges.get(0);
            LinearTrajectory traj = (LinearTrajectory) dim.getTrajectory(dim.getActuators().getActuator(0), range0);
            from = traj.getOriginPosition();

            Range lastRange = (Range) ranges.get(ranges.size() - 1);
            traj = (LinearTrajectory) dim.getTrajectory(dim.getActuators().getActuator(0), lastRange);
            to = traj.getEndPosition();

            middle = Math.abs(from + to) / 2;
            sendOutputMsg(output, PasserelleUtil.createContentMessage(this, from));
            sendOutputMsg(middlePort, PasserelleUtil.createContentMessage(this, middle));
            sendOutputMsg(toPort, PasserelleUtil.createContentMessage(this, to));
        }
    }

}
