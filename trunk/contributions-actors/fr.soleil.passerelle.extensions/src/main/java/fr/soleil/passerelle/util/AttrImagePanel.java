package fr.soleil.passerelle.util;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import fr.esrf.tangoatk.core.AttributePolledList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.IAttribute;
import fr.esrf.tangoatk.core.INumberImage;
import fr.esrf.tangoatk.widget.attribute.NumberImageViewer;

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
public class AttrImagePanel extends javax.swing.JPanel {
    private NumberImageViewer numberImageViewer;
    private AttributePolledList attributeList = new AttributePolledList();
    private String attributeName;

    /**
     * Auto-generated main method to display this
     * JPanel inside a new JFrame.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new AttrImagePanel());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public AttrImagePanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout1 = new BorderLayout();
            this.setLayout(thisLayout1);
            setPreferredSize(new Dimension(400, 300));
            {
                numberImageViewer = new NumberImageViewer();
                this.add(numberImageViewer, BorderLayout.CENTER);
                numberImageViewer.setPreferredSize(new java.awt.Dimension(391, 289));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postInitGUI() throws ConnectionException {
        IAttribute image = null;
        try {
            image = (IAttribute) attributeList.add(attributeName);
        } catch (ConnectionException e) {
            e.printStackTrace();
            throw e;
        }
        numberImageViewer.setBestFit(true);
        numberImageViewer.setModel((INumberImage) image);

        attributeList.refresh();// startRefresher();
//      remove viewer from the listener list
        numberImageViewer.setModel(null);
    }

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
