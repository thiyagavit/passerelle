package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.util.List;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

import fr.soleil.salsa.api.SalsaAPI;
import fr.soleil.salsa.entity.IActuator;
import fr.soleil.salsa.entity.IConfig;
import fr.soleil.salsa.entity.IDevice;
import fr.soleil.salsa.entity.IDimension;
import fr.soleil.salsa.entity.IRange;
import fr.soleil.salsa.entity.IRangeIntegrated;
import fr.soleil.salsa.entity.ITrajectory;
import fr.soleil.salsa.entity.impl.scan2d.Config2DImpl;
import fr.soleil.salsa.entity.impl.scan2d.Dimension2DXImpl;
import fr.soleil.salsa.entity.impl.scan2d.Dimension2DYImpl;
import fr.soleil.salsa.entity.scan2D.IRange2DX;
import fr.soleil.salsa.entity.scan2D.IRange2DY;

public final class ScanUtil {

    // The context (ScanServer Name) is managed by the Api itself and the -DSalsaPreferences
    private static SalsaAPI m_salsaApi = null;

    public static SalsaAPI getCurrentSalsaApi() {

        if (m_salsaApi == null) {
            m_salsaApi = new SalsaAPI();
        }
        return m_salsaApi;
    }

    private static double calculDelta(double endPos, double beginPos, int steps) {
        return Math.abs((endPos - beginPos) / steps);
    }

    private static double calculSpeed(double delta, double integrationTime) {
        return delta / integrationTime;
    }

    private static void setTrajectory(final IConfig<?> conf, final IRangeIntegrated sourceRange, boolean relative, int dimensionIndex)  throws PasserelleException{
        if (conf == null) {
            throw new PasserelleException(ErrorCode.ERROR, "Configuration is not defined.", null);
        }

        List<IDimension> dimensionList = conf.getDimensionList();
        if (dimensionList == null || dimensionList.isEmpty() || dimensionIndex >= dimensionList.size()) {
            throw new PasserelleException(ErrorCode.ERROR, "No dimension found.", null);
        }

        IDimension dim = dimensionList.get(dimensionIndex);
        List<IActuator> actuatorsList = dim.getActuatorsList();
        int nbEnable = getNbEnableDevice(actuatorsList);

        if (nbEnable == 0) {
            throw new PasserelleException(ErrorCode.ERROR, "No activated actuator is defined.", null);
        }

        if (dim == null || dim.getRangeList() == null || dim.getRangeList().isEmpty()) {
            throw new PasserelleException(ErrorCode.ERROR, "Range List must not be empty.", null);
        }

        List<? extends IRange> rangeList = dim.getRangeList();
        if (rangeList.size() > 1) {
            throw new PasserelleException(ErrorCode.ERROR, "There must be only one range", null);
        }

        final IRange destRange = rangeList.get(0);
        List<ITrajectory> sourceTrajectoriesList = sourceRange.getTrajectoriesList();
        List<ITrajectory> destTrajectoriesList = destRange.getTrajectoriesList();

        if (sourceTrajectoriesList == null || sourceTrajectoriesList.isEmpty() || destTrajectoriesList == null
                || destTrajectoriesList.isEmpty()) {
            throw new PasserelleException(ErrorCode.ERROR, "Actuators (trajectory) must not be empty.", null);
        }

        if (nbEnable > sourceTrajectoriesList.size()) {
            throw new PasserelleException(ErrorCode.ERROR,
                    "Trajectory list size must be equals to activated actuator list size", null);
        }

        if (destTrajectoriesList.size() < sourceTrajectoriesList.size()) {
            throw new PasserelleException(ErrorCode.ERROR,
                    "Trajectory list size must be at least smaller than the trajectory list of the configuration", null);
        }

        // Range Update
        if (destRange instanceof IRangeIntegrated) {
            ((IRangeIntegrated) destRange).setIntegrationTime(sourceRange.getIntegrationTime());
        }
        destRange.setStepsNumber(sourceRange.getStepsNumber());

        IActuator actuator = null;
        int nbOfActuator = sourceTrajectoriesList.size();
        int nbOfActivatedActuator = 0;
        ITrajectory sourceTrajectory = null;
        ITrajectory destTrajectory = null;
        for (int actuatorIndex = 0; actuatorIndex < actuatorsList.size(); actuatorIndex++) {
            actuator = actuatorsList.get(actuatorIndex);
            if (actuator.isEnabled() && nbOfActivatedActuator < nbOfActuator) {
                sourceTrajectory = sourceTrajectoriesList.get(nbOfActivatedActuator);
                destTrajectory = destTrajectoriesList.get(actuatorIndex);
                destTrajectory.setBeginPosition(sourceTrajectory.getBeginPosition());
                destTrajectory.setEndPosition(sourceTrajectory.getEndPosition());
                destTrajectory.setRelative(relative);
                nbOfActivatedActuator++;
            }
        }
    }
    
