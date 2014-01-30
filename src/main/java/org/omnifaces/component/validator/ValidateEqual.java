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
package org.omnifaces.component.validator;

import java.util.HashSet;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

/**
 * <strong>ValidateEqual</strong> validates if ALL of the given <code>UIInput</code> components have the same value.
 * The default message is
 * <blockquote>{0}: Please fill out the same value for all of those fields</blockquote>
 * <p>
 * For general usage instructions, refer {@link ValidateMultipleFields} documentation.
 *
 * @author Bauke Scholtz
 */
@FacesComponent(ValidateEqual.COMPONENT_TYPE)
public class ValidateEqual extends ValidateMultipleFields {

	// Public constants -----------------------------------------------------------------------------------------------

	/** The standard component type. */
	public static final String COMPONENT_TYPE = "org.omnifaces.component.validator.ValidateEqual";

	// Private constants ----------------------------------------------------------------------------------------------

	private static final String DEFAULT_MESSAGE = "{0}: Please fill out the same value for all of those fields";

	// Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * The default constructor sets the default message.
	 */
	public ValidateEqual() {
		super(DEFAULT_MESSAGE);
	}

	// Actions --------------------------------------------------------------------------------------------------------

	/**
	 * Validate if all values are equal.
	 */
	@Override
	public boolean validateValues(FacesContext context, List<UIInput> inputs, List<Object> values) {
		return (new HashSet<Object>(values).size() == 1);
	}

}