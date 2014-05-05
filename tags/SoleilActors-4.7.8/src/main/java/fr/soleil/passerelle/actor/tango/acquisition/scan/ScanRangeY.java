package fr.soleil.passerelle.actor.tango.acquisition.scan;

class ScanRangeY {

	private double m_from;
	private double m_to;
	private int m_numberOfSteps;
	private boolean m_relative;

	private boolean fromInit = false;
	private boolean toInit = false;
	private boolean stepNumerInit = false;
	private boolean relativeInit = false;

//	protected int rangeNumber = 1;

	public ScanRangeY() {
	}

	public ScanRangeY(final double from, final double to, final int stepNr,
			final boolean relative) {
		m_from = from;
		m_to = to;
		m_numberOfSteps = stepNr;
		m_relative = relative;
	}

	public double getFrom() {
		return m_from;
	}

	public void setFrom(final double from) {
		this.m_from = from;
		fromInit = true;
	}

	public double getTo() {
		return m_to;
	}

	public void setTo(final double to) {
		this.m_to = to;
		toInit = true;
	}

	public int getNumberOfSteps() {
		return m_numberOfSteps;
	}

	public void setNumberOfSteps(final int stepNumer) {
		this.m_numberOfSteps = stepNumer;
		stepNumerInit = true;
	}

	public boolean isRelative() {
		return m_relative;
	}

	public void setRelative(final boolean relative) {
		this.m_relative = relative;
		relativeInit = true;
	}

	public boolean isFromInit() {
		return fromInit;
	}

	public boolean isToInit() {
		return toInit;
	}

	public boolean isStepNumerInit() {
		return stepNumerInit;
	}

	public boolean isRelativeInit() {
		return relativeInit;
	}

}
