/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

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

import lombok.Getter;
import de.blizzy.documentr.util.NotFoundException;
import de.blizzy.documentr.util.Util;

public class PageNotFoundException extends NotFoundException {
	@Getter
	private String projectName;
	@Getter
	private String branchName;
	@Getter
	private String path;

	public PageNotFoundException(String projectName, String branchName, String path) {
		super("page not found: " + projectName + "/" + branchName + "/" + Util.toUrlPagePath(path)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
	}
}
