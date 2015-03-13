/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.cytoscape.search.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;

import org.cytoscape.search.EnhancedSearchIndex;
import org.cytoscape.search.EnhancedSearchQuery;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.search.util.EnhancedSearchUtils;
import org.cytoscape.search.util.CustomMultiFieldQueryParser;
import org.cytoscape.search.util.AttributeFields;

public class EnhancedSearchQueryImpl extends EnhancedSearchQuery {

	private IdentifiersCollector hitCollector = null;
	private ArrayList<CyNode> nodelist = null;
	private ArrayList<CyEdge> edgelist = null;

	public EnhancedSearchQueryImpl(RAMDirectory index, CyNetwork net) {
		super(index, net);
	}

	public void executeQuery(String queryString) {
		try {

			// Define attribute fields in which the search is to be carried on
			AttributeFields attFields = new AttributeFields(network);
			/*
			 * String[] fields = attFields.getFields(); for(int
			 * i=0;i<fields.length;i++){
			 * System.out.println("Field "+i+":"+fields[i] ); }
			 */
			// Build an IndexSearcher using the in-memory index
			searcher = new IndexSearcher(idx);
			//System.out.println("Query Before- " + queryString);

			queryString = replacePattern(queryString, "node");
			queryString = replacePattern(queryString, "edge");

			// queryString = EnhancedSearchUtils.replaceWhitespace(queryString);
			queryString = EnhancedSearchUtils.queryToLowerCase(queryString);
			//System.out.println("Query After- " + queryString);
			search(searcher, queryString, attFields);
			searcher.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 
	 * @param queryString
	 * @param identifier
	 * @return
	 */
	private static String replacePattern(String queryString, String identifier) {

		String pat = identifier + ".[\\S]*:";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(queryString);

		while (m.find()) {
			// System.out.println(m.pattern());
			// System.out.println("M.Group(): " + m.group());
			String str = m.group();
			String[] ss = str.split("\\.");

			int end = m.end();
			if (queryString.charAt(end) != '\"') {
				Pattern temp = Pattern.compile("[\\S]*");
				Matcher mt = temp.matcher(queryString.substring(end));

				// System.out.println(queryString.substring(end));
				if (mt.find()) {
					// System.out.println("Small Match:" + mt.group());
					String sm = mt.group();
					queryString = queryString.replace(sm, sm + " AND doctype:"
							+ identifier + ")");
				}
			} else {
				Pattern temp = Pattern.compile("[^\"]*");
				Matcher mt = temp.matcher(queryString.substring(end + 1));

				// System.out.println("Match2:" + queryString.substring(end));
				if (mt.find()) {
					// System.out.println("Small Match2:" + mt.group());
					String sm = mt.group();
					queryString = queryString.replace(sm + "\"", sm
							+ "\" AND doctype:" + identifier + ")");
				}
			}
			queryString = queryString.replace(str, "(" + ss[1]);
			// System.out.println(queryString.charAt(end));
			// System.out.println(ss[1]);
		}
		return queryString;
	}

	/**
	 * Searches for the given query string. By default (without specifying
	 * attributeName), search is carried out on all attribute fields. This
	 * functionality is enabled with the use of MultiFieldQueryParser.
	 */
	private void search(Searcher searcher, String queryString,
			AttributeFields attFields) throws IOException {

		// Build a Query object.
		// CustomMultiFieldQueryParser is used to support range queries on
		// numerical attribute fields.
		CustomMultiFieldQueryParser queryParser = new CustomMultiFieldQueryParser(
				attFields, new StandardAnalyzer());

		try {
			//System.out.println("Before Calling Parse:" + queryString);
			// Execute query
			Query query = queryParser.parse(queryString);
			System.out.println("ESQuery :" + query.toString());
			hitCollector = new IdentifiersCollector(searcher);
			searcher.search(query, hitCollector);
		} catch (ParseException pe) {
			// Parse exceptions occur when colon appear in the query in an
			// unexpected location, e.g. when attribute or value are
			// missing in the query. In such case, the hitCollector
			// variable will be null.
			System.out.println("Invalid query '" + queryString + "'");
			String message = pe.getMessage();
			System.out.println(message);
		} catch (Exception e) {
			// Other types of exception may occur
			System.out.println("Error during execution of query '"
					+ queryString + "'");
			String message = e.getMessage();
			System.out.println(message);
		}
	}

	// hitCollector object may be null if this method is called before
	// executeQuery
	public int getHitCount() {
		if (hitCollector != null) {
			return hitCollector.getHitCount();
		} else {
			return 0;
		}
	}

	// hitCollector object may be null if this method is called before
	// ExecuteQuery
	/*
	 * private ArrayList<Document> getHits() { if (hitCollector != null) {
	 * return hitCollector.getHits(); } else { return null; } }
	 */

	private void parseHits() {
		ArrayList<Document> al = hitCollector.getHits();
		Iterator<Document> it = al.iterator();
		nodelist = new ArrayList<CyNode>();
		edgelist = new ArrayList<CyEdge>();
		while (it.hasNext()) {
			Document currdoc = (Document) it.next();
			String type = currdoc.get("doctype");
			if (type.equals("node")) {
				CyNode currNode = network.getNode((new Integer(currdoc
						.get(EnhancedSearchIndex.INDEX_FIELD))).intValue());
				if (currNode != null) {
					nodelist.add(currNode);
				}
			} else if (type.equals("edge")) {
				CyEdge currEdge = network.getEdge((new Integer(currdoc
						.get(EnhancedSearchIndex.INDEX_FIELD))).intValue());
				if (currEdge != null) {
					edgelist.add(currEdge);
				}
			}
		}

	}

	public ArrayList<CyNode> getNodeHits() {
		if (hitCollector != null) {
			if (nodelist == null) {
				parseHits();
			}
			return nodelist;
		} else {
			return null;
		}
	}

	public ArrayList<CyEdge> getEdgeHits() {
		if (hitCollector != null) {
			if (edgelist == null)
				parseHits();
			return edgelist;
		} else {
			return null;
		}
	}
}

class IdentifiersCollector extends HitCollector {

	public static final String INDEX_FIELD = "id";

	private Searcher searcher;

	// public ArrayList<String> hitsIdentifiers = new ArrayList<String>();
	public ArrayList<Document> hitsIdentifiers = new ArrayList<Document>();

	public IdentifiersCollector(Searcher searcher) {
		this.searcher = searcher;
	}

	public void collect(int id, float score) {
		try {
			Document doc = searcher.doc(id);
			// String currID = doc.get(INDEX_FIELD);
			// hitsIdentifiers.add(currID);
			hitsIdentifiers.add(doc);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public int getHitCount() {
		return hitsIdentifiers.size();
	}

	/*
	 * public ArrayList<String> getHits() { return hitsIdentifiers; }
	 */
	public ArrayList<Document> getHits() {
		return hitsIdentifiers;
	}

}
