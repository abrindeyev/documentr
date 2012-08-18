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
package de.blizzy.documentr.web.markdown.macro.impl;

import org.apache.commons.lang3.StringEscapeUtils;

import de.blizzy.documentr.web.markdown.macro.AbstractMacro;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;

public class TwitterMacro extends AbstractMacro {
	public static final MacroDescriptor DESCRIPTOR = new MacroDescriptor("twitter", //$NON-NLS-1$
			TwitterMacro.class, "{{twitter [SEARCHTERMS]/}}"); //$NON-NLS-1$

	@Override
	public String getHtml(String body) {
		String searchTerms = getParameters().trim();
		@SuppressWarnings("nls")
		String html = "<script charset=\"UTF-8\" src=\"http://widgets.twimg.com/j/2/widget.js\"></script>\n" +
			"<script>\n" +
			"new TWTR.Widget({" +
				"version: 2," +
				"type: 'search'," +
				"search: '" + StringEscapeUtils.escapeEcmaScript(searchTerms) + "'," +
				"interval: 15000," +
				"title: ''," +
				"subject: ''," +
				"width: 250," +
				"height: 300," +
				"features: {" +
					"scrollbar: true," +
					"loop: false," +
					"live: true," +
					"behavior: 'default'" +
				"}" +
			"}).render().start(); documentr.fixTwitterCss();\n" +
			"</script>\n";
		return html;
	}
}
