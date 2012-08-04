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
import static org.mockito.Mockito.*;

import org.junit.Test;

public class CommitCherryPickResultTest {
	@Test
	public void getPageVersion() {
		PageVersion pageVersion = mock(PageVersion.class);
		CommitCherryPickResult result = new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.UNKNOWN);
		assertEquals(pageVersion, result.getPageVersion());
	}

	@Test
	public void getStatus() {
		PageVersion pageVersion = mock(PageVersion.class);
		CommitCherryPickResult result = new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.UNKNOWN);
		assertSame(CommitCherryPickResult.Status.UNKNOWN, result.getStatus());

		result = new CommitCherryPickResult(pageVersion, "conflictText"); //$NON-NLS-1$
		assertSame(CommitCherryPickResult.Status.CONFLICT, result.getStatus());
	}

	@Test
	public void getConflictText() {
		PageVersion pageVersion = mock(PageVersion.class);
		CommitCherryPickResult result = new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.UNKNOWN);
		assertNull(result.getConflictText());

		result = new CommitCherryPickResult(pageVersion, "conflictText"); //$NON-NLS-1$
		assertEquals("conflictText", result.getConflictText()); //$NON-NLS-1$
	}
}
