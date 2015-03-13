package org.cytoscape.search.ui.tasks;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;

import java.util.Collection;

public abstract class SelectUtils {

	public static void setSelectedNodes(Collection<CyNode> nodes, boolean select) {
		for (CyNode n : nodes) {
			n.attrs().set("selected", select);
		}
	}

	public static void setSelectedEdges(Collection<CyEdge> edges, boolean select) {
		for (CyEdge e : edges) {
			e.attrs().set("selected", select);
		}
	}

}
