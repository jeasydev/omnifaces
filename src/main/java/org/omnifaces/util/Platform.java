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
package org.omnifaces.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static javax.faces.validator.BeanValidator.VALIDATOR_FACTORY_KEY;
import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.omnifaces.util.Faces.getApplicationAttribute;
import static org.omnifaces.util.Faces.setApplicationAttribute;

import java.util.logging.Logger;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * This class provides access to (Java EE) platform services from the view point of JSF.
 * <p>
 * Note that this utility class can only be used in a JSF environment and is thus not
 * a Java EE general way to obtain platform services.
 * 
 * @since 1.6
 * @author Arjan Tijms
 */
public final class Platform {
	
	// Constants ------------------------------------------------------------------------------------------------------
	
	public static final String BEAN_VALIDATION_AVAILABLE = "org.omnifaces.BEAN_VALIDATION_AVAILABLE";
	private static final Logger logger = Logger.getLogger(Platform.class.getName());
	
	
	// Constructors ---------------------------------------------------------------------------------------------------
	
	private Platform() {
		// Hide constructor.
	}
	
	
	// Bean Validation ------------------------------------------------------------------------------------------------
	
	public static ValidatorFactory getBeanValidatorFactory() {
		
		ValidatorFactory validatorFactory = getApplicationAttribute(VALIDATOR_FACTORY_KEY);
		
		if (validatorFactory == null) {
			validatorFactory = buildDefaultValidatorFactory();
			setApplicationAttribute(VALIDATOR_FACTORY_KEY, validatorFactory);
		}
		
		return validatorFactory;
	}
	
	public static Validator getBeanValidator() {
		return getBeanValidatorFactory().getValidator();
	}
	
	public static boolean isBeanValidationAvailable() {
		
		Boolean beanValidationAvailable = getApplicationAttribute(BEAN_VALIDATION_AVAILABLE);
		
		if (beanValidationAvailable == null) {
			try {
				Class.forName("javax.validation.Validation");
				getBeanValidator();
				beanValidationAvailable = TRUE;
			} catch (Throwable e) {
				beanValidationAvailable = FALSE;
				logger.warning("Bean validation not available.");
			}
			
			setApplicationAttribute(BEAN_VALIDATION_AVAILABLE, beanValidationAvailable);
		}
		
		return beanValidationAvailable;
	}
	

}
