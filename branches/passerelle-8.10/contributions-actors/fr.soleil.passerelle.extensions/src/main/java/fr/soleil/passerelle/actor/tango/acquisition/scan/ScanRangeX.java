package fr.soleil.passerelle.actor.tango.acquisition.scan;

public class ScanRangeX extends ScanRangeY {

	private double m_integrationTime;
	private boolean integrationTimeInit;

	public ScanRangeX() {
		super();
		integrationTimeInit = false;
	}

	public ScanRangeX(final double from, final double to,
			final int stepNr, final double integrationTime,
			final boolean relative) {
		super(from,to,stepNr,relative);
		m_integrationTime = integrationTime;
	}

	public boolean isIntegrationTimeInit() {
		return integrationTimeInit;
	}

	public double getIntegrationTime() {
		return m_integrationTime;
	}

	public void setIntegrationTime(final double integrationTime) {
		this.m_integrationTime = integrationTime;
		integrationTimeInit = true;
	}

}
