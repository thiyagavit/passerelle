package fr.soleil.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.ProcessingException;

import fr.soleil.lib.project.math.ArrayUtils;
import fr.soleil.passerelle.util.ExceptionUtil;

//TODO: intersection de 2 dtes
public class BeamCalculations {

    private final static Logger logger = LoggerFactory.getLogger(BeamCalculations.class);

    // def position(_measurePoints, _measuredVals):
    // points_nb = len(_measurePoints)
    // integral_f = 0
    // integral_fx = 0
    // for j in range (points_nb-1):
    // tmp = (1./2)*abs(_measurePoints[j+1]-_measurePoints[j])
    // integral_f += (_measuredVals[j]+_measuredVals[j+1])*tmp
    // integral_fx +=
    // (_measurePoints[j]*_measuredVals[j]+_measurePoints[j+1]*_measuredVals[j+1])*tmp
    //
    // return integral_fx/integral_f
    // math def (wikipedia) c=integrale(x*f(x))/intergrale(f(x))
    /**
     * centroid = geometric center, barycenter, center of mass
     */
    public static double centroidPosition(final double[] x, final double[] y) {
        double integralF = 0;
        double integralFx = 0;
        for (int i = 0; i < x.length - 1; i++) {
            final double tmp = 0.5 * Math.abs(x[i + 1] - x[i]);
            // System.out.println(tmp);
            integralF += (y[i] + y[i + 1]) * tmp;
            // System.out.println("integralF "+integralF);
            integralFx += (x[i] * y[i] + x[i + 1] * y[i + 1]) * tmp;
            // System.out.println("integralFx "+integralFx);
        }
        return integralFx / integralF;
    }

    // def percentMaxValue(_measuredVals, _percentage):
    // return max(_measuredVals)*(float(_percentage)/100)
    // def bounds(_measurePoints, _measuredVals, _percentage):
    //
    // pm = percentMaxValue(_measuredVals, _percentage)
    // #print pm
    // inx = 0
    // while _measuredVals[inx] < pm:
    // inx = inx+1
    // inx1_down = inx-1
    // inx1_up = inx
    // while _measuredVals[inx] >= pm:
    // inx = inx+1
    // inx2_up = inx-1
    // inx2_down = inx
    //
    // bound1 = linear_interpolation.get_intermediatePos(pm,
    // _measurePoints[inx1_down], _measurePoints[inx1_up],
    // _measuredVals[inx1_down], _measuredVals[inx1_up])
    //
    // bound2 = linear_interpolation.get_intermediatePos(pm,
    // _measurePoints[inx2_down], _measurePoints[inx2_up],
    // _measuredVals[inx2_down], _measuredVals[inx2_up])
    //
    // return bound1, bound2

    // def get_intermediatePos(intermediateVal, neighbor1_pos, neighbor2_Pos,
    // neighbor1_val, neighbor2_val):
    //
    // # if _hm does not lie between neighbor1_val and neighbor2_val, it does
    // not make sense
    // a, b = coeffs(neighbor1_pos, neighbor2_Pos, neighbor1_val, neighbor2_val)
    //
    // return (intermediateVal-b)/a
    //
    // def coeffs(neighbor1_pos, neighbor2_Pos, neighbor1_val, neighbor2_val):
    //
    // a = (neighbor2_val-neighbor1_val)/( neighbor2_Pos - neighbor1_pos);
    // b = neighbor1_val-a*neighbor1_pos;
    //
    // return a,b
    //
    // def value(_measurePoints, _measuredVals, _percentage):
    //
    // a, b = bounds(_measurePoints, _measuredVals, _percentage)
    // return abs(b-a)

    /**
     * retrieve the 2 positions x1 and x2 at half maximum
     */
    public static double[] getHmPositions(final double[] x, final double[] y) {
    	// Sort the array as ascending, the last value will be the max
    	Arrays.sort(y);
    
        // calculate half maximum
        final double halfMax =y[y.length - 1] * 0.5;
        // System.out.println("max "+Collections.max(yVal));
        // System.out.println("halfMax "+halfMax);
        // finding the 2 nearest positions for first width point x1
        int i = 0;
        while (y[i] < halfMax) {
            // System.out.println("i "+i +" y[i] "+y[i]);
            i++;
        }
        final double x1Down = x[i - 1];
        final double x1Up = x[i];
        final double y1Down = y[i - 1];
        final double y1Up = y[i];

        // finding the 2 nearest positions for second width point x2
        int j = y.length - 1;
        while (y[j] < halfMax) {
            j--;
        }
        final double x2Down = x[j + 1];
        final double x2Up = x[j];
        final double y2Down = y[j + 1];
        final double y2Up = y[j];

        // make an interpolation to find the real x1 and x2
        final double x1 = Analysis.linearInterpolation(halfMax, x1Down, x1Up, y1Down, y1Up);
        final double x2 = Analysis.linearInterpolation(halfMax, x2Down, x2Up, y2Down, y2Up);

        return new double[] { x1, x2 };
    }

