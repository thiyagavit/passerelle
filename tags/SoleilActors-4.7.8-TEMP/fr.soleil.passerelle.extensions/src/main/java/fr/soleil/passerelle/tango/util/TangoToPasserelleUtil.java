/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package fr.soleil.passerelle.tango.util;

import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;

/**
 * Some utilities...
 * 
 * @author erwin.de.ley@isencia.be
 */
public final class TangoToPasserelleUtil {

    /**
     * Convert a DevFailed to a String
     * 
     * @param df The DevFailed
     * @return The string
     */
    public static String getDevFailedString(final DevFailed df, final Actor act) {
        return TangoToPasserelleUtil.getDevFailedString(df, act, true);
    }

    /**
     * Convert a DevFailed to a String, and trace it in ExecutionTracerService
     * 
     * @param df The DevFailed
     * @return The string
     */
    public static String getDevFailedString(final DevFailed df, final Director dir) {
        return TangoToPasserelleUtil.getDevFailedString(df, dir, false);
    }

    public static String getDevFailedString(final DevFailed df, final NamedObj obj, final boolean isActor) {
        final StringBuffer buffer = new StringBuffer("\n");
        if (df != null) {
            if (df.errors != null) {
                for (int i = 0; i < df.errors.length; i++) {
                    try {
                        buffer.append("Error Level " + i + ":\n");
                        buffer.append("\t - desc: " + df.errors[i].desc.toString() + "\n");
                        buffer.append("\t - origin: " + df.errors[i].origin.toString() + "\n");
                        buffer.append("\t - reason: " + df.errors[i].reason.toString() + "\n");
                        String sev = "";
                        if (df.errors[i].severity.value() == ErrSeverity.ERR.value()) {
                            sev = "ERROR";
                        } else if (df.errors[i].severity.value() == ErrSeverity.PANIC.value()) {
                            sev = "PANIC";
                        } else if (df.errors[i].severity.value() == ErrSeverity.WARN.value()) {
                            sev = "WARN";
                        }
                        buffer.append("\t - severity: " + sev + "\n");

                        if (isActor) {
                            final Actor act = (Actor) obj;
                            ExecutionTracerService.trace(act, "Error Level " + i + ":");
                            ExecutionTracerService.trace(act, "\t - desc: " + df.errors[i].desc.toString());
                            ExecutionTracerService.trace(act, "\t - origin: " + df.errors[i].origin.toString());
                            ExecutionTracerService.trace(act, "\t - reason: " + df.errors[i].reason.toString());
                            ExecutionTracerService.trace(act, "\t - severity: " + sev);
                        } else {
                            final Director dir = (Director) obj;
                            ExecutionTracerService.trace(dir, "Error Level " + i + ":");
                            ExecutionTracerService.trace(dir, "\t - desc: " + df.errors[i].desc.toString());
                            ExecutionTracerService.trace(dir, "\t - origin: " + df.errors[i].origin.toString());
                            ExecutionTracerService.trace(dir, "\t - reason: " + df.errors[i].reason.toString());
                            ExecutionTracerService.trace(dir, "\t - severity: " + sev);
                        }
                    } catch (Exception e) {
                        buffer.append("one element in df.errors[" + i + "] is null and can not be read");
                        e.printStackTrace();
                    }
                }
            } else {
                buffer.append("DevFailed.errors is null object nothing can be read on it");
            }
        } else {
            buffer.append("DevFailed is null object nothing can be read on it");
        }

        return buffer.toString();
    }
}
