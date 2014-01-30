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

import java.io.IOException;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.omnifaces.el.MethodExpressionValueExpressionAdapter;
import org.omnifaces.el.ValueExpressionMethodWrapper;
import org.omnifaces.util.Hacks;

/**
 * This handler wraps a value expression that's actually a method expression by another value expression that returns a method expression
 * that gets the value of first value expression, which as "side-effect" executes the original method expression.
 *
 * <p>
 * This somewhat over-the-top chain of wrapping is done so a method expression can be passed into a Facelet tag as parameter.
 *
 * @author Arjan Tijms
 *
 */
public class MethodParam extends TagHandler {

	private final TagAttribute name;
	private final TagAttribute value;

	public MethodParam(TagConfig config) {
		super(config);
		this.name = this.getRequiredAttribute("name");
		this.value = this.getRequiredAttribute("value");
	}

	@Override
	public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
		String nameStr = name.getValue(ctx);

		// The original value expression we get inside the Facelets tag, that's actually the method expression passed-in by the user.
		ValueExpression valueExpression = value.getValueExpression(ctx, Object.class);

		// A method expression that wraps the value expression and uses its own invoke method to get the value from the wrapped expression.
		MethodExpression methodExpression = new MethodExpressionValueExpressionAdapter(valueExpression);

		// Using the variable mapper so the expression is scoped to the body of the Facelets tag. Since the variable mapper only accepts
		// value expressions, we once again wrap it by a value expression that directly returns the method expression.

		ValueExpression valueExpressionWrapper;

		// JUEL older than 2.2.6 had the expectation that the value expression only wraps a Method instance that can be statically called.
		if (Hacks.isJUELUsed(ctx.getExpressionFactory()) && !Hacks.isJUELSupportingMethodExpression()) {
			valueExpressionWrapper = new ValueExpressionMethodWrapper(methodExpression);
		} else {
			// Sun/Apache EL and JUEL 2.2.6 and higher can call a method expression wrapped in a value expression.
			valueExpressionWrapper = ctx.getExpressionFactory().createValueExpression(methodExpression, MethodExpression.class);
		}

		ctx.getVariableMapper().setVariable(nameStr, valueExpressionWrapper);
	}

}