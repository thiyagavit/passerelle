package fr.soleil.passerelle.tango.util;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tangounit.junit.TangoUnitTest;

public class TangoAccessTest extends TangoUnitTest{
    
    // sur tangodb:20001
    private static String deviceName = "tango/tangotest/spjz_01.01";
    private static TangoCommand cmdInit;
    private static TangoCommand cmdSwitch;
    
    
    @BeforeClass
    public static void setUp() throws DevFailed {
        try {
            cmdInit = new TangoCommand(deviceName, "Init");
            cmdSwitch = new TangoCommand(deviceName, "SwitchStates");
            
        } catch (DevFailed e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void isCurrentStateEqualStateRequired() throws DevFailed {
        cmdInit.execute();
        assertTrue("Etat courant invalide devrait être Running", TangoAccess.isCurrentStateEqualStateRequired("tango/tangotest/spjz_01.01", DevState.RUNNING));
        
        cmdSwitch.execute();
        
        assertTrue("Etat courant invalide devrait être Fault", TangoAccess.isCurrentStateEqualStateRequired("tango/tangotest/spjz_01.01", DevState.FAULT));
        
    }
    
   @Test
    public void getCurrentState() throws DevFailed {
        
        cmdInit.execute();
            
        assertTrue("Etat courant invalide devrait être Running", TangoAccess.getCurrentState(deviceName).equals(DevState.RUNNING));
        
        cmdSwitch.execute();
        
        final TangoCommand cmdState = new TangoCommand(deviceName, "State");        
        assertTrue("Etat courant invalide devrait être Fault1", TangoAccess.getCurrentState(cmdState).equals(DevState.FAULT));
        assertTrue("Etat courant invalide devrait être Fault2", TangoAccess.getCurrentState(cmdState)== DevState.FAULT);
        assertTrue("Etat courant invalide devrait être Fault3", TangoAccess.getCurrentState(cmdState).equals(DevState.FAULT));
        
        cmdInit.execute();
        assertTrue("Etat courant invalide devrait être Running1", TangoAccess.getCurrentState(cmdState).equals(DevState.RUNNING));
        assertTrue("Etat courant invalide devrait être Running2", TangoAccess.getCurrentState(deviceName).equals(DevState.RUNNING));
        
        assertTrue("Etat courant invalide devrait être Unknown",TangoAccess.getCurrentState("")== DevState.UNKNOWN);
 
    }
    
    @Test
    public void executeCmdAccordingState() throws DevFailed {
        cmdInit.execute();
        cmdSwitch.execute();
        
        assertTrue("Commande non executee", TangoAccess.executeCmdAccordingState(deviceName, DevState.FAULT, "Init"));
        assertTrue("Etat courant invalide devrait être Running", TangoAccess.getCurrentState(deviceName).equals(DevState.RUNNING));
        assertTrue("Commande executee", !TangoAccess.executeCmdAccordingState(deviceName, DevState.FAULT, "Init"));
        
        
    }
}
