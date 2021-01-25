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
package de.ipb_halle.molecularfaces;

import javax.faces.context.FacesContext;

public interface AddResourceRenderer {
	/**
	 * Adds resources to the component tree. See
	 * <a href="https://stackoverflow.com/a/12451778">https://stackoverflow.com/a/12451778</a> for a good implementation example. This
	 * method is called before the render response in the {@link javax.faces.event.PostAddToViewEvent}.
	 * 
	 * @param context Faces context for the component calling this method.
	 */
	public void addResources(FacesContext context);
}