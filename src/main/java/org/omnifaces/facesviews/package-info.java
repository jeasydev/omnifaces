/**
 * <p>
 * This package contains the classes for the OmniFaces' FacesViews feature.
 *
 * <h3>Introduction</h3>
 *
 * <p>
 * FacesViews is a feature where a special dedicated directory (<code>/WEB-INF/faces-views</code>), or optionally
 * one or more user specified directories, can be used to store Facelets source files.
 *
 * <p>
 * All files found in these directory are automatically mapped as Facelets files
 * and made available using both their original extension as well as without an extension (extensionless). Optionally scanning
 * can be restricted to include only certain extensions.
 *
 * <p>
 * With FacesViews, there is thus no need to list all Facelets views that should be accessed without an extension in
 * some configuration file. Additionally, it thus automatically maps Facelets files to their original file extension, which
 * prevents exposing the source code of those Facelets that happens with the default JSF mapping.
 *
 * <p>
 * In Servlet 3.0 containers, scanning is done automatically and no further configuration is needed. The feature
 * is compatible with applications that don't have <code>web.xml</code> or <code>faces-config.xml</code> configuration
 * files. As such, it can be used as an alternative to declaring the {@link javax.faces.webapp.FacesServlet} in <code>web.xml</code>
 * for the <code>.xhtml</code> to <code>.xhtml</code> mapping.
 *
 * <p>
 * <b>Example 1:</b>
 *
 * <p>
 * Consider the following file structure and assume no further configuration has been done:
 *
 * <pre>
 *    /WEB-INF/faces-views/index.xhtml
 *    /WEB-INF/faces-views/users/add.xhtml
 *    /normal.xhtml
 * </pre>
 *
 * <p>
 * This will make the Facelets available via the following URLs (given a root deployment on domain <code>example.com</code>):
 *
 * <pre>
 *    example.com/index
 *    example.com/users/add
 *    example.com/index.xhtml (will direct to /index by default)
 *    example.com/users/add.xhtml (will direct to /users/add by default)
 *    example.com/normal.xhtml
 * </pre>
 *
 * <p>
 * Note that although the directory outside <code>/WEB-INF/faces-views</code> is not scanned, the {@link javax.faces.webapp.FacesServlet}
 * <em>is</em> mapped on all extensions found in <code>/WEB-INF/faces-views</code>, so this will also affect files outside
 * this directory. In the above example <code>normal.xhtml</code> is thus also available via the <code>.xhtml</code> extension, since
 * the whole FacesServlet is mapped on this.
 *
 * <p>
 * Also note that the extension variants of the scanned views will redirect to the extensionless variants. This behavior can be changed (see below),
 * so that these views are either directly available (no redirect) or are not available at all.
 *
 * <p>
 * <b>Example 2:</b>
 *
 * <p>
 * Consider the following <code>web.xml</code>:
 *
 * <pre>
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_SCAN_PATHS&lt;/param-name&gt;
 *        &lt;param-value&gt;/*.xhtml&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 * </pre>
 *
 * <p>
 * And this file structure:
 *
 * <pre>
 *    /page1.xhtml
 *    /foo/page2.xhtml
 *    /WEB-INF/resources/template.xhtml
 *    /script.js
 * </pre>
 *
 * <p>
 * This will make the Facelets available via the following URLs (given a root deployment on domain <code>example.com</code>):
 *
 * <pre>
 *    example.com/page1
 *    example.com/foo/page2
 *    example.com/page1.xhtml (will direct to /page1 by default)
 *    example.com/foo/page2.xhtml (will direct to /foo/page2 by default)
 * </pre>
 *
 * <p>
 * Note that in the above example, <code>/WEB-INF</code> was NOT scanned and thus <code>template.xhtml</code> is not made publicly available. Likewise
 * <code>/script.js</code> was also not scanned since it doesn't have the configured extension (<code>.xhtml</code>). Finally, although a <code>web.xml</code> was used, there
 * does not need to be a mapping for the <code>FacesServlet</code> in it when using a Servlet 3.0 container.
 *
 * <h3>Welcome files</h3>
 *
 * <p>
 * If a <code>&lt;welcome-file&gt;</code> is defined in <code>web.xml</code> that's scanned by FacesViews <strong>AND</strong> <code>REDIRECT_TO_EXTENSIONLESS</code> is used
 * (which is the default, see below), it's necessary to define an extensionless welcome file to prevent a request to <code>/</code> being redirected to
 * <code>/[welcome file]</code>. E.g. without this <code>http://example.com</code> will redirect to say <code>http://example.com/index</code>.
 *
 * <p>
 * For example:
 *
 * <pre>
 *    &lt;welcome-file-list&gt;
 *        &lt;welcome-file&gt;index&lt;/welcome-file&gt;
 *    &lt;/welcome-file-list&gt;
 * </pre>
 *
 * <h3>Dispatch methods</h3>
 *
 * <p>
 * JSF normally inspects the request URI to derive a logical view id from it. It assumes the FacesServlet is either mapped on a prefix path
 * or an extension, and will get confused when an extensionless "exactly mapped" request is encountered. To counter this, FacesViews
 * makes use of a filter that intercepts each request and makes it appear to JSF that the request was a normal extension mapped one.
 *
 * <p>
 * In order to do this dispatching, two methods are provided; forwarding, and wrapping the request and continuing the filter chain.
 * For the last method to work, the FacesServlet is programmatically mapped to every individual resource (page/view) that is encountered. By
 * default the filter is automatically registered and is inserted <b>after</b> all filters that are declared in <code>web.xml</code>.
 *
 * <p>
 * These internal details are important for users to be aware of, since they greatly influence how extensionless requests interact with other
 * filter based functionality such as security filters, compression filters, file upload filters, etc.
 *
 * <p>
 * With the forwarding method, filters typically have to be set to dispatch type <code>FORWARD</code> as well. If the FacesView filter is the first
 * in the chain other filters that are set to dispatch type <code>REQUEST</code> will <strong>NOT</strong> be invoked at all (the chain is ended). If the
 * FacesView filter is set to be the last, other filters will be invoked, but they should not modify the response (a forward clears
 * the response buffer till so far if not committed).
 *
 * <p>
 * No such problems appear to exist when the FacesView filter simply continues the filtering chain. However, since it wraps the requess there
 * might be unforeseen problems with containers or other filters that get confused when the request URI changes in the middle of the chain.
 * Continuing the chain has been tested with JBoss EAP 6.0.1, GlassFish 3.1.2.2, WebLogic 12.1.1 and TomEE 1.5.2-snapshot and thus with both
 * Mojarra and MyFaces. However, since it's a new method for OmniFaces 1.4 we kept the existing forward as an alternative.
 *
 * <p>
 * The configuration options below provide more details about the dispatch methods and the filter position which can be used for tweaking
 * FacesViews for interoperability with other filters.
 *
 * <h3>Configuration</h3>
 *
 * <p>
 * The following context parameters are available.
 *
 * <table border="1" cellspacing="0">
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_ENABLED_PARAM_NAME}</code></td>
 * <td>Used to completely switch scanning off.
 * <br/>Allowed values: {<code>true</code>,<code>false</code>}
 * <br/>Default value: <code>true</code>
 * <br/>(note that if no <code>/WEB-INF/faces-views</code> directory is present and no explicit paths have been configured, no scanning will be done either)</td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_SCAN_PATHS_PARAM_NAME}</code></td>
 * <td>A comma separated list of paths that are to be scanned in addition to <code>/WEB-INF/faces-views</code>.
 * <br/>Allowed values: any path relative to the web root, including the root path (<code>/</code>) and <code>/WEB-INF</code>.
 * A wildcard can be added to the path, which will cause only files with the given extension te be scanned.
 * <br/>Examples:
 * <br/>- Scan all files in both folder1 and folder2: <code>/folder1, /folder2</code>
 * <br/>- Scan only .xhtml files in the root: <code>/*.xhtml</code>
 * <br/>Note that when the root path is given, all its sub paths are also scanned EXCEPT <code>WEB-INF</code>, <code>META-INF</code> and <code>resources</code>.
 * If those have to be scanned as well, they can be added to the list of paths explicitly.
 * <br/>Default value: <code>/WEB-INF/faces-views</code> (note when this value is set, those paths will be in addition to the default <code>/WEB-INF/faces-views</code>)
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_SCANNED_VIEWS_EXTENSIONLESS_PARAM_NAME}</code></td>
 * <td>Used to set how scanned views should be rendered in JSF controlled links.
 * With this setting set to <code>false</code>, it depends on whether the request URI uses an extension or not.
 * If it doesn't, links are also rendered without one, otherwise they are rendered with an extension.
 * When set to <code>true</code> links are always rendered without an extension.
 * <br/>Default value: <code>true</code>
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_EXTENSION_ACTION_PARAM_NAME}</code></td>
 * <td>Determines the action that is performed whenever a resource is requested WITH extension that's also available without an extension.
 * <br/>Allowed values: {<code>SEND_404</code>,<code>REDIRECT_TO_EXTENSIONLESS</code>,<code>PROCEED</code>}, which have the following meaning:
 * <br/>- <code>SEND_404</code>: Send a 404 (not found), makes it look like e.g. <code>/foo.xhtml</code> never existed and there's only <code>/foo</code>.
 * <br/>- <code>REDIRECT_TO_EXTENSIONLESS</code>: Redirects to the same URL, but with the extension removed. E.g. <code>/foo.xhtml</code> is redirected to <code>/foo</code>.
 * <br/>- <code>PROCEED</code>: No special action is taken. Both <code>/foo.xhtml</code> and <code>/foo</code> are processed as-if they were separate views (with same content).
 * <br/>Default value: <code>REDIRECT_TO_EXTENSIONLESS</code>
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_PATH_ACTION_PARAM_NAME}</code></td>
 * <td>Determines the action that is performed whenever a resource is requested in a public path that has been used for scanning views by faces views
 * (e.g. the paths set by <code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_SCAN_PATHS_PARAM_NAME}</code>, but excluding the root path /).
 * <br/>Allowed values: {<code>SEND_404</code>,<code>REDIRECT_TO_SCANNED_EXTENSIONLESS</code>,<code>PROCEED</code>}, which have the following meaning:
 * <br/>- <code>SEND_404</code>: Send a 404 (not found), makes it look like e.g. <code>/path/foo.xhtml</code> never existed and there's only <code>/foo</code> and optionally <code>/foo.xhtml</code>.
 * <br/>- <code>REDIRECT_TO_SCANNED_EXTENSIONLESS</code>: Redirects to the resource corresponding with the one that was scanned. e.g. <code>/path/foo.xml</code> redirects to <code>/foo</code>.
 * <br/>- <code>PROCEED</code>: No special action is taken. <code>/path/foo.xml</code> and <code>/foo</code> (and optionally <code>/foo.xhtml</code>) will be accessible.
 * <br/>Default value: <code>REDIRECT_TO_EXTENSIONLESS</code>
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_DISPATCH_METHOD_PARAM_NAME}</code></td>
 * <td>Determines the method used by FacesViews to invoke the FacesServlet.
 * <br/>Allowed values: {<code>FORWARD</code>,<code>DO_FILTER</code>}, which have the following meaning:
 * <br/>- <code>FORWARD</code>: Use a forward to invoke the {@link javax.faces.webapp.FacesServlet}. Using this method the {@link javax.faces.webapp.FacesServlet} does not have to be mapped to the (extensionless) requested resource or to everything (/*) when manually mapping.
 * <br/>- <code>DO_FILTER</code>: Use a plain {@link javax.servlet.FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} to invoke the {@link javax.faces.webapp.FacesServlet}. Using this method necessitates the FacesServlet to be mapped to the (extensionless) requested resource or to everything (/*) when manually mapping.
 * <br/>Default value: <code>DO_FILTER</code>
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_VIEW_HANDLER_MODE_PARAM_NAME}</code></td>
 * <td>Determines how the {@link org.omnifaces.facesviews.FacesViewsViewHandler} should build the action URL that's used in e.g. forms and links.
 * <br/>Allowed values: {<code>STRIP_EXTENSION_FROM_PARENT</code>, <code>BUILD_WITH_PARENT_QUERY_PARAMETERS</code>}, which have the following meaning:
 * <br/>- <code>STRIP_EXTENSION_FROM_PARENT</code>: Strip the extension from the parent view handler's outcome using the at runtime determined extension mapping of the FacesServlet. Requires Servlet 3.0+.
 * <br/>- <code>BUILD_WITH_PARENT_QUERY_PARAMETERS</code>: The <code>FacesViewsViewHandler</code> constructs the action URL itself and only takes the query parameters (if any) from the parent view handler outcome. This is mode is automatically selected on Servlet 2.5.
 * <br/>Default value: <code>STRIP_EXTENSION_FROM_PARENT</code> on Servlet 3.0+, <code>BUILD_WITH_PARENT_QUERY_PARAMETERS</code> on Servlet 2.5.
 * </td>
 * </tr>
 *
 * <tr>
 * <td nowrap><code>{@value org.omnifaces.facesviews.FacesViews#FACES_VIEWS_FILTER_AFTER_DECLARED_FILTERS_PARAM_NAME}</code></td>
 * <td>Used to set whether the {@link org.omnifaces.facesviews.FacesViewsForwardingFilter} should match before declared filters (<code>false</code>) or after declared filters (<code>true</code>),
 * when automatic scanning and mapping is used (as opposed to manually mapping the Servlet as explained in the Servlet 2.5 section below).
 * <br/>Default value: <code>true</code> (the FacesViews forwarding filter is the last in the filter chain)
 * </td>
 * </tr>
 *
 * </table>
 *
 * <h3>Servlet 2.5 configuration</h3>
 *
 * <p>
 * Servlet 2.5 users will have to install the {@link org.omnifaces.facesviews.FacesViewsForwardingFilter} and
 * {@link org.omnifaces.facesviews.FacesViewsViewHandler} manually in <code>web.xml</code>:
 *
 * <pre>
 *    &lt;filter&gt;
 *        &lt;filter-name&gt;FacesViewsForwardingFilter&lt;/filter-name&gt;
 *        &lt;filter-class&gt;org.omnifaces.facesviews.FacesViewsForwardingFilter&lt;/filter-class&gt;
 *    &lt;/filter&gt;
 *    &lt;filter-mapping&gt;
 *        &lt;filter-name&gt;FacesViewsForwardingFilter&lt;/filter-name&gt;
 *        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;/filter-mapping&gt;
 *
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_DISPATCH_METHOD&lt;/param-name&gt;
 *        &lt;param-value&gt;FORWARD&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 * </pre>
 *
 * <p>
 * Note that the <code>FORWARD</code> method has been set. Without that, the FacesServlet has to be mapped manually (also in web.xml) to
 * each and every view that is used. This is similar to how the welcome file (see below) has to be mapped to the FacesServlet for Servlet
 * 2.5 users.
 *
 * <p>
 * When an extensionless welcome file is defined in <code>web.xml</code> (see above), the FacesServlet has to be explicitly mapped
 * to this welcome file for Servlet 2.5. E.g.
 *
 * <pre>
 *    &lt;welcome-file-list&gt;
 *        &lt;welcome-file&gt;index&lt;/welcome-file&gt;
 *    &lt;/welcome-file-list&gt;
 *
 *    &lt;servlet&gt;
 *        &lt;servlet-name&gt;facesServlet&lt;/servlet-name&gt;
 *        &lt;servlet-class&gt;javax.faces.webapp.FacesServlet&lt;/servlet-class&gt;
 *        &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 *    &lt;servlet-mapping&gt;
 *        &lt;servlet-name&gt;facesServlet&lt;/servlet-name&gt;
 *        &lt;url-pattern&gt;*.xhtml&lt;/url-pattern&gt;
 *        &lt;url-pattern&gt;/welcome&lt;/url-pattern&gt;
 *    &lt;/servlet-mapping&gt;
 * </pre>
 *
 * <p>
 * And this in <code>faces-config.xml</code>
 *
 * <pre>
 *    &lt;application&gt;
 *        &lt;view-handler&gt;org.omnifaces.facesviews.FacesViewsViewHandler&lt;/view-handler&gt;
 *    &lt;/application&gt;
 * </pre>
 *
 * <p>
 * <em>(at the moment Servlet 2.5 compatibility has not been tested thorougly)</em>
 *
 * <h3>OmniFaces 1.3 compatibility</h3>
 *
 * <p>
 * In OmniFaces 1.4, a major overhaul was done for FacesViews and several things are done differently from how they
 * were done in 1.3
 *
 * <p>
 * Most notably is that the FacesServlet dispatch changed from forwarding to continuing the chain, the FacesView filter
 * moved from being the first in the chain to being the last, links are always rendered as their extensionless variant independent
 * of the request using an extension or not, and when a request with an extension is used anyway (e.g. by typing it directly into
 * the address bar) it's now redirected to the extensionless variant.
 *
 * <p>
 * By putting the following settings in <code>web.xml</code> a behavior that most closely resembles 1.3 can be obtained:
 *
 * <pre>
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_DISPATCH_METHOD&lt;/param-name&gt;
 *        &lt;param-value&gt;FORWARD&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_FILTER_AFTER_DECLARED_FILTERS&lt;/param-name&gt;
 *        &lt;param-value&gt;false&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_SCANNED_VIEWS_ALWAYS_EXTENSIONLESS&lt;/param-name&gt;
 *        &lt;param-value&gt;false&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 *    &lt;context-param&gt;
 *        &lt;param-name&gt;org.omnifaces.FACES_VIEWS_EXTENSION_ACTION&lt;/param-name&gt;
 *        &lt;param-value&gt;PROCEED&lt;/param-value&gt;
 *    &lt;/context-param&gt;
 * </pre>
 *
 *
 * @author Arjan Tijms
 *
 */
package org.omnifaces.facesviews;

