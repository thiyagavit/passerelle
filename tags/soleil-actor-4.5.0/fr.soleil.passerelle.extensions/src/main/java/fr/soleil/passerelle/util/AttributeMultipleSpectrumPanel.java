package fr.soleil.passerelle.util;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import fr.esrf.tangoatk.core.AttributePolledList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.INumberSpectrum;
import fr.esrf.tangoatk.core.util.AttrDualSpectrum;
import fr.esrf.tangoatk.widget.attribute.NonAttrNumberSpectrumViewer;


/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* *************************************
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED
* for this machine, so Jigloo or this code cannot be used legally
* for any corporate or commercial purpose.
* *************************************
*/
@SuppressWarnings("serial")
public class AttributeMultipleSpectrumPanel extends javax.swing.JPanel {
	private NonAttrNumberSpectrumViewer nonAttrNumberSpectrumViewer;
	private AttributePolledList attributeList = new AttributePolledList();
    private String attributeXName;
    private String[] attributeYNames;
    
	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		System.out.println(System.getProperty("TANGO_HOST"));
		AttributeMultipleSpectrumPanel pan= new AttributeMultipleSpectrumPanel();
		pan.setAttributeXName("tango/tangotest/1/double_spectrum");
		String [] y ={"tango/tangotest/test/double_spectrum","tango/tangotest/test2/double_spectrum"};
		
		pan.setAttributeYNames(y);
		try {
			pan.postInitGUI();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JFrame frame = new JFrame();
		frame.getContentPane().add(pan);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public AttributeMultipleSpectrumPanel() {
		super();
		initGUI();
		
	}
	
	private void initGUI() {
		try {
            
			setPreferredSize(new Dimension(400, 300));
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			{
				nonAttrNumberSpectrumViewer = new NonAttrNumberSpectrumViewer();
				this.add(nonAttrNumberSpectrumViewer , BorderLayout.CENTER);
				//nonAttrNumberSpectrumViewer.setPreferredSize(new java.awt.Dimension(1, 1));
				nonAttrNumberSpectrumViewer.setBounds(199, 5, 1, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 public void postInitGUI() throws ConnectionException{
	 	try {
	 	//configure X axis
	 		//System.out.println("x");
	 	INumberSpectrum xmodel = (INumberSpectrum)attributeList.add(attributeXName);
        //xmodel.setDescription("X");
        
        //configure Yi axis
        for(int i=0; i<attributeYNames.length; i++){
        	INumberSpectrum ymodel = (INumberSpectrum)attributeList.add(attributeYNames[i]);
        	
        	AttrDualSpectrum dual = new AttrDualSpectrum(xmodel.getDevice(), xmodel.getNameSansDevice(),
        			ymodel.getDevice(), ymodel.getNameSansDevice());
        	nonAttrNumberSpectrumViewer.addModel(dual);
            dual.refresh();
         
        }
        nonAttrNumberSpectrumViewer.getXAxis().setName(xmodel.getNameSansDevice());
  
	 	} catch (ConnectionException e) {
			e.printStackTrace();
			throw e;
		}
    }

	public String getAttributeXName() {
		return attributeXName;
	}
	public void setAttributeXName(String attributeXName) {
		this.attributeXName = attributeXName;
	}
	public String[] getAttributeYNames() {
		return attributeYNames;
	}
	public void setAttributeYNames(String[] attributeYNames) {
		this.attributeYNames = attributeYNames;
	}
}
