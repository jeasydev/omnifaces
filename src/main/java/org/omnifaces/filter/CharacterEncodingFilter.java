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
package org.omnifaces.filter;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.faces.context.ExternalContext;
import javax.faces.context.PartialViewContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This filter will set the request body character encoding when not already set by the client. Even though
 * JSF2/Facelets uses by default UTF-8 everywhere, which is the best charset choice these days, JSF2/Facelets might
 * fail to set it to UTF-8 when something else has set it to a different value before JSF2/Facelets gets the chance to
 * set it during the restore view phase. PrimeFaces 3.x for example is known to do that. During ajax requests, it will
 * call {@link ExternalContext#getRequestParameterMap()} inside {@link PartialViewContext#isAjaxRequest()} right before
 * building/restoring the view, which will implicitly set the request body character encoding to the server-default
 * value, which is not UTF-8 per se.
 * <p>
 * To get this filter to run, map it as follows in <code>web.xml</code>:
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;characterEncodingFilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.omnifaces.filter.CharacterEncodingFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *   &lt;filter-name&gt;characterEncodingFilter&lt;/filter-name&gt;
 *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * <p>As JSF2/Facelets uses by default UTF-8 everywhere, the default charset is also set to UTF-8. When really
 * necessary for some reason, then it can be overridden by specifying the <code>encoding</code> initialization
 * parameter in the <code>&lt;filter&gt;</code> element as follows:
 * <pre>
 * &lt;init-param&gt;
 *   &lt;description&gt;
 *     The character encoding which is to be used to parse the HTTP request body. Defaults to UTF-8.
 *   &lt;/description&gt;
 *   &lt;param-name&gt;encoding&lt;/param-name&gt;
 *   &lt;param-value&gt;ISO-8859-1&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * <p>Please note that this only affects HTTP POST requests, not HTTP GET requests. For HTTP GET requests, you should
 * be specifying the charset at servletcontainer level (e.g. <code>&lt;Context URIEncoding="UTF-8"&gt;</code> in Tomcat,
 * or <code>&lt;parameter-encoding default-charset="UTF-8"&gt;</code> in Glassfish). Also note that this doesn't affect
 * HTTP responses in any way. For HTTP responses, you should be specifying the charset in
 * <code>&lt;f:view encoding&gt;</code>, which also already defaults to UTF-8 by the way.
 *
 * @author Bauke Scholtz
 * @link http://code.google.com/p/primefaces/issues/detail?id=2223
 * @link http://stackoverflow.com/a/9839362/157882
 * @since 1.2
 */
public class CharacterEncodingFilter extends HttpFilter {

	// Constants ------------------------------------------------------------------------------------------------------

	private static final String INIT_PARAM_ENCODING = "encoding";
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String ERROR_ENCODING =
		"The 'encoding' init param must represent a valid charset. Encountered an invalid charset of '%s'.";

	// Vars -----------------------------------------------------------------------------------------------------------

	private String encoding = DEFAULT_ENCODING;

	// Actions --------------------------------------------------------------------------------------------------------

	/**
	 * Initializes the filter parameters.
	 */
	@Override
	public void init() throws ServletException {
		String encoding = getInitParameter(INIT_PARAM_ENCODING);

		if (encoding != null) {
			try {
				Charset.forName(encoding);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(String.format(ERROR_ENCODING, encoding), e);
			}

			this.encoding = encoding;
		}
	}

	/**
	 * Perform the filtering job. Only if the request character encoding has not been set yet, then set it.
	 */
	@Override
	public void doFilter
		(HttpServletRequest request, HttpServletResponse response, HttpSession session, FilterChain chain)
			throws ServletException, IOException
	{
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding(encoding);
		}

		chain.doFilter(request, response);
	}

}