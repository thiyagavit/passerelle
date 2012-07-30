package fr.soleil.passerelle.testUtils;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.soleil.passerelle.actor.acquisition.CCDSequence;
import fr.soleil.passerelle.actor.acquisition.PreDefinedScanSequence;
import fr.soleil.passerelle.actor.acquisition.ScanSequence;
import fr.soleil.passerelle.actor.basic.CommandSequence;
import fr.soleil.passerelle.actor.basic.GroupSequence;
import fr.soleil.passerelle.actor.basic.ReadSequence;
import fr.soleil.passerelle.actor.basic.TimeoutSequence;
import fr.soleil.passerelle.actor.basic.WriteSequence;
import fr.soleil.passerelle.actor.flow.ComparatorSequence;
import fr.soleil.passerelle.actor.flow.LoopsSequence;
import fr.soleil.passerelle.actor.flow.WhileLoopSequence;
import fr.soleil.passerelle.actor.tango.control.MotorSequence;
import fr.soleil.passerelle.error.ErrorReceiverSequence;
import fr.soleil.passerelle.error.ErrorSequence;

@RunWith(Suite.class)
@SuiteClasses( { CCDSequence.class,
		ScanSequence.class,
		PreDefinedScanSequence.class,
		// Storage.class,
		CommandSequence.class, GroupSequence.class, ReadSequence.class,
		TimeoutSequence.class, WriteSequence.class, ComparatorSequence.class,
		LoopsSequence.class, WhileLoopSequence.class,
		// SnapSequence.class,
		MotorSequence.class, ErrorReceiverSequence.class, ErrorSequence.class })
public class AllTests {

	@BeforeClass
	public static void setProperties() {
		FlowHelperForTests.setProperties(AllTests.class);
	}
}
