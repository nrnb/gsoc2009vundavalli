package org.cytoscape.search.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RETest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String input = "node.canonicalName:900 AND edge.attr:\"val asdas\"";
		String input = "(node.canonicalName:900 AND edge.attr_name:\"val asdas\") OR name:sample";
		System.out.println("Initial Query:"+input);
		input = replacePattern(input, "node");
		input = replacePattern(input, "edge");		
		System.out.println("Final Query:"+input);
	}

	private static String replacePattern(String queryString, String identifier) {

		String pat = identifier + ".[\\S]*:";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(queryString);

		while (m.find()) {
			// System.out.println(m.pattern());
			//System.out.println("M.Group(): " + m.group());
			String str = m.group();
			String[] ss = str.split("\\.");
			
			int end = m.end();
			if (queryString.charAt(end) != '\"') {
				Pattern temp = Pattern.compile("[\\S]*");
				Matcher mt = temp.matcher(queryString.substring(end));

				//System.out.println(queryString.substring(end));
				if (mt.find()) {
					//System.out.println("Small Match:" + mt.group());
					String sm = mt.group();
					queryString = queryString.replace(sm, sm+" AND doctype:"+identifier+")");
				}
			} else {
				Pattern temp = Pattern.compile("[^\"]*");
				Matcher mt = temp.matcher(queryString.substring(end + 1));

				//System.out.println("Match2:" + queryString.substring(end));
				if (mt.find()) {
					//System.out.println("Small Match2:" + mt.group());
					String sm = mt.group();
					queryString = queryString.replace(sm+"\"", sm + "\" AND doctype:"+identifier+")");
				}
			}
			queryString = queryString.replace(str, "("+ss[1]);
			//System.out.println(queryString.charAt(end));
			// System.out.println(ss[1]);
		}
		return queryString;
	}

}
