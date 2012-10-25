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
package de.blizzy.documentr.repository;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mock;

import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;

public class LockedRepositoryTest extends AbstractDocumentrTest {
	@Mock
	private LockManager lockManager;

	@Test
	public void lockAndUnlockProjectCentral() {
		Lock lock = new Lock(Thread.currentThread());
		when(lockManager.lockProjectCentral("project")).thenReturn(lock); //$NON-NLS-1$

		ILockedRepository repo = LockedRepository.lockProjectCentral("project", lockManager); //$NON-NLS-1$
		Closeables.closeQuietly(repo);
		
		verify(lockManager).unlock(lock);
	}
	
	@Test
	public void lockAndUnlockProjectBranch() {
		Lock lock = new Lock(Thread.currentThread());
		when(lockManager.lockProjectBranch("project", "branch")).thenReturn(lock); //$NON-NLS-1$ //$NON-NLS-2$
		
		ILockedRepository repo = LockedRepository.lockProjectBranch("project", "branch", lockManager); //$NON-NLS-1$ //$NON-NLS-2$
		Closeables.closeQuietly(repo);
		
		verify(lockManager).unlock(lock);
	}
	
	@Test(expected=IllegalStateException.class)
	public void rMustThrowIllegalStateExceptionIfNoRepository() {
		Lock lock = new Lock(Thread.currentThread());
		when(lockManager.lockProjectCentral("project")).thenReturn(lock); //$NON-NLS-1$

		ILockedRepository repo = LockedRepository.lockProjectCentral("project", lockManager); //$NON-NLS-1$
		repo.r();
	}
}
