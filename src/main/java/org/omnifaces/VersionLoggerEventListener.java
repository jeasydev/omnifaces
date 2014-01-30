/*
 * Copyright 2013 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.omnifaces;

import java.util.logging.Logger;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;

import org.omnifaces.eventlistener.DefaultSystemEventListener;
import org.omnifaces.util.Faces;

/**
 * This event listener logs the version of OmniFaces that's being used when JSF has started up.
 *
 * @since 1.4
 * @author Arjan Tijms
 */
public class VersionLoggerEventListener extends DefaultSystemEventListener {

	private static final Logger logger = Logger.getLogger(VersionLoggerEventListener.class.getName());

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		logger.info("Using OmniFaces version " + Faces.class.getPackage().getSpecificationVersion());
	}

}
