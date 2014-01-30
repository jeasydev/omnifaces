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
package org.omnifaces.component.input;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.omnifaces.component.input.Form.PropertyKeys.includeRequestParams;
import static org.omnifaces.component.input.Form.PropertyKeys.includeViewParams;
import static org.omnifaces.component.input.Form.PropertyKeys.useRequestURI;
import static org.omnifaces.util.FacesLocal.getRequestQueryStringMap;
import static org.omnifaces.util.FacesLocal.getRequestURIWithQueryString;
import static org.omnifaces.util.FacesLocal.getViewParameterMap;

import java.io.IOException;

import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;

import org.omnifaces.taghandler.IgnoreValidationFailed;
import org.omnifaces.util.State;

/**
 * <strong>Form</strong> is a component that extends the standard {@link UIForm} and provides a way to keep view
 * or request parameters in the request URL after a post-back and offers in combination with the
 * <code>&lt;o:ignoreValidationFailed&gt;</code> tag on an {@link UICommand} component the possibility to ignore
 * validation failures so that the invoke action phase will be executed anyway.
 * <p>
 * You can use it the same way as <code>&lt;h:form&gt;</code>, you only need to change <code>h:</code> to
 * <code>o:</code>.
 *
 * <h3>Include View Params</h3>
 * <p>
 * The standard {@link UIForm} doesn't put the original view parameters in the action URL that's used for the post-back.
 * Instead, it relies on those view parameters to be stored in the state associated with the standard
 * {@link UIViewParameter}. Via this state those parameters are invisibly re-applied after every post-back.
 * <p>
 * The disadvantage of this invisible retention of view parameters is that the user doesn't see them anymore in the
 * address bar of the browser that is used to interact with the faces application. Copy-pasting the URL from the address
 * bar or refreshing the page by hitting enter inside the address bar will therefore not always yield the expected
 * results.
 * <p>
 * To solve this, this component offers an attribute <code>includeViewParams="true"</code> that will optionally include
 * all view parameters, in exactly the same way that this can be done for <code>&lt;h:link&gt;</code> and
 * <code>&lt;h:button&gt;</code>.
 * <p>
 * This setting is ignored when <code>includeRequestParams="true"</code> or <code>useRequestURI="true"</code> is used.
 *
 * <h3>Include Request Params</h3>
 * <p>
 * As an alternative to <code>includeViewParams</code>, you can use <code>includeRequestParams="true"</code> to
 * optionally include the current GET request query string.
 * <p>
 * This setting overrides the <code>includeViewParams</code>.
 * This setting is ignored when <code>useRequestURI="true"</code> is used.
 *
 * <h3>Use request URI</h3>
 * <p>
 * As an alternative to <code>includeViewParams</code> and <code>includeRequestParams</code>, you can use
 * <code>useRequestURI="true"</code> to use the current request URI, including with the GET request query string, if
 * any. This is particularly useful if you're using FacesViews or forwarding everything to 1 page. Otherwise, by default
 * the current view ID will be used.
 * <p>
 * This setting overrides the <code>includeViewParams</code> and <code>includeRequestParams</code>.
 *
 * <h3>Ignore Validation Failed</h3>
 * <p>
 * In order to properly use the <code>&lt;o:ignoreValidationFailed&gt;</code> tag on an {@link UICommand} component, its
 * parent <code>&lt;h:form&gt;</code> component has to be replaced by this <code>&lt;o:form&gt;</code> component.
 * See also {@link IgnoreValidationFailed}.
 *
 * @since 1.1
 * @author Arjan Tijms
 * @author Bauke Scholtz
 */
@FacesComponent(Form.COMPONENT_TYPE)
public class Form extends UIForm {

	// Constants ------------------------------------------------------------------------------------------------------

	public static final String COMPONENT_TYPE = "org.omnifaces.component.input.Form";

	enum PropertyKeys {
		includeViewParams,
		includeRequestParams,
		useRequestURI
	}

	// Variables ------------------------------------------------------------------------------------------------------

	private final State state = new State(getStateHelper());

	// Actions --------------------------------------------------------------------------------------------------------

	@Override
	public void processValidators(FacesContext context) {
		if (isIgnoreValidationFailed(context)) {
			super.processValidators(new IgnoreValidationFailedFacesContext(context));
		}
		else {
			super.processValidators(context);
		}
	}

	@Override
	public void processUpdates(FacesContext context) {
		if (isIgnoreValidationFailed(context)) {
			super.processUpdates(new IgnoreValidationFailedFacesContext(context));
		}
		else {
			super.processUpdates(context);
		}
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		if (isUseRequestURI()) {
			super.encodeBegin(new ActionURLDecorator(context, useRequestURI));
		}
		else if (isIncludeRequestParams()) {
			super.encodeBegin(new ActionURLDecorator(context, includeRequestParams));
		}
		else if (isIncludeViewParams()) {
			super.encodeBegin(new ActionURLDecorator(context, includeViewParams));
		}
		else {
			super.encodeBegin(context);
		}
	}

