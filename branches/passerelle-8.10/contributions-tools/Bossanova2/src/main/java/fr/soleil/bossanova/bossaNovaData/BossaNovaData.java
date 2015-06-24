package fr.soleil.bossanova.bossaNovaData;

import fr.soleil.bossanova.Bossanova2;
import fr.soleil.bossanova.controller.BossaNovaSequencerImpl;


/**
 * This class is used to store object and use them everywhere in the application without cross reference problem
 * @author DBA
 *
 */
public class BossaNovaData {
    private static BossaNovaData singleton = null;
    
    private Bossanova2 application = null;
    private BossaNovaSequencerImpl sequencer = null;
    
    public static BossaNovaData getSingleton()
    {
        if( singleton == null) 
            createSingleton();
        return singleton;
    }
    
    public static void createSingleton()
    {
        singleton = new BossaNovaData();
    }

    public Bossanova2 getApplication() {
        return application;
    }

    public void setApplication(Bossanova2 application) {
        this.application = application;
    }

    public BossaNovaSequencerImpl getSequencer() {
        return this.sequencer;
    }

    public void setSequencer(BossaNovaSequencerImpl sequencer) {
        this.sequencer = sequencer;
    }
}