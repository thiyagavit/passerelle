package fr.soleil.passerelle.tango.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;

public final class FilterHelper {
	
	public static String[] getDevicesForPatternAsArray(String devicePattern) throws DevFailed {
		return FilterHelper.getDevicesForPattern(devicePattern)
			.toArray(new String[0]);
	}
	
	public static List<String> getDevicesForPattern(String devicePattern) throws DevFailed {
		Database db = ApiUtil.get_db_obj();
		String[] devices = db.get_device_name("*", "*");
		List<String> devicesToCheck = new ArrayList<String>();
		for (int i = 0; i < devices.length; i++) {
			//System.out.println("#####testing "+ devices[i]);
			Pattern pattern = Pattern.compile(
					FilterHelper.wildcardToRegex(devicePattern),
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(devices[i]);		
			if (matcher.matches()) {
				devicesToCheck.add(devices[i]);
				//System.out.println("!!!will check device "+devices[i]);
			}
		}
		return devicesToCheck;
	}

	public static String wildcardToRegex(String wildcard) {
	    StringBuffer s = new StringBuffer(wildcard.length());
	    s.append('^');
	    for (int i = 0, is = wildcard.length(); i < is; i++) {
	        char c = wildcard.charAt(i);
	        switch(c) {
	            case '*':
	                s.append(".*");
	                break;
	            case '?':
	                s.append(".");
	                break;
	                // escape special regexp-characters
	            case '(': case ')': case '[': case ']': case '$':
	            case '^': case '.': case '{': case '}': case '|':
	            case '\\':
	                s.append("\\");
	                s.append(c);
	                break;
	            default:
	                s.append(c);
	                break;
	        }
	    }
	    s.append('$');
	  //  System.out.println("regex: "+s.toString());
	    return s.toString();
	}
	
	  
    /*String test = "123ABC";
    System.out.println(test);
    System.out.println("#### testing 1*");
    System.out.println(Pattern.matches(wildcardToRegex("1*"), test));
    System.out.println("#### testing ?2*");
    System.out.println(Pattern.matches(wildcardToRegex("?2*"), test));
    System.out.println("#### testing ??2*");
    System.out.println(Pattern.matches(wildcardToRegex("??2*"), test));
    System.out.println("#### testing *A*");
    System.out.println(Pattern.matches(wildcardToRegex("*A*"), test));
    System.out.println("#### testing *Z*");
    System.out.println(Pattern.matches(wildcardToRegex("*Z*"), test));
    System.out.println("#### testing 123*");
    System.out.println(Pattern.matches(wildcardToRegex("123*"), test));
    System.out.println("#### testing 123");
    System.out.println(Pattern.matches(wildcardToRegex("123"), test));
    System.out.println("#### testing *ABC");
    System.out.println(Pattern.matches(wildcardToRegex("*ABC"), test));
    System.out.println("#### testing *abc");
    System.out.println(Pattern.matches(wildcardToRegex("*abc"), test));
    System.out.println("#### testing ABC*");
    System.out.println(Pattern.matches(wildcardToRegex("ABC*"), test));

	*/
}
