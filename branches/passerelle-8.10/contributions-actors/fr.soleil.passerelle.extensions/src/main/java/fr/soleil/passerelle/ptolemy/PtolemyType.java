package fr.soleil.passerelle.ptolemy;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

public enum PtolemyType {
    BOOLEAN(BaseType.BOOLEAN), INT(BaseType.INT), SHORT(BaseType.SHORT), LONG(BaseType.LONG), DOUBLE(
	    BaseType.DOUBLE), STRING(BaseType.STRING), FUNCTION(new FunctionType(
	    new Type[] { BaseType.UNKNOWN }, BaseType.UNKNOWN)), COMPLEX(BaseType.COMPLEX), ARRAY(
	    new ArrayType(BaseType.UNKNOWN)), BOOLEANMATRIX(BaseType.BOOLEAN_MATRIX), DOUBLEMATRIX(
	    BaseType.DOUBLE_MATRIX), INTMATRIX(BaseType.INT_MATRIX), LONGMATRIX(
	    BaseType.LONG_MATRIX);
    // TODO COMPLEXMATRIC

    private static final Map<Type, PtolemyType> typesMap = new HashMap<Type, PtolemyType>();
    static {
	for (final PtolemyType s : EnumSet.allOf(PtolemyType.class)) {
	    typesMap.put(s.getType(), s);
	}
    }

    Type type;

    public Type getType() {
	return type;
    }

    PtolemyType(final Type type) {
	this.type = type;
    }

    public static PtolemyType getPtolemyType(final Type type) {
	return typesMap.get(type);
    }

    public Token getTokenForString(final String value) throws IllegalActionException {
	if (this == PtolemyType.BOOLEAN) {
	    // conversion is forced
	    final double d = Double.parseDouble(value);
	    boolean val = true;
	    if (d == 0) {
		val = false;
	    }
	    return new BooleanToken(val);
	} else if (this == PtolemyType.INT) {
	    // conversion is forced
	    final int val = (int) Double.parseDouble(value);
	    return new IntToken(val);
	} else if (this == PtolemyType.SHORT) {
	    // conversion is forced
	    final short val = (short) Double.parseDouble(value);
	    return new ShortToken(val);
	} else if (this == PtolemyType.LONG) {
	    // conversion is forced
	    final long val = (long) Double.parseDouble(value);
	    return new LongToken(val);
	} else if (this == PtolemyType.DOUBLE) {
	    return new DoubleToken(value);
	} else if (this == PtolemyType.STRING) {
	    return new StringToken(value);
	} else if (this == PtolemyType.FUNCTION) {
	    return new FunctionToken(value);
	} else if (this == PtolemyType.COMPLEX) {
	    return new ComplexToken(value);
	} else if (this == PtolemyType.ARRAY) {
	    if (value.startsWith("{") && value.endsWith("}")) {
		return new ArrayToken(value);
	    } else if (value.startsWith("[") && value.endsWith("]")) {
		String value2 = value.replace("[", "{");
		value2 = value2.replace("]", "}");
		return new ArrayToken(value2);
	    } else {
		return new ArrayToken("{" + value + "}");
	    }
	} else if (this == PtolemyType.BOOLEANMATRIX) {
	    if (value.startsWith("[") && value.endsWith("]")) {
		return new BooleanMatrixToken(value);
	    } else {
		return new BooleanMatrixToken("[" + value + "]");
	    }
	} else if (this == PtolemyType.DOUBLEMATRIX) {
	    if (value.startsWith("[") && value.endsWith("]")) {
		return new DoubleMatrixToken(value);
	    } else {
		return new DoubleMatrixToken("[" + value + "]");
	    }
	} else if (this == PtolemyType.INTMATRIX) {
	    if (value.startsWith("[") && value.endsWith("]")) {
		return new IntMatrixToken(value);
	    } else {
		return new IntMatrixToken("[" + value + "]");
	    }
	} else if (this == PtolemyType.LONGMATRIX) {
	    if (value.startsWith("[") && value.endsWith("]")) {
		return new LongMatrixToken(value);
	    } else {
		return new LongMatrixToken("[" + value + "]");
	    }
	}
	// TODO: all types
	return null;
    }

