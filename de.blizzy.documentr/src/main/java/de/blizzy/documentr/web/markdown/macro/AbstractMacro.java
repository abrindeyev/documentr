/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.blizzy.documentr.web.markdown.macro;

import de.blizzy.documentr.pagestore.IPageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

public abstract class AbstractMacro implements IMacro {
	private HtmlSerializerContext context;
	private IPageStore pageStore;
	private String params;

	@Override
	public void setParameters(String params) {
		this.params = params;
	}
	
	protected String getParameters() {
		return params;
	}

	@Override
	public void setHtmlSerializerContext(HtmlSerializerContext context) {
		this.context = context;
	}
	
	protected HtmlSerializerContext getHtmlSerializerContext() {
		return context;
	}

	@Override
	public void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}
	
	protected IPageStore getPageStore() {
		return pageStore;
	}
}
