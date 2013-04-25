package fr.soleil.passerelle.actor.flow;


public class ComparatorHelper {

	public enum ComparisonNature {
		/**
		 * less than
		 */
		LT,
		/**
		 * less or equals
		 */
		LE,
		/**
		 * greater than
		 */
		GE,
		/**
		 * greater than
		 */
		GT,
		/**
		 * equals
		 */
		EQ,
		/**
		 * not equals
		 */
		NE
	};

	public enum ComparisonType {
		STRING(0), BOOLEAN(1), DOUBLE(2);

		private final int value;

		ComparisonType(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};

	public static boolean compare(final String leftConst,
			final String rightConst, final ComparisonNature comparison,
			final double tolerance) {
		final ComparisonType compType = ComparatorHelper.getComparisonType(
				leftConst, rightConst);

		boolean comparisonOK = false;
		switch (compType) {
		case DOUBLE:
			final double[] values = ComparatorHelper.getDoubleValues(leftConst,
					rightConst);
			comparisonOK = ComparatorHelper.compareDouble(values[0], values[1],
					ComparisonNature.EQ, tolerance);
			break;
		case STRING:
			comparisonOK = ComparatorHelper.compareString(leftConst,
					rightConst, ComparisonNature.EQ);
			break;
		case BOOLEAN:
			final double[] values2 = ComparatorHelper.getDoubleValues(
					leftConst, rightConst);
			comparisonOK = ComparatorHelper.compareDouble(values2[0],
					values2[1], ComparisonNature.EQ);
			break;
		}
		return comparisonOK;
	}

	public static boolean compareDouble(final double leftValue,
			final double rightValue, final ComparisonNature comparison,
			final double tolerance) {
		boolean result = false;
		switch (comparison) {
		case GT:
			if (leftValue + tolerance > rightValue) {
				result = true;
			}
			break;
		case GE:
			if (leftValue + tolerance >= rightValue) {
				result = true;
			}
			break;
		case LT:
			if (leftValue < rightValue + tolerance) {
				result = true;
			}
			break;
		case LE:
			if (leftValue <= rightValue + tolerance) {
				result = true;
			}
			break;
		case EQ:
			if (leftValue <= rightValue + tolerance
					&& leftValue >= rightValue - tolerance) {
				result = true;
			}
			break;
		case NE:
			if (leftValue > rightValue + tolerance
					|| leftValue < rightValue - tolerance) {
				result = true;
			}
			break;
		default:
			// this is a quite dramatic inconsistency, so better to fail
			// fast
			throw new IllegalArgumentException(
					"Invalid value for _comparison private variable on comparison type "
							+ comparison);
		}
		return result;
	}

	public static boolean compareDouble(final double leftValue,
			final double rightValue, final ComparisonNature comparison) {
		boolean result = false;
		switch (comparison) {
		case GT:
			if (leftValue > rightValue) {
				result = true;
			}
			break;
		case GE:
			if (leftValue >= rightValue) {
				result = true;
			}
			break;
		case LT:
			if (leftValue < rightValue) {
				result = true;
			}
			break;
		case LE:
			if (leftValue <= rightValue) {
				result = true;
			}
			break;
		case EQ:
			if (leftValue == rightValue) {
				result = true;
			}
			break;
		case NE:
			if (leftValue != rightValue) {
				result = true;
			}
			break;
		default:
			// this is a quite dramatic inconsistency, so better to fail
			// fast
			throw new IllegalArgumentException(
					"Invalid value for _comparison private variable on comparison type "
							+ comparison);
		}
		return result;
	}

	public static boolean compareString(final String leftConst,
			final String rightConst, final ComparisonNature comparison) {
		boolean result = false;
		switch (comparison) {
		case GT:
			if (leftConst.compareToIgnoreCase(rightConst) > 0) {
				result = true;
			}
			break;
		case GE:
			if (leftConst.compareToIgnoreCase(rightConst) >= 0) {
				result = true;
			}
			break;
		case LT:
			if (leftConst.compareToIgnoreCase(rightConst) < 0) {
				result = true;
			}
			break;
		case LE:
			if (leftConst.compareToIgnoreCase(rightConst) <= 0) {
				result = true;
			}
			break;
		case EQ:
			if (leftConst.compareToIgnoreCase(rightConst) == 0) {
				result = true;
			}
			break;
		case NE:
			if (leftConst.compareToIgnoreCase(rightConst) != 0) {
				result = true;
			}
			break;
		default:
			// this is a quite dramatic inconsistency, so better to fail
			// fast
			throw new IllegalArgumentException(
					"Invalid value for _comparison private variable on comparison type "
							+ comparison);
		}
		return result;
	}

	/**
	 * 
	 * @param leftConst
	 * @param rightConst
	 * @return 0=string, 1= boolean, 2= double
	 */
	public static ComparisonType getComparisonType(final String leftConst,
			final String rightConst) {
		ComparisonType comparisonType = ComparisonType.DOUBLE;
		ComparisonType compTypeLeft = ComparisonType.DOUBLE;
		ComparisonType compTypeRight = ComparisonType.DOUBLE;
		try {
			Double.valueOf(leftConst);
			compTypeLeft = ComparisonType.DOUBLE;
		} catch (final NumberFormatException e) {
			try {
				if (leftConst.compareToIgnoreCase("true") == 0
						|| leftConst.compareToIgnoreCase("false") == 0) {
					Boolean.valueOf(leftConst);
					compTypeLeft = ComparisonType.BOOLEAN;
				} else {
					compTypeLeft = ComparisonType.STRING;
				}
			} catch (final NumberFormatException e2) {
				compTypeLeft = ComparisonType.STRING;
			}
		}
		// System.out.println("compTypeLeft is a "+compTypeLeft);
		try {
			Double.valueOf(rightConst);
			compTypeRight = ComparisonType.DOUBLE;
		} catch (final NumberFormatException e) {
			try {
				if (rightConst.compareToIgnoreCase("true") == 0
						|| rightConst.compareToIgnoreCase("false") == 0) {
					Boolean.valueOf(rightConst);
					compTypeRight = ComparisonType.BOOLEAN;
				} else {
					compTypeRight = ComparisonType.STRING;
				}
			} catch (final NumberFormatException e2) {
				compTypeRight = ComparisonType.STRING;
			}
		}
		// System.out.println("compTypeRight is a "+compTypeRight);
		if (compTypeLeft.getValue() > compTypeRight.getValue()) {
			comparisonType = compTypeRight;
		} else {
			comparisonType = compTypeLeft;
		}

		// System.out.println("comparison type: " + comparisonType);
		return comparisonType;
	}

	public static double[] getDoubleValues(final String leftConst,
			final String rightConst) {
		final double[] values = new double[2];
		if (leftConst.equalsIgnoreCase("true")
				|| leftConst.equalsIgnoreCase("false")) {
			if (Boolean.parseBoolean(leftConst)) {
				values[0] = 1;
			} else {
				values[0] = 0;
			}
		} else {
			values[0] = Double.parseDouble(leftConst);
		}
		if (rightConst.equalsIgnoreCase("true")
				|| rightConst.equalsIgnoreCase("false")) {
			if (Boolean.parseBoolean(rightConst)) {
				values[1] = 1;
			} else {
				values[1] = 0;
			}
		} else {
			values[1] = Double.parseDouble(rightConst);
		}
		return values;
	}
}
