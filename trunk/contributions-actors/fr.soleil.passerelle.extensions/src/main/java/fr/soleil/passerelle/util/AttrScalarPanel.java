package fr.soleil.passerelle.util;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import fr.esrf.tangoatk.core.AttributePolledList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.IAttribute;
import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.widget.attribute.SimpleScalarViewer;

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
public class AttrScalarPanel extends javax.swing.JPanel {
    private SimpleScalarViewer simpleScalarViewer;
    private AttributePolledList attributeList = new AttributePolledList();
    private String attributeName;
    private String value = "";
    private boolean isAttribute = false;

    public static final int DEFAULT_DIM_WIDTH = 351;
    public static final int DEFAULT_DIM_HEIGHT = 43;

    /**
     * Auto-generated main method to display this
     * JPanel inside a new JFrame.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new AttrScalarPanel());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public AttrScalarPanel() {
        super();
        initGUI(DEFAULT_DIM_WIDTH, DEFAULT_DIM_HEIGHT);
    }

    public AttrScalarPanel(final int dimWidthValue, final int dimHeightValue) {
        super();
        initGUI(dimWidthValue, dimHeightValue);
    }

    private void initGUI(final int dimWidthValue, final int dimHeightValue) {
        try {
            this.setPreferredSize(new java.awt.Dimension(dimWidthValue, dimHeightValue));
            this.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            {
                simpleScalarViewer = new SimpleScalarViewer();
                this.add(simpleScalarViewer, BorderLayout.CENTER);
                simpleScalarViewer.setText("simpleScalarViewer");
                simpleScalarViewer.setFont(new Font("Arial", 0, 20));
                simpleScalarViewer.setBounds(199, 5, 1, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postInitGUI() throws ConnectionException {
        IAttribute scalar = null;
        if (isAttribute) {
            try {
                scalar = (IAttribute) attributeList.add(attributeName);

            } catch (ConnectionException e) {
                e.printStackTrace();
                throw e;
            }
            simpleScalarViewer.setModel((INumberScalar) scalar);
            simpleScalarViewer.setUnitVisible(true);
            attributeList.refresh();// startRefresher();
//      remove viewer from the listener list
            simpleScalarViewer.setModel((INumberScalar) null);
        } else {
            simpleScalarViewer.setText(getValue());
        }

    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isAttribute() {
        return isAttribute;
    }

    public void setIsAttribute(boolean isAttribute) {
        this.isAttribute = isAttribute;
    }
}
