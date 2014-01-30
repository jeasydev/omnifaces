/*
 * Copyright 2012 OmniFaces.
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
package org.omnifaces.facesviews;

import static org.omnifaces.facesviews.FacesServletDispatchMethod.DO_FILTER;
import static org.omnifaces.facesviews.FacesViews.FACES_VIEWS_ENABLED_PARAM_NAME;
import static org.omnifaces.facesviews.FacesViews.FACES_VIEWS_RESOURCES;
import static org.omnifaces.facesviews.FacesViews.FACES_VIEWS_RESOURCES_EXTENSIONS;
import static org.omnifaces.facesviews.FacesViews.getFacesServletDispatchMethod;
import static org.omnifaces.facesviews.FacesViews.mapFacesServlet;
import static org.omnifaces.util.ResourcePaths.filterExtension;
import static org.omnifaces.util.ResourcePaths.isExtensionless;
import static org.omnifaces.util.Servlets.getApplicationAttribute;
import static org.omnifaces.util.Utils.isEmpty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.omnifaces.config.WebXml;
import org.omnifaces.eventlistener.DefaultServletContextListener;

/**
 * Convenience class for Servlet 3.0 users, which will map the FacesServlet to extensions found
 * during scanning in {@link FacesViewsInitializer}. This part of the initialization is executed
 * in a separate ServletContextListener, because the FacesServlet has to be available. This is
 * not guaranteed to be the case in an ServletContainerInitializer.
 * <p>
 * For a guide on FacesViews, please see the <a href="package-summary.html">package summary</a>.
 *
 * @author Arjan Tijms
 *
 */
@WebListener
public class FacesViewsInitializerListener extends DefaultServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent context) {

        ServletContext servletContext = context.getServletContext();

        if (!"false".equals(servletContext.getInitParameter(FACES_VIEWS_ENABLED_PARAM_NAME))) {

        	Set<String> extensions = getApplicationAttribute(servletContext, FACES_VIEWS_RESOURCES_EXTENSIONS);

        	if (!isEmpty(extensions)) {

        		Set<String> mappings = new HashSet<String>(extensions);
        		for (String welcomeFile : WebXml.INSTANCE.init(servletContext).getWelcomeFiles()) {
        			if (isExtensionless(welcomeFile)) {
        				if (!welcomeFile.startsWith("/")) {
        					welcomeFile = "/" + welcomeFile;
        				}
        				mappings.add(welcomeFile);
        			}
        		}

        		if (getFacesServletDispatchMethod(servletContext) == DO_FILTER) {
        			// In order for the DO_FILTER method to work the FacesServlet, in addition the forward filter, has
        			// to be mapped on all extensionless resources.
	        		Map<String, String> collectedViews = getApplicationAttribute(servletContext, FACES_VIEWS_RESOURCES);
	        		mappings.addAll(filterExtension(collectedViews.keySet()));
        		}

        		mapFacesServlet(servletContext, mappings);
        	}
        }
    }

}