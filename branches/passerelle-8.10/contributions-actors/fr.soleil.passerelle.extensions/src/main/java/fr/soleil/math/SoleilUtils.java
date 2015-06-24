package fr.soleil.math;


public class SoleilUtils {
//	protected static final Logger logger = LoggerFactory
//			.getLogger(SoleilUtils.class);
//
//	public static class ResultItemByDateComparator implements
//			Comparator<ResultItem> {
//
//		public int compare(ResultItem o1, ResultItem o2) {
//			return o1.getCreationTS().compareTo(o2.getCreationTS());
//		}
//	}
//
//	public static boolean checkDerivative(Collection<ResultItem> events,
//			String operator, double threshold, Actor actor)
//			throws ProcessingException {
//		double[] x = new double[events.size()];
//		double[] y = new double[events.size()];
//		int i = 0;
//		ExecutionTracerService.trace(actor, "number of events : "
//				+ events.size());
//		if (events.size() < 3) {
//			return true;
//		}
//		List<ResultItem> list = new ArrayList<ResultItem>();
//		for (ResultItem event : events) {
//			if (event.getCreationTS() != null) {
//				list.add(event);
//			}
//		}
//		Collections.sort(list, new ResultItemByDateComparator());
//		for (ResultItem event : list) {
//			x[i] = event.getCreationTS().getTime();
//			y[i++] = event.getDoubleValue();
//		}
//		double[] derivative = Analysis.polynomialSplineDerivative(x, y);
//		for (double der : derivative) {
//			if (executeLogicalOperator(operator, der, threshold)) {
//				ExecutionTracerService.trace(actor, "derivative supo "
//						+ threshold);
//
//				return false;
//			}
//		}
//
//		return true;
//
//	}
//
//	public static boolean checkDerivative(Collection<ResultItem> events,
//			double threshold, Actor actor) throws ProcessingException {
//		return checkDerivative(events, ">", threshold, actor);
//	}
//
//	public static boolean executeLogicalOperator(String operator, double n0,
//			double n1) {
//		if (operator.equals(">")) {
//			return n0 > n1;
//		} else if (operator.equals(">=")) {
//			return n0 >= n1;
//		} else if (operator.equals("<=")) {
//			return n0 <= n1;
//		} else if (operator.equals("<")) {
//			return n0 < n1;
//		} else if (operator.equals("!=")) {
//			return n0 != n1;
//		} else if (operator.equals("==")) {
//			return n0 == n1;
//		} else {
//			return false;
//		}
//
//	}
}
