package fr.soleil.passerelle.actor;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import com.isencia.passerelle.core.Port;

public class PortUtilities {

	@SuppressWarnings("unchecked")
	public static List<Port> getOrderedInputPorts(final Actor actor,
			final String prefix, final int offset) {
		final List<Port> outputPortList = actor.inputPortList();
		return sort(prefix, outputPortList, offset);
	}

	@SuppressWarnings("unchecked")
	public static List<Port> getOrderedOutputPorts(final Actor actor,
			final String prefix, final int offset) {
		final List<Port> inputPortList = actor.outputPortList();
		return sort(prefix, inputPortList, offset);
	}

	private static List<Port> sort(final String prefix,
			final List<Port> portList, final int offset) {
		final List<Port> orderedPorts = new ArrayList<Port>();
		for (final Port port : portList) {
			if (port.getName().startsWith(prefix)) {
				final String name = new String(port.getName());
				final int idx = Integer.valueOf(name.replaceAll(prefix, ""));
				orderedPorts.add(idx - offset, port);

			}
		}
		return orderedPorts;
	}

}