	private boolean isIgnoreValidationFailed(FacesContext context) {
		return context.getAttributes().get(IgnoreValidationFailed.class.getName()) == TRUE;
	}


	// Getters/setters ------------------------------------------------------------------------------------------------

	/**
	 * Return whether or not the view parameters should be encoded into the form's action URL.
	 */
	public Boolean isIncludeViewParams() {
		return state.get(includeViewParams, FALSE);
	}

	/**
	 * Set whether or not the view parameters should be encoded into the form's action URL.
	 *
	 * @param includeViewParams
	 *            The state of the switch for encoding view parameters
	 */
	public void setIncludeViewParams(boolean includeViewParams) {
		state.put(PropertyKeys.includeViewParams, includeViewParams);
	}

	/**
	 * Return whether or not the request parameters should be encoded into the form's action URL.
	 * @since 1.5
	 */
	public Boolean isIncludeRequestParams() {
		return state.get(includeRequestParams, FALSE);
	}

	/**
	 * Set whether or not the request parameters should be encoded into the form's action URL.
	 *
	 * @param includeRequestParams
	 *            The state of the switch for encoding request parameters.
	 * @since 1.5
	 */
	public void setIncludeRequestParams(boolean includeRequestParams) {
		state.put(PropertyKeys.includeRequestParams, includeRequestParams);
	}

	/**
	 * Return whether or not the request URI should be used as form's action URL.
	 * @since 1.6
	 */
	public Boolean isUseRequestURI() {
		return state.get(useRequestURI, FALSE);
	}

	/**
	 * Set whether or not the request URI should be used as form's action URL.
	 *
	 * @param useRequestURI
	 *            The state of the switch for using request URI.
	 * @since 1.6
	 */
	public void setUseRequestURI(boolean useRequestURI) {
		state.put(PropertyKeys.useRequestURI, useRequestURI);
	}

	// Nested classes -------------------------------------------------------------------------------------------------

	/**
	 * FacesContext wrapper which performs NOOP during {@link FacesContext#validationFailed()} and
	 * {@link FacesContext#renderResponse()}.
	 *
	 * @author Bauke Scholtz
	 */
	static class IgnoreValidationFailedFacesContext extends FacesContextWrapper {

		private FacesContext wrapped;

		public IgnoreValidationFailedFacesContext(FacesContext wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void validationFailed() {
			// NOOP.
		}

		@Override
		public void renderResponse() {
			// NOOP.
		}

		@Override
		public FacesContext getWrapped() {
			return wrapped;
		}

	}

	/**
	 * Helper class used for creating a FacesContext with a decorated FacesContext -&gt; Application -&gt; ViewHandler
	 * -&gt; getActionURL.
	 *
	 * @author Arjan Tijms
	 */
	static class ActionURLDecorator extends FacesContextWrapper {

		private final FacesContext facesContext;
		private final PropertyKeys type;


		public ActionURLDecorator(FacesContext facesContext, PropertyKeys type) {
			this.facesContext = facesContext;
			this.type = type;
		}

		@Override
		public Application getApplication() {
			return new ApplicationWrapper() {

				private final Application application = ActionURLDecorator.super.getApplication();

				@Override
				public ViewHandler getViewHandler() {
					final ApplicationWrapper outer = this;

					return new ViewHandlerWrapper() {

						private final ViewHandler viewHandler = outer.getWrapped().getViewHandler();

						/**
						 * The actual method we're decorating in order to either include the view parameters into the
						 * action URL, or include the request parameters into the action URL, or use request URI as
						 * action URL.
						 */
						@Override
						public String getActionURL(FacesContext context, String viewId) {
							if (type == useRequestURI) {
								return getRequestURIWithQueryString(context);
							}
							else if (type == includeRequestParams) {
								return context.getExternalContext().encodeBookmarkableURL(
									super.getActionURL(context, viewId), getRequestQueryStringMap(context));
							}
							else if (type == includeViewParams) {
								return context.getExternalContext().encodeBookmarkableURL(
									super.getActionURL(context, viewId), getViewParameterMap(context));
							}
							else {
								return super.getActionURL(context, viewId);
							}
						}

						@Override
						public ViewHandler getWrapped() {
							return viewHandler;
						}
					};
				}

				@Override
				public Application getWrapped() {
					return application;
				}
			};
		}

		@Override
		public FacesContext getWrapped() {
			return facesContext;
		}
	}

}