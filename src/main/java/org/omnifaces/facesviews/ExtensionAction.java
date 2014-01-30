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
package org.omnifaces.facesviews;

/**
 * The action that is done when a Faces Views request with an extension is done.
 * <p>
 * Note that this is only used for views that were discovered via Faces Views. It has
 * no affect on other resources, even if they have the same extension.
 *
 * @author Arjan Tijms
 * @since 1.4
 */
public enum ExtensionAction {

	/** Send a 404 (not found), makes it look like e.g. "foo.xhtml" never existed and there's only "foo". */
	SEND_404,

	/** Redirects to the same URL, but with the extension removed. E.g. "/foo.xhtml" is redirected to "/foo". */
	REDIRECT_TO_EXTENSIONLESS,

	/** No special action is taken. Both "/foo.xhtml" and "/foo" are processed as-if they were separate views (with same content). */
	PROCEED;

}