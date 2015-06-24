/*******************************************************************************
 * Copyright (c) 2005, 2006 SOLEIL Synchrotron and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SOLEIL Synchrotron - initial API and implementation
 *******************************************************************************/
package fr.soleil.bossanova.resources;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
	private static final String BUNDLE_NAME = "fr.soleil.bossanova.resources.icons"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Icons() {
	}

	public static Icon getIcon(String key) {
		ImageIcon icon = null;
		
		try {
			URL url = ClassLoader.getSystemResource(RESOURCE_BUNDLE.getString(key));
			if(url != null)
				icon = new ImageIcon(url);
		} catch (MissingResourceException e) {
		}
		return icon;
	}
}