    public static double fwhm(final double[] x, final double[] y) throws ProcessingException {
        double res = Double.NaN;
        try {
            final double[] values = getHmPositions(x, y);
            res = Math.abs(values[1] - values[0]);
        } catch (Exception e) {
            ExceptionUtil.throwProcessingException("Invalid Input Values to calculate fwhm",BeamCalculations.class.getName(),e);
        }
        return res;
    }

    public static double midPositionFwhm(final double[] x, final double[] y) {
        final double[] values = getHmPositions(x, y);
        return Math.abs(values[1] - values[0]) / 2 + values[0];
    }

    // public static double closestPositionToOrigin(final double[] x,
    // final double[] y) {
    // double minDistance = Math.abs(x[0]) + Math.abs(y[0]);
    // double minPosition = x[0];
    // for (int i = 0; i < y.length; i++) {
    // final double distance = Math.abs(x[i]) + Math.abs(y[i]);
    // if (distance < minDistance) {
    // minDistance = distance;
    // minPosition = x[i];
    // }
    // }
    // return minPosition;
    //

    // public static double findPositionOfFirstDiff(final double diffValue,
    // final double[] x, final double[] y) {
    // final double diff = 0;
    // for (int i = 1; i < x.length; i++) {
    // if (y[i] - y[i - 1] >= diffValue) {
    //
    // }
    // }
    // }
    /**
     * paramaters {x0, a, p}<br>
     * x0: position de l'intersection des 2 dtes <br>
     * a: coefficient de la 1er dte: y = a<br>
     * p: pente de la 2eme droite <br>
     */
    public static double intersectionPosition(final double[] x, final double[] y, final double[] initialGuess)
            throws ProcessingException {
        final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        final CurveFitter fitter = new CurveFitter(optimizer);
        for (int i = 0; i < x.length; i++) {
            fitter.addObservedPoint(x[i], y[i]);
        }
        double result = Double.NaN;
        // paramaters {x0, a, p}
        // final int max = y.length - 1;
        // final double[] initialGuess = new double[3];
        // initialGuess[0] = 0;// x[max] - x[0] / 2;
        // initialGuess[1] = y[0];
        // initialGuess[2] = (y[max] - y[max - 1]) / (x[max] - x[max - 1]);
        // System.out.println("init guess " +
        // ArrayUtils.toString(initialGuess));
        // optimizer.setInitialStepBoundFactor(10.0);
        // optimizer.setCostRelativeTolerance(1.0e-30);
        // optimizer.setParRelativeTolerance(1.0e-30);
        // optimizer.setOrthoTolerance(1.0e-30);
        try {
            // the result is x0
            final double parameters[] = fitter.fit(new BeamCalculations.FunctionForFittingDualRamp(), initialGuess);
            logger.debug(Arrays.toString(parameters));
            logger.debug("MaxIterations: " + optimizer.getMaxIterations() + ", Iterations " + optimizer.getIterations());
            logger.debug("MaxEvaluations " + optimizer.getMaxEvaluations() + ", Evaluations "
                    + optimizer.getEvaluations());
            logger.debug("RMS " + optimizer.getRMS());
            for (final double element : x) {
                logger.debug(String.valueOf(new BeamCalculations.FunctionForFittingDualRamp()
                        .value(element, parameters)));
            }
            result = parameters[0];
        } catch (final Exception e) {
            //e.printStackTrace();
            ExceptionUtil.throwProcessingException("Impossible to fit",BeamCalculations.class.getName(),e);
        }
        
        return result ;
    }

    /**
     * paramaters {x0, a, p}<br>
     * x0: position de l'intersection des 2 dtes <br>
     * a: coefficient de la 1er dte: y = a<br>
     * p: pente de la 2eme droite <br>
     */
    public static class FunctionForFittingDualRamp implements ParametricRealFunction {

        public double[] gradient(final double x, final double[] parameters) throws FunctionEvaluationException {
            final double x0 = parameters[0];
            final double[] gradient = new double[parameters.length];
            final double p = parameters[2];
            double dydx0, dyda, dydp;
            if (x <= x0) {
                dydx0 = 0;
                dyda = 1;
                dydp = 0;
            } else {
                dydx0 = 1;
                dyda = -p;
                dydp = x - x0;
            }
            
            gradient[0] = dydx0;
            gradient[1] = dyda;
            gradient[2] = dydp;
            return gradient;
        }

        public double value(final double x, final double[] parameters) throws FunctionEvaluationException {
            double value;
            final double x0 = parameters[0];
            final double a = parameters[1];
            final double p = parameters[2];
            if (x <= x0) {
                value = a;
            } else {
                value = p * (x - x0) + a;
            }
            // System.out.println("value " + value);
            return value;
        }
    }
}
