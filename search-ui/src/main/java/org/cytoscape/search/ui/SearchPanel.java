package org.cytoscape.search.ui;

import javax.swing.JPanel;

import org.cytoscape.work.TaskManager;

public abstract class SearchPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	abstract public void performSearch(boolean reindex);

	abstract public void updateSearchField();

	abstract public void clearAll();

	abstract public RootPanel getattrPanel();
	
	abstract public MainPanel getmainPanel();

	abstract public void initattrPanel();
	
	abstract public void setTaskManager(TaskManager tm);

}
