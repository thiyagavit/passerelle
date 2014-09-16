package fr.soleil.passerelle.actor.tango.control.motor.configuration;

/**
 * this exception must be raise when the configuration is invalid
 * 
 */
public class MotorConfigurationException extends Exception {

    private static final long serialVersionUID = 3609537197206971127L;

    public MotorConfigurationException() {
    }

    public MotorConfigurationException(String message) {
        super(message);
    }

    public MotorConfigurationException(Throwable cause) {
        super(cause);
    }

    public MotorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
