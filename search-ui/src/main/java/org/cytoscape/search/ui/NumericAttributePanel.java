package org.cytoscape.search.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.model.CyDataTable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CyNetworkManager;

public class NumericAttributePanel extends BasicDraggablePanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JTextField jTextField = null;
	private JPanel attrPanel = null;

	private CyNetworkManager netmgr = null;
	private String attrName = null;
	private String type = null;
	private String valType = null;
	private String attrQuery = null; // @jve:decl-index=0:
	private NumberRangeModel rangeModel = null;
	private JRangeSliderExtended rangeSlider = null;
	private int minValue = 0, maxValue = 0;

	/**
	 * This is the default constructor
	 */
	public NumericAttributePanel(CyNetworkManager nm, String attrname,
			String attrType, String attrValType) {
		super();
		this.netmgr = nm;
		this.attrName = attrname;
		this.type = attrType;
		this.valType = attrValType;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 8, 5, 0);

		jLabel = new JLabel();
		if (type.equals("NODE")) {
			jLabel.setText(attrName + " [N]");
		} else {
			jLabel.setText(attrName + " [E]");
		}
		// jLabel.setText(attrName);
		jLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (attrPanel.isVisible()) {
					attrPanel.setVisible(false);
				} else {
					attrPanel.setVisible(true);
				}
			}
		});
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		attrPanel = new JPanel();
		attrPanel.setLayout(new GridBagLayout());
		attrPanel.setVisible(false);
		jTextField = this.getJTextField();
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 12, 4, 10);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 0;
		gc.gridwidth = GridBagConstraints.REMAINDER;
		gc.anchor = GridBagConstraints.EAST;
		attrPanel.add(jTextField, gc);
		// gc.gridx = 1;
		// gc.weightx = 1.0;
		// gc.fill = GridBagConstraints.HORIZONTAL;
		// attrPanel.add(Box.createHorizontalStrut(0), gc);
		//System.out.println("Value Type:" + valType);
		if (valType.equals("java.lang.Integer")) {
			List<Integer> l = getIntAttrValues();
			int[] values = new int[l.size()];
			for (int i = 0; i < l.size(); i++) {
				values[i] = l.get(i).intValue();
				//System.out.println("Numeric Attribute " + i + ":" + values[i]);
			}
			Arrays.sort(values);
			// minValue = NumberUtils.min(values);
			minValue = values[0];
			// maxValue = NumberUtils.max(values);
			maxValue = values[values.length - 1];
		} else if (valType.equals("java.lang.Double")) {
			List<Double> l1 = getDoubleAttrValues();
			double[] values1 = new double[l1.size()];
			for (int i = 0; i < l1.size(); i++) {
				values1[i] = l1.get(i).doubleValue();
				//System.out.println("Numeric Attribute " + i + ":" + values1[i]);
			}
			Arrays.sort(values1);
			// minValue = (int) NumberUtils.min(values1);
			minValue = (int) values1[0];
			// maxValue = ((int) NumberUtils.max(values1)) + 1;
			maxValue = (int) values1[values1.length - 1] + 1;
		}

		rangeModel = new NumberRangeModel(minValue, maxValue, minValue,
				maxValue);
		rangeSlider = new JRangeSliderExtended(netmgr, rangeModel,
				JRangeSlider.HORIZONTAL, JRangeSlider.LEFTRIGHT_TOPBOTTOM);
		rangeSlider.setPreferredSize(new Dimension(150, 15));
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 1;
		gc.fill = GridBagConstraints.HORIZONTAL;
		attrPanel.add(rangeSlider, gc);

		GridBagConstraints gg = new GridBagConstraints();
		gg.fill = GridBagConstraints.HORIZONTAL;
		gg.gridwidth = GridBagConstraints.REMAINDER;
		gg.weightx = 1.0;

		this.add(attrPanel, gg);
		//rangeSlider.initPopup(new Integer(minValue).toString(), new Integer(
			//	maxValue).toString());

	}

	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(40, 20));

			jTextField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String term = attrName + ":" + jTextField.getText();
					if (attrQuery == null) {
						attrQuery = term;
					} else {
						attrQuery = attrQuery + " OR " + term;
					}
					SearchPanelFactory.getGlobalInstance(netmgr)
							.updateSearchField();
					jTextField.setText(null);
				}
			});

			jTextField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					String text = jTextField.getText();
					if (text != null && !text.equals("")) {
						String term = attrName + ":" + text;
						if (attrQuery == null) {
							attrQuery = term;
						} else {
							attrQuery = attrQuery + " OR " + term;

						}
						SearchPanelFactory.getGlobalInstance(netmgr)
								.updateSearchField();
						jTextField.setText(null);
					}
				}
			});
		}
		return jTextField;
	}

	public List<Integer> getIntAttrValues() {
		final CyNetwork network = netmgr.getCurrentNetwork();
		CyDataTable datatable = network.getCyDataTables(type).get(
				CyNetwork.DEFAULT_ATTRS);
		List<Integer> l = datatable.getColumnValues(attrName, Integer.class);
		return l;
	}

	public List<Double> getDoubleAttrValues() {
		final CyNetwork network = netmgr.getCurrentNetwork();
		CyDataTable datatable = network.getCyDataTables(type).get(
				CyNetwork.DEFAULT_ATTRS);
		List<Double> l = datatable.getColumnValues(attrName, Double.class);
		return l;
	}

	public String getQuery() {
		String complete = null;
		if (getQueryFromBox() != null) {
			complete = getQueryFromBox();
		}
		if (rangeQuery() != null) {
			if (complete != null) {
				complete = complete.substring(0, complete.lastIndexOf(")"))
						+ " OR " + rangeQuery() + ")";
			} else {
				complete = "(" + rangeQuery() + ")";
			}
		}
		return complete;
	}

	public String getQueryFromBox() {
		String res = "(";
		if (attrQuery != null) {
			res = res + attrQuery + ")";
			return res;
		} else
			return null;
	}

	public String rangeQuery() {
		if (rangeSlider.query != null) {
			return attrName + ":[" + rangeSlider.query + "]";
		} else
			return null;
	}

	public void clearAll() {
		jTextField.setText(null);
		attrQuery = null;
		rangeSlider.query = null;
		rangeSlider.setRange(minValue, maxValue);
		// rangeSlider.resetPopup();
	}

}
