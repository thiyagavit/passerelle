package fr.soleil.passerelle.ptolemy.data.exp;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.PtParser;

import com.isencia.passerelle.util.StringUtils;

import fr.soleil.math.Analysis;
import fr.soleil.math.BeamCalculations;

public class SoleilFunctions {

    private final static Logger logger = LoggerFactory.getLogger(SoleilFunctions.class);

    public static void init() {
        PtParser.registerFunctionClass(SoleilFunctions.class.getName());
        // use static classes of commons-maths
        PtParser.registerFunctionClass(StatUtils.class.getName());
        PtParser.registerFunctionClass(MathUtils.class.getName());
        
        PtParser.registerFunctionClass(Analysis.class.getName());
        PtParser.registerFunctionClass(BeamCalculations.class.getName());
        // use static classes of commons.lang
        PtParser.registerFunctionClass(StringUtils.class.getName());
    }

    public static String test(final String test) {
        logger.debug("test - in");
        return test;
    }

    public static String test2(final Double[] test) {
        logger.debug("test2 - in");
        return test[0].toString();
    }
    
    
}
