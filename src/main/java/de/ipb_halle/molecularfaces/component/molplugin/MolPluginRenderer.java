/*
 * MolecularFaces
 * Copyright 2021 Leibniz-Institut für Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package de.ipb_halle.molecularfaces.component.molplugin;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.render.Renderer;

import de.ipb_halle.molecularfaces.util.RendererUtils;

/**
 * This {@link Renderer} offers functionalities for the specific renderers of
 * {@link MolPluginCore}-derived molecular structure plugin components.
 * 
 * @author flange
 */
public abstract class MolPluginRenderer extends Renderer {
	@Override
	public void decode(FacesContext context, UIComponent component) {
		MolPluginCore plugin = (MolPluginCore) component;

		if (!plugin.isReadonly()) {
			RendererUtils.decodeComponent(context, plugin);
		}
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {
		return RendererUtils.convertSubmittedValueToObject(context, component, submittedValue);
	}

	/**
	 * Generate the inline css style for the &lt;div&gt; element of the rendered
	 * editor or viewer.
	 * 
	 * @param plugin component of the molecular structure plugin
	 * @return inline css style
	 */
	protected String generateDivStyle(MolPluginCore plugin) {
		StringBuilder sb = new StringBuilder(128);

		// width attribute
		sb.append("width:").append(plugin.getWidth()).append("px;");

		// height attribute
		sb.append("height:").append(plugin.getHeight()).append("px;");

		// border attribute
		if (plugin.isBorder()) {
			sb.append("border:solid;border-width:1px;");
		}

		return sb.toString();
	}
}
