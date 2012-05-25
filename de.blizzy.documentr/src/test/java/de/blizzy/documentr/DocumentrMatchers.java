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
package de.blizzy.documentr;

import static org.mockito.Matchers.*;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageTextData;
import de.blizzy.documentr.web.access.UserForm;
import de.blizzy.documentr.web.branch.BranchForm;
import de.blizzy.documentr.web.page.PageForm;
import de.blizzy.documentr.web.project.ProjectForm;

public final class DocumentrMatchers {
	private DocumentrMatchers() {}

	public static Page argPage(final String parentPagePath, final String title, final String text) {
		Matcher<Page> matcher = new ArgumentMatcher<Page>() {
			@Override
			public boolean matches(Object argument) {
				Page page = (Page) argument;
				String pageText = null;
				if (page.getData() instanceof PageTextData) {
					pageText = ((PageTextData) page.getData()).getText();
				}
				return StringUtils.equals(page.getParentPagePath(), parentPagePath) &&
						StringUtils.equals(page.getTitle(), title) &&
						StringUtils.equals(pageText, text);
			}
		};
		return argThat(matcher);
	}

	public static PageForm argPageForm(final String projectName, final String branchName, final String path,
			final String parentPagePath, final String title, final String text) {
		
		Matcher<PageForm> matcher = new ArgumentMatcher<PageForm>() {
			@Override
			public boolean matches(Object argument) {
				PageForm form = (PageForm) argument;
				return StringUtils.equals(form.getProjectName(), projectName) &&
						StringUtils.equals(form.getBranchName(), branchName) &&
						StringUtils.equals(form.getPath(), path) &&
						StringUtils.equals(form.getParentPagePath(), parentPagePath) &&
						StringUtils.equals(form.getTitle(), title) &&
						StringUtils.equals(form.getText(), text);
			}
		};
		return argThat(matcher);
	}

	public static User argUser(final String loginName, final String password, final boolean disabled, final boolean admin) {
		Matcher<User> matcher = new ArgumentMatcher<User>() {
			@Override
			public boolean matches(Object argument) {
				User user = (User) argument;
				return StringUtils.equals(user.getLoginName(), loginName) &&
						StringUtils.equals(user.getPassword(), password) &&
						(user.isDisabled() == disabled) &&
						(user.isAdmin() == admin);
			}
		};
		return argThat(matcher);
	}
	
	public static UserForm argUserForm(final String loginName, final String password1, final String password2,
			final boolean disabled, final boolean admin) {
		
		Matcher<UserForm> matcher = new ArgumentMatcher<UserForm>() {
			@Override
			public boolean matches(Object argument) {
				UserForm form = (UserForm) argument;
				return StringUtils.equals(form.getLoginName(), loginName) &&
						StringUtils.equals(form.getPassword1(), password1) &&
						StringUtils.equals(form.getPassword2(), password2) &&
						(form.isDisabled() == disabled) &&
						(form.isAdmin() == admin);
			}
		};
		return argThat(matcher);
	}

	public static BranchForm argBranchForm(final String projectName, final String name, final String startingBranch) {
		Matcher<BranchForm> matcher = new ArgumentMatcher<BranchForm>() {
			@Override
			public boolean matches(Object argument) {
				BranchForm form = (BranchForm) argument;
				return StringUtils.equals(form.getProjectName(), projectName) &&
						StringUtils.equals(form.getName(), name) &&
						StringUtils.equals(form.getStartingBranch(), startingBranch);
			}
		};
		return argThat(matcher);
	}

	public static ProjectForm argProjectForm(final String name) {
		Matcher<ProjectForm> matcher = new ArgumentMatcher<ProjectForm>() {
			@Override
			public boolean matches(Object argument) {
				ProjectForm form = (ProjectForm) argument;
				return StringUtils.equals(form.getName(), name);
			}
		};
		return argThat(matcher);
	}
}