    // TODO: matrix
    public static String getStringForToken(final Token token) {
	if (token == null) {
	    return null;
	}
	String s = "";
	// MATRIX
	if (token.getType().getTokenClass() == BooleanMatrixToken.class) {
	    final boolean[][] out = ((BooleanMatrixToken) token).booleanMatrix();
	    for (int i = 0; i < out.length; i++) {
		for (int j = 0; j < out[i].length; j++) {
		    s = s + out[i][j];
		    if (j < out[i].length) {
			s += ",";
		    }
		}
		if (i < out.length) {
		    s += ";";
		}
	    }
	} else if (token.getType().getTokenClass() == DoubleMatrixToken.class) {
	    final double[][] out = ((DoubleMatrixToken) token).doubleMatrix();
	    for (int i = 0; i < out.length; i++) {
		for (int j = 0; j < out[i].length; j++) {
		    s = s + out[i][j];
		    if (j < out[i].length) {
			s += ",";
		    }
		}
		if (i < out.length) {
		    s += ";";
		}
	    }
	} else if (token.getType().getTokenClass() == IntMatrixToken.class) {
	    final int[][] out = ((IntMatrixToken) token).intMatrix();
	    for (int i = 0; i < out.length; i++) {
		for (int j = 0; j < out[i].length; j++) {
		    s = s + out[i][j];
		    if (j < out[i].length) {
			s += ",";
		    }
		}
		if (i < out.length) {
		    s += ";";
		}
	    }
	} else if (token.getType().getTokenClass() == LongMatrixToken.class) {
	    final long[][] out = ((LongMatrixToken) token).longMatrix();
	    for (int i = 0; i < out.length; i++) {
		for (int j = 0; j < out[i].length; j++) {
		    s = s + out[i][j];
		    if (j < out[i].length) {
			s += ",";
		    }
		}
		if (i < out.length) {
		    s += ";";
		}
	    }
	}// ARRAY
	else if (token.getType().getTokenClass() == ArrayToken.class) {
	    final Token[] arrayToken = ((ArrayToken) token).arrayValue();
	    if (arrayToken != null) {
		if (arrayToken[0].getType().getTokenClass() == DoubleToken.class) {
		    final Double[] out = new Double[arrayToken.length];
		    for (int i = 0; i < arrayToken.length; i++) {
			out[i] = ((DoubleToken) arrayToken[i]).doubleValue();
			s = s + out[i];
			if (i < arrayToken.length - 1) {
			    s = s + ",";
			}
		    }
		}/*
		  * TODO: not sure that is operation is sufficient to convert to
		  * real else if(dT[0].getType().getTokenClass()
		  * ==ComplexToken.class) { Double[] out = new
		  * Double[dT.length]; String s = ""; for (int i = 0; i <
		  * dT.length; i++) { out[i] =
		  * ((ComplexToken)dT[i]).complexValue().real; s = s+out[i];
		  * if(i < dT.length -1) s=s+","; }
		  * passerelleMsg.setBodyContent(s,ManagedMessage
		  * .objectContentType); }
		  */else if (arrayToken[0].getType().getTokenClass() == StringToken.class) {
		    for (int i = 0; i < arrayToken.length; i++) {
			s = s + ((StringToken) arrayToken[i]).stringValue();
			if (i < arrayToken.length - 1) {
			    s = s + ",";
			}
		    }
		} else if (arrayToken[0].getType().getTokenClass() == LongToken.class) {
		    final Long[] out = new Long[arrayToken.length];
		    for (int i = 0; i < arrayToken.length; i++) {
			out[i] = ((LongToken) arrayToken[i]).longValue();
			s = s + out[i];
			if (i < arrayToken.length - 1) {
			    s = s + ",";
			}
		    }
		} else if (arrayToken[0].getType().getTokenClass() == IntToken.class) {
		    final Integer[] out = new Integer[arrayToken.length];
		    for (int i = 0; i < arrayToken.length; i++) {
			out[i] = ((IntToken) arrayToken[i]).intValue();
			s = s + out[i];
			if (i < arrayToken.length - 1) {
			    s = s + ",";
			}
		    }
		} else if (arrayToken[0].getType().getTokenClass() == BooleanToken.class) {
		    final Boolean[] out = new Boolean[arrayToken.length];
		    for (int i = 0; i < arrayToken.length; i++) {
			out[i] = ((BooleanToken) arrayToken[i]).booleanValue();
			s = s + out[i];
			if (i < arrayToken.length - 1) {
			    s = s + ",";
			}
		    }
		}
	    }
	} else if (token.getType().getTokenClass() == BooleanToken.class) {
	    s = Boolean.toString(((BooleanToken) token).booleanValue());
	} else if (token.getType().getTokenClass() == IntToken.class) {
	    s = Integer.toString(((IntToken) token).intValue());
	} else if (token.getType().getTokenClass() == LongToken.class) {
	    s = Long.toString(((LongToken) token).longValue());
	} else if (token.getType().getTokenClass() == DoubleToken.class) {
	    s = Double.toString(((DoubleToken) token).doubleValue());
	} else if (token.getType().getTokenClass() == StringToken.class) {
	    s = ((StringToken) token).stringValue();
	} else if (token.getType().getTokenClass() == FunctionToken.class) {
	    s = ((FunctionToken) token).toString();
	}// TODO: complex
	return s;
    }
}
