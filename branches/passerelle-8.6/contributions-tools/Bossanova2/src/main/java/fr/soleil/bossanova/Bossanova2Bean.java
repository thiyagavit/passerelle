package fr.soleil.bossanova;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXErrorPane;

import com.isencia.passerelle.hmi.state.StateMachine;
import fr.soleil.bean.Startable;
import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.controller.BatchManager;
import fr.soleil.bossanova.gui.MainScreen;

public class Bossanova2Bean extends JPanel implements Startable {

    private static final long serialVersionUID = 1L;
    private Bossanova2 application;

    public Bossanova2Bean() {
        super();
        setLayout(new BorderLayout());
        initSystemProperties();

        try {
            application = new Bossanova2();
            application.initUI(false);
            MainScreen mainScreen = application.getMainScreen();
            add(mainScreen.getContentPane());

            //mainScreen.setSize(new Dimension(800, 600));
            //setSize(new Dimension(800, 600));
            mainScreen.setSize(new Dimension(400, 300));
            setSize(new Dimension(400, 300));
        } catch (Exception e) {
            JXErrorPane.showDialog(e);
        }
    }

    private void initSystemProperties() {
        testAndCreateDefaultSystemProperties("sequences.directory",
                "/usr/Local/configFiles/passerelle/models/passerelle");
        testAndCreateDefaultSystemProperties("actors.common.directory",
                "/usr/Local/configFiles/passerelle/conf_ide/common/actors");
        testAndCreateDefaultSystemProperties("actors.specific.directory",
                "/usr/Local/configFiles/passerelle/conf_ide/specific/");
        testAndCreateDefaultSystemProperties("TANGO_HOST", "tangodb:20001");
        testAndCreateDefaultSystemProperties("com.isencia.home", System.getenv("PASSERELLE_HOME"));
        testAndCreateDefaultSystemProperties("java.ext.dirs", System.getenv("CLASSPATH"));
    }

    private void testAndCreateDefaultSystemProperties(String propertyToTest,
            String defaultValue) {
        if (System.getProperty(propertyToTest) == null) {
            System.setProperty(propertyToTest, defaultValue);
        }
    }

    public void setVisibleSequences(String[] sequences) {
        ArrayList<String> sequencesList = new ArrayList<String>(Arrays.asList(sequences));
        RepositoryManager.getSequenceRepository().setEnabledElements(
                sequencesList);
        RepositoryManager.getSequenceRepository().notifyEnabledElementsChanged();
    }

    public void setBatch(String fileName) {
        File selectedFile = new File(fileName);
        if (selectedFile != null) {
            try {
                BatchManager.loadBatch(selectedFile);
                StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
            } catch (Exception e) {
                JXErrorPane.showDialog(e);
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // Startable implementation
    // -----------------------------------------------------------------------------------
    public void start() {
        // NOTHING TO DO (YET)
    }

    public void stop() {
        // NOTHING TO DO
    }

    public void pauseBatch() {
        if (StateMachine.getInstance().getCurrentState().equals(StateMachine.MODEL_EXECUTING_SUSPENDED)) {
            application.resumeModel();
        } else {
            application.suspendModel();
        }
    }

    public void startBatch() {
        application.launchModel();
    }

    public void stopBatch() {
        application.stopModel();
    }

    public static void main(String[] args) {
        JFrame test = new JFrame();
        Bossanova2Bean bean = new Bossanova2Bean();
        // String[] seqs = new String[] { "tests*" };
        // bean.setVisibleSequences(seqs);
        // bean.setBatch("/home/viguier/test.bnb");
        test.add(bean);
        test.setVisible(true);
        //test.setSize(new Dimension(800, 600));
        test.setSize(new Dimension(400, 300));

    }
}
