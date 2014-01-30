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
package org.omnifaces.taghandler;

import static org.omnifaces.taghandler.RenderTimeTagHandlerHelper.collectRenderTimeAttributes;
import static org.omnifaces.taghandler.RenderTimeTagHandlerHelper.createInstance;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.ConverterConfig;
import javax.faces.view.facelets.ConverterHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagHandlerDelegate;

import org.omnifaces.taghandler.RenderTimeTagHandlerHelper.RenderTimeAttributes;
import org.omnifaces.taghandler.RenderTimeTagHandlerHelper.RenderTimeTagHandler;
import org.omnifaces.taghandler.RenderTimeTagHandlerHelper.RenderTimeTagHandlerDelegate;

/**
 * The <code>&lt;o:converter&gt;</code> basically extends the <code>&lt;f:converter&gt;</code> tag family with the
 * possibility to evaluate the value expression in all attributes on a per request basis instead of on a per view
 * build time basis. This allows the developer to change the attributes on a per request basis.
 * <p>
 * When you specify for example the standard <code>&lt;f:convertDateTime&gt;</code> by
 * <code>converterId="javax.faces.DateTime"</code>, then you'll be able to use all its attributes such as
 * <code>pattern</code> and <code>locale</code> as per its documentation, but then with the possibility to supply
 * request based value expressions.
 * <pre>
 * &lt;o:converter converterId="javax.faces.DateTime" pattern="#{item.pattern}" locale="#{item.locale}" /&gt;
 * </pre>
 *
 * @author Bauke Scholtz
 */
public class Converter extends ConverterHandler implements RenderTimeTagHandler {

	// Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * The constructor.
	 * @param config The converter config.
	 */
	public Converter(ConverterConfig config) {
		super(config);
	}

	// Actions --------------------------------------------------------------------------------------------------------

	/**
	 * Create a {@link javax.faces.convert.Converter} based on the <code>binding</code> and/or <code>converterId</code>
	 * attributes as per the standard JSF <code>&lt;f:converter&gt;</code> implementation and collect the render time
	 * attributes. Then create an anonymous <code>Converter</code> implementation which wraps the created
	 * <code>Converter</code> and delegates the methods to it after setting the render time attributes. Finally set the
	 * anonymous implementation on the parent component.
	 * @param context The involved facelet context.
	 * @param parent The parent component to set the <code>Converter</code> on.
	 * @throws IOException If something fails at I/O level.
	 */
	@Override
	public void apply(FaceletContext context, UIComponent parent) throws IOException {
		if (!ComponentHandler.isNew(parent) && UIComponent.getCompositeComponentParent(parent) == null) {
			// If it's not new nor inside a composite component, we're finished.
			return;
		}

		if (!(parent instanceof ValueHolder)) {
			// It's likely a composite component. TagHandlerDelegate will pickup it and pass the target component back.
			super.apply(context, parent);
			return;
		}

		final javax.faces.convert.Converter converter = createInstance(context, this, "converterId");
		final RenderTimeAttributes attributes = collectRenderTimeAttributes(context, this, converter);
		((ValueHolder) parent).setConverter(new RenderTimeConverter() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getAsObject(FacesContext context, UIComponent component, String value) {
				attributes.invokeSetters(context.getELContext(), converter);
				return converter.getAsObject(context, component, value);
			}

			@Override
			public String getAsString(FacesContext context, UIComponent component, Object value) {
				attributes.invokeSetters(context.getELContext(), converter);
				return converter.getAsString(context, component, value);
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T create(Application application, String id) {
		return (T) application.createConverter(id);
	}

	@Override
	public TagAttribute getTagAttribute(String name) {
		return getAttribute(name);
	}

	@Override
	protected TagHandlerDelegate getTagHandlerDelegate() {
		return new RenderTimeTagHandlerDelegate(this, super.getTagHandlerDelegate());
	}

	@Override
	public boolean isDisabled(FaceletContext context) {
		return false; // This attribute isn't supported on converters anyway.
	}

	// Nested classes -------------------------------------------------------------------------------------------------

	/**
	 * So that we can have a serializable converter.
	 *
	 * @author Bauke Scholtz
	 */
	protected static abstract class RenderTimeConverter implements javax.faces.convert.Converter, Serializable {
		private static final long serialVersionUID = 1L;
	}

}