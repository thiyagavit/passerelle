package fr.soleil.passerelle.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import fr.soleil.comete.definition.widget.IChartViewer;
import fr.soleil.comete.swing.Chart;

@SuppressWarnings("serial")
public class AttributeMultipleSpectrumPanel extends javax.swing.JPanel {
    private Chart widget;

    public AttributeMultipleSpectrumPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        widget = new Chart();

        widget.setAutoHighlightOnLegend(true);
        widget.setFreezePanelVisible(false);
        widget.setManagementPanelVisible(true);
        widget.setMathExpressionEnabled(true);
        widget.setCyclingCustomMap(false);
       
        // try {

        setPreferredSize(new Dimension(400, 300));
        BorderLayout thisLayout = new BorderLayout();
        this.setLayout(thisLayout);
        {

            this.add(widget, BorderLayout.CENTER);
            //widget.setBounds(199, 5, 1, 1); 
        }

    }

    public void setManagementPanel(boolean value){
        if (this.widget != null) {
            this.widget.setManagementPanelVisible(value);
        }
    }
    
    public void addChartData(final TreeMap<String, Object> chartData) {
        if (this.widget != null) {
            this.widget.addData(chartData);
            if (chartData != null) {
                for (String id:chartData.keySet()) {
                    widget.setDataViewMarkerStyle(id, IChartViewer.MARKER_BOX);
                }
            }
        }
    }

    /**
     * Auto-generated main method to display this JPanel inside a new JFrame.
     */
    public static void main(String[] args) {
	        System.out.println(System.getProperty("TANGO_HOST"));
	        
	        AttributeMultipleSpectrumPanel pan= new AttributeMultipleSpectrumPanel();
	       
	        TreeMap<String, Object> chartData = new TreeMap<String, Object>();
	        double[][] data = new double[2][];
	        data[1] = new double []{10, 20,30,40,50};
	        data[0] = new double []{1, 2,3,4,5};
	        chartData.put("Ma nouvelle courbe", data);
	       
	        data = new double[2][];
	        data[1] = new double []{60, 70,80,90,100};
            data[0] = new double []{1, 2,3,4,5};
            chartData.put("Ma nouvelle courbe2", data);
            pan.addChartData(chartData);
            
            pan.setManagementPanel(false);
            
	        JFrame frame = new JFrame();
	        frame.getContentPane().add(pan);
	        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	        frame.pack();
	        frame.setVisible(true);
	    }
}
