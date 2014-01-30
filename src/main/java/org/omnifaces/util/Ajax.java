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
package org.omnifaces.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;

import org.omnifaces.context.OmniPartialViewContext;

/**
 * Collection of utility methods for working with {@link PartialViewContext}. There are also shortcuts to the current
 * {@link OmniPartialViewContext} instance.
 *
 * @author Bauke Scholtz
 * @since 1.2
 */
public final class Ajax {

	// Constants ------------------------------------------------------------------------------------------------------

	private static final String ERROR_ARGUMENTS_LENGTH =
		"The arguments length must be even. Encountered %d items.";
	private static final String ERROR_ARGUMENT_TYPE =
		"The argument name must be a String. Encountered type '%s' with value '%s'.";

	// Constructors ---------------------------------------------------------------------------------------------------

	private Ajax() {
		// Hide constructor.
	}

	// Shortcuts ------------------------------------------------------------------------------------------------------

	/**
	 * Returns the current partial view context (the ajax context).
	 * <p>
	 * <i>Note that whenever you absolutely need this method to perform a general task, you might want to consider to
	 * submit a feature request to OmniFaces in order to add a new utility method which performs exactly this general
	 * task.</i>
	 * @return The current partial view context.
	 * @see FacesContext#getPartialViewContext()
	 */
	public static PartialViewContext getContext() {
		return FacesContext.getCurrentInstance().getPartialViewContext();
	}

	/**
	 * Update the given client IDs in the current ajax response. Note that those client IDs should not start with the
	 * naming container separator character like <code>:</code>. This method also supports the client ID keywords
	 * <code>@all</code>, <code>@form</code> and <code>@this</code> which respectively refers the entire view, the
	 * currently submitted form as obtained by {@link Components#getCurrentForm()} and the currently processed
	 * component as obtained by {@link UIComponent#getCurrentComponent(FacesContext)}. Any other client ID starting
	 * with <code>@</code> is by design ignored, including <code>@none</code>.
	 * @param clientIds The client IDs to be updated in the current ajax response.
	 * @see PartialViewContext#getRenderIds()
	 */
	public static void update(String... clientIds) {
		PartialViewContext context = getContext();
		Collection<String> renderIds = context.getRenderIds();

		for (String clientId : clientIds) {
			if (clientId.charAt(0) != '@') {
				renderIds.add(clientId);
			}
			else if (clientId.equals("@all")) {
				context.setRenderAll(true);
			}
			else if (clientId.equals("@form")) {
			    UIComponent currentForm = Components.getCurrentForm();

			    if (currentForm != null) {
			    	renderIds.add(currentForm.getClientId());
			    }
			}
			else if (clientId.equals("@this")) {
			    UIComponent currentComponent = Components.getCurrentComponent();

			    if (currentComponent != null) {
			    	renderIds.add(currentComponent.getClientId());
			    }
			}
		}
	}

	/**
	 * Update the entire view.
	 * @see PartialViewContext#setRenderAll(boolean)
	 * @since 1.5
	 */
	public static void updateAll() {
		getContext().setRenderAll(true);
	}

