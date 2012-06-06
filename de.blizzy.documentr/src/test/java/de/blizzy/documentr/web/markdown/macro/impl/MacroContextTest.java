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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.page.IPageStore;

public class MacroContextTest {
	@Test
	public void getPageStore() {
		IPageStore pageStore = mock(IPageStore.class);
		DocumentrPermissionEvaluator permissionEvaluator = mock(DocumentrPermissionEvaluator.class);
		MacroContext context = new MacroContext(pageStore, permissionEvaluator);
		assertSame(pageStore, context.getPageStore());
	}

	@Test
	public void getPermissionEvaluator() {
		IPageStore pageStore = mock(IPageStore.class);
		DocumentrPermissionEvaluator permissionEvaluator = mock(DocumentrPermissionEvaluator.class);
		MacroContext context = new MacroContext(pageStore, permissionEvaluator);
		assertSame(permissionEvaluator, context.getPermissionEvaluator());
	}
}