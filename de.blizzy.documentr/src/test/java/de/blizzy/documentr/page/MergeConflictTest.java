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
package de.blizzy.documentr.page;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MergeConflictTest {
	private static final String TEXT = "text"; //$NON-NLS-1$
	private static final String COMMIT = "commit"; //$NON-NLS-1$
	
	private MergeConflict conflict;

	@Before
	public void setUp() {
		conflict = new MergeConflict(TEXT, COMMIT);
	}
	
	@Test
	public void getText() {
		assertEquals(TEXT, conflict.getText());
	}
	
	@Test
	public void getNewBaseCommit() {
		assertEquals(COMMIT, conflict.getNewBaseCommit());
	}
}
