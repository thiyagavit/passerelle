package fr.soleil.math;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.apache.commons.math.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math.analysis.interpolation.NevilleInterpolator;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.ProcessingException;

import fr.soleil.passerelle.util.ExceptionUtil;

public class Analysis {

    private final static Logger logger = LoggerFactory.getLogger(Analysis.class);

    private class BeamDataFunction implements UnivariateRealFunction {
        UnivariateRealFunction function;
        UnivariateRealInterpolator interpolator = new SplineInterpolator();

        public BeamDataFunction(final double[] x, final double[] y) {
            try {
                function = interpolator.interpolate(x, y);
                logger.debug("interpolated by function " + function.getClass());
            } catch (final MathException e) {
                // TODO
                // throw new
                // ProcessingException("impossible to create interpolator",null,e);
            }

        }

        public double value(final double val) throws FunctionEvaluationException {
            // System.out.println("getting "+val);
            try {
                return function.value(val);
            } catch (final FunctionEvaluationException e) {
                // TODO
                // throw new
                // ProcessingException("impossible to interpolate",null,e);
            }
            return 0;
        }

    }

    public static double[] trapezoidIntegrale(final double[] x, final double[] y) throws ProcessingException {
        final double[] result = new double[x.length];
        final Analysis s = new Analysis();
        final UnivariateRealFunction function = s.new BeamDataFunction(x, y);
        final UnivariateRealIntegrator integrator = new TrapezoidIntegrator(function);
        try {
            double sum = 0;
            result[0] = sum;
            for (int i = 0; i < y.length - 1; i++) {
                // System.out.println("integrate  "+x[i]+" "+x[i+2]);
                sum = sum + integrator.integrate(x[i], x[i + 1]);
                // System.out.println("integrate  "+sum);
                result[i + 1] = sum;
            }
            // return integrator.integrate(x[0], x[x.length-1]);
        } catch (final Exception e) {
            ExceptionUtil.throwProcessingException("impossible to integrate", integrator, e);
        }
        return result;
    }

    public static double linearInterpolation(final double intermediateVal, final double neighbor1Pos,
            final double neighbor2Pos, final double neighbor1Val, final double neighbor2Val) {
        final double a = (neighbor2Val - neighbor1Val) / (neighbor2Pos - neighbor1Pos);
        final double b = neighbor1Val - a * neighbor1Pos;
        return (intermediateVal - b) / a;
    }

    public static double dividedDifferenceInterpolation(final double xVal, final double x[], final double y[])
            throws ProcessingException {
        return Analysis.interpolate(new DividedDifferenceInterpolator(), xVal, x, y);
    }

    public static double splineInterpolation(final double xVal, final double x[], final double y[])
            throws ProcessingException {
        return Analysis.interpolate(new SplineInterpolator(), xVal, x, y);
    }

    public static double nevilleInterpolation(final double xVal, final double x[], final double y[])
            throws ProcessingException {
        return Analysis.interpolate(new NevilleInterpolator(), xVal, x, y);
    }

    private static double interpolate(final UnivariateRealInterpolator interpolator, final double xVal,
            final double x[], final double y[]) throws ProcessingException {
        Double value = null;
        UnivariateRealFunction function = null;
        try {
            function = interpolator.interpolate(x, y);
            logger.debug("interpolated by function " + function.getClass());
        } catch (final MathException e) {
            ExceptionUtil.throwProcessingException("impossible to create interpolator", interpolator, e);
        }
        try {
            if (function != null) {
                value = function.value(xVal);
            }

        } catch (final FunctionEvaluationException e) {
            ExceptionUtil.throwProcessingException("impossible to interpolate", interpolator, e);
        }

        return value;
    }

    public static double[] polynomialSplineDerivative(final double[] x, final double[] y) throws ProcessingException {
        final double[] result = new double[x.length];
        final UnivariateRealInterpolator interpolator = new SplineInterpolator();
        UnivariateRealFunction function = null;
        try {
            function = interpolator.interpolate(x, y);
            logger.debug("interpolated by function " + function.getClass());
        } catch (final MathException e) {
            ExceptionUtil.throwProcessingException("impossible to create interpolator", interpolator, e);
        }
        final UnivariateRealFunction derivateFunction = ((PolynomialSplineFunction) function).derivative();
        if(function != null){
        for (int i = 0; i < x.length; i++) {
                try {
                    result[i] = derivateFunction.value(x[i]);
                } catch (final FunctionEvaluationException e) {
                    ExceptionUtil.throwProcessingException("impossible to get derivative value", interpolator, e);
                }
            }
        }
        return result;
    }

    // f'(x)= (f(x+h)-f(x-h)) / (2h) en tout point f(x) defini (classe C1) pour
    // h->0.
    // f'(a) = (f(b)-f(a))/(b-a)
    //
    // 1- la derivee s'exprime de fa�on analytique ( ce qui est tout de m�me le
    // cas pour bien des fonctions )
    // 2- om ne peut l'exprmier ou on a f avec un jeu de points alors
    // a- si on peut fitter f avec une fonction derivable, on se ram�ne au point
    // 1
    // b- si non utiliser f'(x) = limite(e->0) ( f(x+e) - f(x-e) ) /2/e [ si f
    // est derivable a gauche et a droite].
    // Numeriquement cela demande un peu de precautions dans le cas on f est
    // donne par un jeu de points.
    // Le bruit amplifie nettement les derivees !!!.
    // meme sans modele, un fit lineaire ou quadratique autour du point
    // d'interet peut sensiblement ameliorer le resultat.
    public static double[] numericalDerivative(final double[] x, final double[] y) throws ProcessingException {
        final int size = x.length;
        if (x.length != y.length) {
            ExceptionUtil.throwProcessingException("input arrays must have the same size",Analysis.class.getName());
        }
        final double[] result = new double[size];
        result[0] = (y[1] - y[0]) / (x[1] - x[0]);
        for (int i = 1; i < size - 1; i++) {
            // result[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]);
            result[i] = (y[i + 1] - y[i - 1]) / (x[i + 1] - x[i - 1]);
        }
        result[size - 1] = (y[size - 1] - y[size - 2]) / (x[size - 1] - x[size - 2]);
        return result;
    }

    /*
     * case MATH_DERIVATIVE: for(int i=0;i<source.length-1;i++) { double d =
     * (source[i+1].y - source[i].y) / (source[i+1].x - source[i].x);
     * addInt(source[i].x, d); } break; case MATH_INTEGRAL: double sum = 0.0;
     * addInt(source[0].x, sum); for(int i=0;i<source.length-1;i++) { sum +=
     * ((source[i+1].y + source[i].y)/2.0) * (source[i+1].x - source[i].x);
     * addInt(source[i+1].x, sum); } break;
     */
}
