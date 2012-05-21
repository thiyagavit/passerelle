package fr.soleil.passerelle;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.isencia.passerelle.model.Flow;

@RunWith(Parameterized.class)
public class LaunchSequence {

	@Parameters
	public static List<Object[]> getParametres() {

		return Arrays
				.asList(new Object[][] {
						{ "/fr/soleil/passerelle/resources/automaticConversions.moml" },
						{ "/fr/soleil/passerelle/resources/WriterWait.moml" } });
	}

	private final Flow topLevel;

	public LaunchSequence(final String sequencePath) {
		topLevel = FlowHelperForTests.loadMoml(this.getClass(), sequencePath);
	}

	@BeforeClass
	public static void setUp() {
		FlowHelperForTests.setProperties(LaunchSequence.class);
	}

	@Test
	public void execute() {
		FlowHelperForTests.executeBlockingError(topLevel, null);
	}

}
