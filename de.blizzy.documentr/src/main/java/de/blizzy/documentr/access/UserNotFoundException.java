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

import de.blizzy.documentr.NotFoundException;

/** Thrown when no user is found that has a specific login name. */
public class UserNotFoundException extends NotFoundException {
	private String loginName;

	public UserNotFoundException(String loginName) {
		super("user not found: " + loginName); //$NON-NLS-1$
		
		this.loginName = loginName;
	}
	
	public String getLoginName() {
		return loginName;
	}
}
