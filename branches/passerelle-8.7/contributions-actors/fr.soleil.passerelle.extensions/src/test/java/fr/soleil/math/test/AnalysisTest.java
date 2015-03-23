package fr.soleil.math.test;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.isencia.passerelle.actor.ProcessingException;

import fr.soleil.math.Analysis;
import fr.soleil.math.BeamCalculations;

public class AnalysisTest {

    @Test
    public void fwhm() {

        final BeamProfiles functions = new BeamProfiles();

        final double expectedFWHM = 10;
        final double[] l = functions.createLorentzian(100, expectedFWHM, 50);
        //System.out.println("l = " + ArrayUtils.toString(l));
        /*
         * TangoAttribute attr; try { attr = new
         * TangoAttribute("tango/tangotest/1/double_spectrum");
         * attr.convertFromDoubleArrayAndInsert(l); attr.write(); } catch
         * (DevFailed e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        //
        //System.out.println("line = " + ArrayUtils.toString(functions.createLine(100)));
        //org.apache.commons.lang.ArrayUtils.indexOf(array, valueToFind)
        double result = 0;
        try {
            result = BeamCalculations.fwhm(functions.createLine(100), l);
        } catch (ProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("fwhm posistion is " + result);
        AssertJUnit.assertEquals(expectedFWHM, result, 0.1);

    }

    @Test
    public void centroid() {
        final BeamProfiles functions = new BeamProfiles();
        final double[] x = functions.createLine(250);
        final double expectedCentroid = 186.98740644484573;
        final double[] y = functions.createSigmoid(250, 1);

        /*
         * System.setProperty("TANGO_HOST", "calypso:20001"); TangoAttribute
         * attr; try { attr = new
         * TangoAttribute("tango/tangotest/1/double_spectrum");
         * //attr.convertFromDoubleArrayAndInsert(s); //attr.write(); } catch
         * (DevFailed e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        final double result = BeamCalculations.centroidPosition(x, y);
        System.out.println("centroid position is " + result);
        AssertJUnit.assertEquals(expectedCentroid, result, 0.1);

    }

    @Test
    public void numericalDerivative() {
        final BeamProfiles functions = new BeamProfiles();
        final double[] x = functions.createLine(250);
        final double[] y = functions.createSigmoid(250, 1);

        /*
         * System.setProperty("TANGO_HOST", "calypso:20001"); TangoAttribute
         * attr; try { attr = new
         * TangoAttribute("tango/tangotest/1/double_spectrum");
         * //attr.convertFromDoubleArrayAndInsert(s); //attr.write(); } catch
         * (DevFailed e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        try {
            Analysis.numericalDerivative(x, y);

        } catch (final ProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // public static void main(final String args[]) {
    // System.setProperty("TANGO_HOST", "calypso:20001");
    // final BeamProfiles functions = new BeamProfiles();
    // TangoAttribute attr = null;
    // final double[] x = functions.createLine(250);
    // final double[] y = functions.createSigmoid(250, 0.05);
    // // double[] y = functions.createLorentzian(250, 25, 50);
    // try {
    // attr = new TangoAttribute("tango/tangotest/1/double_spectrum");
    // attr.insertSpectrum(y);
    // attr.write();
    // } catch (final DevFailed e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // try {
    // Thread.sleep(5000);
    // } catch (final InterruptedException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // try {
    // final double[] d = Analysis.numericalDerivative(x, y);
    // attr.insertSpectrum(d);
    // attr.write();
    //
    // try {
    // Thread.sleep(5000);
    // } catch (final InterruptedException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // // double[] t = Analysis.trapezoidIntegrale(x, y);
    // final double[] t = Analysis.polynomialSplineDerivative(x, y);
    // attr.insertSpectrum(t);
    // attr.write();
    // } catch (final ProcessingException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (final DevFailed e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
}
