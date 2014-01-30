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
package org.omnifaces.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.beans.PropertyEditor;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.BeanValidator;
import javax.faces.validator.RequiredValidator;
import javax.faces.validator.Validator;
import javax.inject.Qualifier;

import org.omnifaces.cdi.param.Attribute;
import org.omnifaces.cdi.param.ParamValue;
import org.omnifaces.util.Utils;

/**
 * Specifies a request parameter that is to be injected into a managed bean within a JSF context, with
 * full support for a converter and one or more validators. Converters and validators can optionally
 * have attributes.
 * <p>
 * By default the name of the request parameter is taken from the name of the variable into which
 * injection takes place. It can be optionally specified.
 * <p>
 * String values can be injected without a converter, other types need a converter.
 * <p>
 * Injection should be into a field of type {@link ParamValue}, with <code>V</code> the actual type of the
 * (converted) request parameter.
 * <p>
 * The following is an example of the injection of a request parameter <code>user</code> following
 * a request such as <code>http://example.com/mypage?user=100</code>:
 *
 * <pre>
 * {@literal @}Inject {@literal @}Param(
 * 	converter="#{userconverter}"
 * 	validator="#{priviledgedUser}"
 * )
 * private ParamValue&lt;User&gt; user;
 * </pre>
 * <p>
 * If conversion or validation fails, a {@link ParamValue} is injected, but it will contain a null value. The
 * conversion and validation messages (if any) will be set in the JSF context then, and {@link FacesContext#isValidationFailed()}
 * will return true;
 *
 * @since 1.6
 * @author Arjan Tijms
 *
 */
@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface Param {

	/**
	 * (Optional) The name of the request parameter. If not specified the name of the injection target field will be used.
	 */
	@Nonbinding	String name() default "";

	/**
	 * (Optional) the label used to refer to the request parameter. If not specified the name of the request parameter.
	 *
	 */
	@Nonbinding	String label() default "";

	/**
	 * (Optional/Required) The converter to be used for converting the request parameter to the type that is to be injected.
	 * Optional if the target type is String, otherwise required.
	 * <p>
	 * A converter can be specified in 3 ways:
	 * <ol>
	 * <li>A string value representing the <em>converter-id</em> as used by {@link
     * javax.faces.application.Application#createConverter(String)}
     * <li>An EL expression that resolves to a String representing the <em>converter-id</em>
     * <li>An EL expression that resolves to a {@link Converter} instance.
     * </ol>
     * <p>
     * If this attribute is specified in addition to {@link Param#converterClass()}, this attribute takes precedence.
	 */
	@Nonbinding String converter() default "";

	/**
	 * (Optional) Flag indicating if this request parameter is required (must be present) or not. The required check is done
	 * after conversion and before validation. A value is said to be not present if it turns out to be empty according to
	 * the semantics of {@link Utils#isEmpty(Object)}.
	 *
	 */
	@Nonbinding boolean required() default false;

	/**
	 * (Optional) The validators to be used for validating the (converted) request parameter.
	 *
	 * <p>
	 * A validator can be specified in 3 ways:
	 * <ol>
	 * <li>A string value representing the <em>validator-id</em> as used by {@link
     * javax.faces.application.Application#createValidator(String)}
     * <li>An EL expression that resolves to a String representing the <em>validator-id</em>
     * <li>An EL expression that resolves to a {@link Validator} instance.
     * </ol>
     * <p>
     * If this attribute is specified in addition to {@link Param#validatorClasses()} then the validators from both
     * attributes will be added to the final collection of validators. The validators from this attribute will however
     * be called first.
	 */
	@Nonbinding String[] validators() default {};

	/**
	 * (Optional) Class of the converter to be used for converting the request parameter to the type that is to be injected.
	 *
	 */
	@Nonbinding Class<? extends Converter> converterClass() default Converter.class;

	/**
	 * (Optional) Class of one ore more validators to be used for validating the (converted) request parameter.
	 */
	@Nonbinding Class<? extends Validator>[] validatorClasses() default {};

	/**
	 * (Optional) Attributes that will be set on the converter instance obtained from {@link Param#converter()} or {@link Param#converterClass()}.
	 * <p>
	 * For each attribute the converter instance should have a writable JavaBeans property with the same name. The value can be a string literal
	 * or an EL expression. String literals are coerced if necessary if there's a {@link PropertyEditor} available (the JDK provides these for
	 * the primitive types and their corresponding boxed types).
	 * <p>
	 * Attributes for which the converter doesn't have a property (setter) are silently ignored.
	 */
	@Nonbinding Attribute[] converterAttributes() default {};

	/**
	 * (Optional) Attributes that will be set on each of the validator instances obtained from {@link Param#validators()()} and {@link Param#validatorClasses()()}.
	 * <p>
	 * For each attribute the validator instances should have a writable JavaBeans property with the same name. The value can be a string literal
	 * or an EL expression. String literals are coerced if necessary if there's a {@link PropertyEditor} available (the JDK provides these for
	 * the primitive types and their corresponding boxed types).
	 * <p>
	 * Attributes for which any given validator doesn't have a property (setter) are silently ignored.
	 */
	@Nonbinding Attribute[] validatorAttributes() default {};

	/**
	 * (Optional) A message that will be used if conversion fails instead of the message set by the converter.
	 * <p>
	 * The value for which conversion failed is available as <code>{0}</code>. The label associated with this
	 * parameter value (see the {@link Param#label()} attribute) is available as <code>{1}</code>.
	 *
	 */
	@Nonbinding String converterMessage() default "";

	/**
	 * (Optional) A message that will be used if validation fails instead of the message set by the validator(s).
	 * <p>
	 * The value for which validation failed is available as <code>{0}</code>. The label associated with this
	 * parameter value (see the {@link Param#label()} attribute) is available as <code>{1}</code>.
	 *
	 */
	@Nonbinding String validatorMessage() default "";

	/**
	 * (Optional) A message that will be used if a non-empty value is submitted instead of the default message associated
	 * with the {@link RequiredValidator}.
	 * <p>
	 * The (empty) value for which the required check failed is available as <code>{0}</code>. (this will be either null or the empty string)
	 * The label associated with this parameter value (see the {@link Param#label()} attribute) is available as <code>{1}</code>.
	 *
	 */
	@Nonbinding String requiredMessage() default "";

	/**
	 * (Optional) Flag that disables bean validation for this instance.
	 * <p>
	 * <b>NOTE:</b> bean validation at the moment (OmniFaces 1.6) is done against the {@link ParamValue} that is injected. In many cases this will
	 * be of limited use. We hope to directly inject the converted type in OmniFaces 1.7 and then bean validation will make more sense.
	 * <p>
	 * If <code>true</code> no bean validation will be attempted. If <code>false</code> (the default) no specific action is taken, and it
	 * will depend on the availability of bean validation and the global {@link BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} setting
	 * whether bean validation is attempted or not.
	 *
	 */
	@Nonbinding boolean disableBeanValidation() default false;

	/**
	 * (Optional) Flag that overrides the global {@link BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} setting.
	 * <p>
	 * If <code>true</code> bean validation will be performed for this instance (given that bean validation is available) despite
	 * it globally being disabled. If <code>false</code> (the default) no specific action is taken.
	 *
	 */
	@Nonbinding boolean overrideGlobalBeanValidationDisabled() default false;

}