    public static void setTrajectory1D(final IConfig<?> conf, final IRangeIntegrated sourceRange, boolean relative)
            throws PasserelleException {
        setTrajectory(conf, sourceRange,relative,0);
    }

    private static int getNbEnableDevice(List<? extends IDevice> deviceList) {
        int nb = 0;
        if (deviceList != null && !deviceList.isEmpty()) {
            for (IDevice device : deviceList) {
                if (device.isEnabled()) {
                    nb++;
                }
            }
        }
        return nb;
    }

    /*
     * public static void setTrajectory2D(final Config2DImpl conf, final
     * List<ScanRangeX> scanRangeXList, final List<ScanRangeY> scanRangeYList) {
     *
     * }
     */
    public static void setTrajectory2D(final Config2DImpl conf, final ScanRangeX scanRangeX, final ScanRangeY scanRangeY)
            throws PasserelleException {
        setTrajectory2D(conf, scanRangeX);
        setTrajectory2D(conf, scanRangeY);
    }

    private static void setTrajectory2D(final Config2DImpl conf, final ScanRangeX scanRangeX)
            throws PasserelleException {

        final Dimension2DXImpl xDim = (Dimension2DXImpl) conf.getDimensionX();

        if (xDim.getRangesList().size() == 0) {
            throw new PasserelleException(ErrorCode.ERROR, "Range List must not be empty.", null);
        }

        final IRange2DX xRange = xDim.getRangesList().get(0);
        // modify one
        if (xRange.getTrajectoriesList().size() == 0) {
            throw new PasserelleException(ErrorCode.ERROR, "X Trajectory must not be empty.", null);
        }

        final ITrajectory xTrajectory = xRange.getTrajectoriesList().get(0);
        // Range Update
        xRange.setIntegrationTime(scanRangeX.getIntegrationTime());
        xRange.setStepsNumber(scanRangeX.getNumberOfSteps());
        // Only one actuator X
        xTrajectory.setBeginPosition(scanRangeX.getFrom());
        xTrajectory.setEndPosition(scanRangeX.getTo());
        xTrajectory.setRelative(scanRangeX.isRelative());

        double delta = calculDelta(xTrajectory.getEndPosition(), xTrajectory.getBeginPosition(),
                xRange.getStepsNumber());
        xTrajectory.setDelta(delta);
        xTrajectory.setSpeed(calculSpeed(delta, xRange.getIntegrationTime()));

    }

    private static void setTrajectory2D(final Config2DImpl conf, final ScanRangeY scanRangeY)
            throws PasserelleException {

        final Dimension2DYImpl yDim = (Dimension2DYImpl) conf.getDimensionY();

        if (yDim.getRangesList().size() == 0) {
            throw new PasserelleException(ErrorCode.ERROR, "Range List must not be empty.", null);
        }

        final IRange2DY yRange = yDim.getRangesList().get(0);
        if (yRange.getTrajectoriesList().size() == 0) {
            throw new PasserelleException(ErrorCode.ERROR, "Y Trajectory must not be empty.", null);
        }

        final ITrajectory yTrajectory = yRange.getTrajectoriesList().get(0);

        // Range Update
        yRange.setStepsNumber(scanRangeY.getNumberOfSteps());

        // Only one actuator Y
        yTrajectory.setBeginPosition(scanRangeY.getFrom());
        yTrajectory.setEndPosition(scanRangeY.getTo());
        yTrajectory.setRelative(scanRangeY.isRelative());

        double delta = calculDelta(yTrajectory.getEndPosition(), yTrajectory.getBeginPosition(),
                yRange.getStepsNumber());
        yTrajectory.setDelta(delta);
        yTrajectory.setSpeed(calculSpeed(delta, yRange.getStepsNumber()));

    }
}
