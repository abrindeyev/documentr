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
package de.blizzy.documentr.web.branch;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

@Controller
@RequestMapping("/branch")
public class BranchController {
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private PageStore pageStore;

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String createBranch(@PathVariable String projectName, Model model) {
		BranchForm form = new BranchForm(projectName, StringUtils.EMPTY, null);
		model.addAttribute("branchForm", form); //$NON-NLS-1$
		return "/project/branch/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String saveBranch(@ModelAttribute @Valid BranchForm form, BindingResult bindingResult)
			throws IOException, GitAPIException {
		
		List<String> branches = repoManager.listProjectBranches(form.getProjectName());
		boolean firstBranch = branches.isEmpty();
		if (branches.contains(form.getName())) {
			bindingResult.rejectValue("name", "branch.name.exists"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (bindingResult.hasErrors()) {
			return "/project/branch/edit"; //$NON-NLS-1$
		}
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.createProjectBranchRepository(form.getProjectName(), form.getName(), form.getStartingBranch());
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		
		if (firstBranch) {
			Page page = Page.fromText(null, "Home", StringUtils.EMPTY); //$NON-NLS-1$
			pageStore.savePage(form.getProjectName(), form.getName(), "home", page); //$NON-NLS-1$
			return "redirect:/page/edit/" + form.getProjectName() + "/" + form.getName() + "/home"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return "redirect:/page/" + form.getProjectName() + "/" + form.getName() + "/home"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@ModelAttribute
	public BranchForm createBranchForm(@PathVariable String projectName, @RequestParam(required=false) String name,
			@RequestParam(required=false) String startingBranch) {
		
		return (name != null) ? new BranchForm(projectName, name, startingBranch) : null;
	}
}