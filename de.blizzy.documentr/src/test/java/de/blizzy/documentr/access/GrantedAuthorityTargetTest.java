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
package de.blizzy.documentr.access;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

public class GrantedAuthorityTargetTest {
	@Test
	public void getTargetId() {
		GrantedAuthorityTarget target = new GrantedAuthorityTarget("project", Type.PROJECT); //$NON-NLS-1$
		assertEquals("project", target.getTargetId()); //$NON-NLS-1$
	}

	@Test
	public void getType() {
		GrantedAuthorityTarget target = new GrantedAuthorityTarget("project", Type.PROJECT); //$NON-NLS-1$
		assertSame(Type.PROJECT, target.getType());
	}
	
	@Test
	public void testEquals() {
		assertEqualsContract(
				new GrantedAuthorityTarget("project", Type.PROJECT), //$NON-NLS-1$
				new GrantedAuthorityTarget("project", Type.PROJECT), //$NON-NLS-1$
				new GrantedAuthorityTarget("project", Type.PROJECT), //$NON-NLS-1$
				new GrantedAuthorityTarget("project2", Type.PROJECT)); //$NON-NLS-1$
	}
	
	@Test
	public void testHashCode() {
		assertHashCodeContract(
				new GrantedAuthorityTarget("project", Type.PROJECT), //$NON-NLS-1$
				new GrantedAuthorityTarget("project", Type.PROJECT)); //$NON-NLS-1$
	}
}
