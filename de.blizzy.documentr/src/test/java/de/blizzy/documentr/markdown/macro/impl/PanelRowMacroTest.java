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
package de.blizzy.documentr.markdown.macro.impl;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;

public class PanelRowMacroTest extends AbstractDocumentrTest {
	@Mock
	private BeanFactory beanFactory;
	@InjectMocks
	private PanelRowMacro macro;

	@Before
	public void setUp() {
		when(beanFactory.getBean(MessageSourceMacroDescriptor.ID, "panelrow")) //$NON-NLS-1$
			.thenReturn(new MessageSourceMacroDescriptor("panelrow")); //$NON-NLS-1$
	}
	
	@Test
	public void getDescriptor() {
		IMacroDescriptor descriptor = macro.getDescriptor();
		assertEquals("panelrow", descriptor.getMacroName()); //$NON-NLS-1$
		assertEquals("{{panelrow}}[CONTENTS]{{/panelrow}}", descriptor.getInsertText()); //$NON-NLS-1$
	}
	
	@Test
	public void createRunnable() {
		IMacroRunnable runnable = macro.createRunnable();
		assertTrue(runnable instanceof PanelRowMacroRunnable);
	}
}