	/**
	 * Update the row of the given {@link UIData} component at the given zero-based row index. This will basically
	 * update all direct children of all {@link UIColumn} components at the given row index.
	 * <p>
	 * Note that the to-be-updated direct child of {@link UIColumn} must be a fullworthy JSF UI component which renders
	 * a concrete HTML element to the output, so that JS/ajax can update it. So if you have due to design restrictions
	 * for example a <code>&lt;h:panelGroup rendered="..."&gt;</code> without an ID, then you should give it an ID.
	 * This way it will render a <code>&lt;span id="..."&gt;</code> which is updateable by JS/ajax.
	 * @param table The {@link UIData} component.
	 * @param index The zero-based index of the row to be updated.
	 * @since 1.3
	 */
	public static void updateRow(UIData table, int index) {
		if (index < 0 || table.getRowCount() < 1 || table.getChildCount() == 0) {
			return;
		}

		int rowCount = (table.getRows() == 0) ? table.getRowCount() : table.getRows();

		if (index >= rowCount) {
			return;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		String tableId = table.getClientId(context);
		char separator = UINamingContainer.getSeparatorChar(context);
		Collection<String> renderIds = getContext().getRenderIds();

		for (UIComponent column : table.getChildren()) {
			if (!(column instanceof UIColumn)) {
				continue;
			}

			for (UIComponent cell : column.getChildren()) {
				renderIds.add(String.format("%s%c%d%c%s", tableId, separator, index, separator, cell.getId()));
			}
		}
	}

	/**
	 * Update the column of the given {@link UIData} component at the given zero-based column index. This will basically
	 * update all direct children of the {@link UIColumn} component at the given column index in all rows. The column
	 * index is the physical column index and does not depend on whether one or more columns is rendered or not (i.e. it
	 * is not necessarily the same column index as the enduser sees in the UI).
	 * <p>
	 * Note that the to-be-updated direct child of {@link UIColumn} must be a fullworthy JSF UI component which renders
	 * a concrete HTML element to the output, so that JS/ajax can update it. So if you have due to design restrictions
	 * for example a <code>&lt;h:panelGroup rendered="..."&gt;</code> without an ID, then you should give it an ID.
	 * This way it will render a <code>&lt;span id="..."&gt;</code> which is updateable by JS/ajax.
	 * @param table The {@link UIData} component.
	 * @param index The zero-based index of the column to be updated.
	 * @since 1.3
	 */
	public static void updateColumn(UIData table, int index) {
		if (index < 0 || table.getRowCount() < 1 || index > table.getChildCount()) {
			return;
		}

		int rowCount = (table.getRows() == 0) ? table.getRowCount() : table.getRows();

		if (rowCount == 0) {
			return;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		String tableId = table.getClientId(context);
		char separator = UINamingContainer.getSeparatorChar(context);
		Collection<String> renderIds = getContext().getRenderIds();
		int columnIndex = 0;

		for (UIComponent column : table.getChildren()) {
			if (!(column instanceof UIColumn) || columnIndex++ != index) {
				continue;
			}

			for (UIComponent cell : column.getChildren()) {
				String cellId = cell.getId();

				for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
					renderIds.add(String.format("%s%c%d%c%s", tableId, separator, rowIndex, separator, cellId));
				}
			}

			break;
		}
	}

	/**
	 * Execute the given scripts on complete of the current ajax response.
	 * @param scripts The scripts to be executed.
	 * @see OmniPartialViewContext#addCallbackScript(String)
	 */
	public static void oncomplete(String... scripts) {
		OmniPartialViewContext context = OmniPartialViewContext.getCurrentInstance();

		for (String script : scripts) {
			context.addCallbackScript(script);
		}
	}

	/**
	 * Add the given data argument to the current ajax response. They are as JSON object available by
	 * <code>OmniFaces.Ajax.data</code>.
	 * @param name The argument name.
	 * @param value The argument value.
	 * @see OmniPartialViewContext#addArgument(String, Object)
	 */
	public static void data(String name, Object value) {
		OmniPartialViewContext.getCurrentInstance().addArgument(name, value);
	}

	/**
	 * Add the given data arguments to the current ajax response. The arguments length must be even. Every first and
	 * second argument is considered the name and value pair. The name must always be a {@link String}. They are as JSON
	 * object available by <code>OmniFaces.Ajax.data</code>.
	 * @param namesValues The argument names and values.
	 * @throws IllegalArgumentException When the arguments length is not even, or when a name is not a string.
	 * @see OmniPartialViewContext#addArgument(String, Object)
	 */
	public static void data(Object... namesValues) {
		if (namesValues.length % 2 != 0) {
			throw new IllegalArgumentException(String.format(ERROR_ARGUMENTS_LENGTH, namesValues.length));
		}

		OmniPartialViewContext context = OmniPartialViewContext.getCurrentInstance();

		for (int i = 0; i < namesValues.length; i+= 2) {
			if (!(namesValues[i] instanceof String)) {
				String type = (namesValues[i]) != null ? namesValues[i].getClass().getName() : "null";
				throw new IllegalArgumentException(String.format(ERROR_ARGUMENT_TYPE, type, namesValues[i]));
			}

			context.addArgument((String) namesValues[i], namesValues[i + 1]);
		}
	}

	/**
	 * Add the given mapping of data arguments to the current ajax response. They are as JSON object available by
	 * <code>OmniFaces.Ajax.data</code>.
	 * @param data The mapping of data arguments.
	 * @see OmniPartialViewContext#addArgument(String, Object)
	 */
	public static void data(Map<String, Object> data) {
		OmniPartialViewContext context = OmniPartialViewContext.getCurrentInstance();

		for (Entry<String, Object> entry : data.entrySet()) {
			context.addArgument(entry.getKey(), entry.getValue());
		}
	}

}