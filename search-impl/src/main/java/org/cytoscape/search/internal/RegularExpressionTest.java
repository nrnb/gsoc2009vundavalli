package org.cytoscape.search.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpressionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String query = new String();
		String input = "node.canonicalName:900 AND edge.attr:val";
		Pattern p = Pattern.compile("node.[\\S]*:[\\S]*");
		Matcher m = p.matcher(input);
		while (m.find()) {
			//System.out.println(m.pattern());
			System.out.println("M.Group(): "+ m.group());
			String str = m.group();
			String[] ss = str.split("\\.");
			System.out.println(ss[1]);
			query = "("+ss[1]+ " AND docType:"+ ss[0]+")";
			input = input.replaceAll(str, query);
		}
		System.out.println(input);
	}

}
