package fr.soleil.passerelle.actor.tango.acquisition.scan;

import com.isencia.passerelle.core.PasserelleException;
import fr.soleil.salsa.api.SalsaAPI;
import fr.soleil.salsa.entity.ITrajectory;
import fr.soleil.salsa.entity.impl.scan1d.Config1DImpl;
import fr.soleil.salsa.entity.impl.scan2d.Config2DImpl;
import fr.soleil.salsa.entity.impl.scan2d.Dimension2DXImpl;
import fr.soleil.salsa.entity.impl.scan2d.Dimension2DYImpl;
import fr.soleil.salsa.entity.scan1d.IDimension1D;
import fr.soleil.salsa.entity.scan1d.IRange1D;
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

	private static double calculDelta(double endPos, double beginPos, int steps){
		return Math.abs((endPos - beginPos)/ steps);
	}
	private static double calculSpeed(double delta, double integrationTime){
		return delta / integrationTime;
	}
	public static void setTrajectory1D(final Config1DImpl conf,
			final ScanRangeX scanRangeX) throws PasserelleException {

		final IDimension1D dim = conf.getDimensionX();
		if (dim.getRangesXList().size() == 0) {
			throw new PasserelleException("Range List must not be empty.",
					null, null);
		}

		final IRange1D xRange = dim.getRangesXList().get(0);

		if (xRange.getTrajectoriesList().size() == 0) {
			throw new PasserelleException(
					"X Actuators (trajectory) must not be empty.", null, null);
		}
		// Range Update
		xRange.setIntegrationTime(scanRangeX.getIntegrationTime());
		xRange.setStepsNumber(scanRangeX.getNumberOfSteps());

		// Only one actuator
		final ITrajectory xtrajectory = xRange.getTrajectoriesList().get(0);
		xtrajectory.setBeginPosition(scanRangeX.getFrom());
		xtrajectory.setEndPosition(scanRangeX.getTo());
		xtrajectory.setRelative(scanRangeX.isRelative());

		double delta = calculDelta(xtrajectory.getEndPosition(),xtrajectory
				.getBeginPosition(), xRange.getStepsNumber());
		xtrajectory.setDelta(delta);
		xtrajectory.setSpeed(calculSpeed(delta,xRange.getIntegrationTime()));

	}

	/*
	 * public static void setTrajectory2D(final Config2DImpl conf, final
	 * List<ScanRangeX> scanRangeXList, final List<ScanRangeY> scanRangeYList) {
	 *
	 * }
	 */
	public static void setTrajectory2D(final Config2DImpl conf,
			final ScanRangeX scanRangeX, final ScanRangeY scanRangeY)
			throws PasserelleException {
		setTrajectory2D(conf, scanRangeX);
		setTrajectory2D(conf, scanRangeY);
	}

	private static void setTrajectory2D(final Config2DImpl conf,
			final ScanRangeX scanRangeX) throws PasserelleException {

		final Dimension2DXImpl xDim = (Dimension2DXImpl) conf.getDimensionX();

		if (xDim.getRangesList().size() == 0) {
			throw new PasserelleException("Range List must not be empty.",
					null, null);
		}

		final IRange2DX xRange = xDim.getRangesList().get(0);
		// modify one
		if (xRange.getTrajectoriesList().size() == 0) {
			throw new PasserelleException("X Trajectory must not be empty.",
					null, null);
		}

		final ITrajectory xTrajectory = xRange.getTrajectoriesList().get(0);
		// Range Update
		xRange.setIntegrationTime(scanRangeX.getIntegrationTime());
		xRange.setStepsNumber(scanRangeX.getNumberOfSteps());
		// Only one actuator X
		xTrajectory.setBeginPosition(scanRangeX.getFrom());
		xTrajectory.setEndPosition(scanRangeX.getTo());
		xTrajectory.setRelative(scanRangeX.isRelative());

		double delta = calculDelta(xTrajectory.getEndPosition(),xTrajectory
				.getBeginPosition(), xRange.getStepsNumber());
		xTrajectory.setDelta(delta);
		xTrajectory.setSpeed(calculSpeed(delta,xRange.getIntegrationTime()));

	}

	private static void setTrajectory2D(final Config2DImpl conf,
			final ScanRangeY scanRangeY) throws PasserelleException {

		final Dimension2DYImpl yDim = (Dimension2DYImpl) conf.getDimensionY();

		if (yDim.getRangesList().size() == 0) {
			throw new PasserelleException("Range List must not be empty.",
					null, null);
		}

		final IRange2DY yRange = yDim.getRangesList().get(0);
		if (yRange.getTrajectoriesList().size() == 0) {
			throw new PasserelleException("Y Trajectory must not be empty.",
					null, null);
		}

		final ITrajectory yTrajectory = yRange.getTrajectoriesList().get(0);

		// Range Update
		yRange.setStepsNumber(scanRangeY.getNumberOfSteps());

		// Only one actuator Y
		yTrajectory.setBeginPosition(scanRangeY.getFrom());
		yTrajectory.setEndPosition(scanRangeY.getTo());
		yTrajectory.setRelative(scanRangeY.isRelative());

		double delta = calculDelta(yTrajectory.getEndPosition(),yTrajectory
				.getBeginPosition(), yRange.getStepsNumber());
		yTrajectory.setDelta(delta);
		yTrajectory.setSpeed(calculSpeed(delta,yRange.getStepsNumber()));

	}
}
