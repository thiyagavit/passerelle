package fr.soleil.math;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.diagnosis.Event;
import com.isencia.passerelle.diagnosis.ResultItem;
import com.isencia.passerelle.util.ExecutionTracerService;

public class SoleilUtils {
	protected static final Logger logger = LoggerFactory.getLogger(SoleilUtils.class);


	
	public static boolean checkDerivative( Collection<ResultItem> events,double threshold,Actor actor) throws ProcessingException{
		double[] x = new double[events.size()];
		double[] y = new double[events.size()];
		int i=0;
		ExecutionTracerService.trace(actor,"number of events : " + events.size());
		
		if (events.size() < 3){
			return true;
		}
		
		for (ResultItem event : events) {
			x[i]=event.getCreationTS().getTime();
			y[i++]=event.getDoubleValue();
		}
		double[] derivative = Analysis.polynomialSplineDerivative(x, y);
		for (double der : derivative){
			if (der > threshold){
				ExecutionTracerService.trace(actor,"derivative supo " + threshold);

				return false;
			}
		}
		
		return true;
	}
}
