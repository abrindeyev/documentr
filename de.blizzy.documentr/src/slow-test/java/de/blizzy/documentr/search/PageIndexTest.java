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
package de.blizzy.documentr.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageChangedEvent;
import de.blizzy.documentr.page.PagesDeletedEvent;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;

public class PageIndexTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH = "foo/bar"; //$NON-NLS-1$
	
	@InjectMocks
	private PageIndex pageIndex;
	@Mock
	private Settings settings;
	@Mock
	private AnonymousAuthenticationToken authentication;
	@Mock
	private DocumentrAnonymousAuthenticationFactory anonymousAuthenticationFactory;
	@Mock
	private MarkdownProcessor markdownProcessor;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private IPageStore pageStore;
	@Mock
	@SuppressWarnings("unused")
	private EventBus eventBus;
	@Mock
	private GlobalRepositoryManager repoManager;
	@Mock
	private UserStore userStore;
	
	@Before
	public void setUp() throws IOException {
		File dataDir = createTempDir();
		when(settings.getDocumentrDataDir()).thenReturn(dataDir);
		
		when(anonymousAuthenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME)).thenReturn(authentication);

		when(repoManager.listProjects()).thenReturn(Collections.<String>emptyList());

		pageIndex.setTaskExecutor(MoreExecutors.sameThreadExecutor());
		
		pageIndex.init();
	}
	
	@After
	public void tearDown() {
		pageIndex.destroy();
	}
	
	@Test
	public void addAndFindPage() throws ParseException, IOException, TimeoutException {
		when(markdownProcessor.markdownToHTML("markdown", PROJECT, BRANCH, PAGE_PATH, authentication, false)) //$NON-NLS-1$
			.thenReturn("html"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true))
			.thenReturn(Page.fromText("title", "markdown")); //$NON-NLS-1$ //$NON-NLS-2$
		
		pageIndex.addPage(new PageChangedEvent(PROJECT, BRANCH, PAGE_PATH));
		pageIndex.commit();
		pageIndex.refresh();

		when(permissionEvaluator.getBranchesForPermission(authentication, Permission.VIEW))
			.thenReturn(Sets.newHashSet(PROJECT + "/" + BRANCH)); //$NON-NLS-1$
		
		when(userStore.listRoles()).thenReturn(Lists.newArrayList("reader")); //$NON-NLS-1$
		
		SearchResult result = pageIndex.findPages("html", 1, authentication); //$NON-NLS-1$
		assertEquals(1, result.getTotalHits());
		assertEquals("<strong>html</strong>", result.getHits().get(0).getTextHtml()); //$NON-NLS-1$
	}

	@Test
	public void findPagesAndSuggestion() throws ParseException, IOException, TimeoutException {
		when(markdownProcessor.markdownToHTML("markdown", PROJECT, BRANCH, PAGE_PATH, authentication, false)) //$NON-NLS-1$
			.thenReturn("html"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true))
			.thenReturn(Page.fromText("title", "markdown")); //$NON-NLS-1$ //$NON-NLS-2$
		
		pageIndex.addPage(new PageChangedEvent(PROJECT, BRANCH, PAGE_PATH));
		pageIndex.commit();
		pageIndex.refresh();
		
		when(permissionEvaluator.getBranchesForPermission(authentication, Permission.VIEW))
			.thenReturn(Sets.newHashSet(PROJECT + "/" + BRANCH)); //$NON-NLS-1$
	
		when(userStore.listRoles()).thenReturn(Lists.newArrayList("reader")); //$NON-NLS-1$
		
		SearchResult result = pageIndex.findPages("htlm", 1, authentication); //$NON-NLS-1$
		SearchTextSuggestion suggestion = result.getSuggestion();
		assertNotNull(suggestion);
		assertEquals(1, suggestion.getTotalHits());
		assertEquals("html", suggestion.getSearchText()); //$NON-NLS-1$
		assertEquals("<strong><em>html</em></strong>", suggestion.getSearchTextHtml()); //$NON-NLS-1$
	}
	
	@Test
	public void deletePages() throws IOException {
		when(markdownProcessor.markdownToHTML("markdown", PROJECT, BRANCH, PAGE_PATH, authentication, false)) //$NON-NLS-1$
			.thenReturn("html"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true))
			.thenReturn(Page.fromText("title", "markdown")); //$NON-NLS-1$ //$NON-NLS-2$
		
		pageIndex.addPage(new PageChangedEvent(PROJECT, BRANCH, PAGE_PATH));
		pageIndex.commit();
		pageIndex.refresh();
		
		pageIndex.deletePages(new PagesDeletedEvent(PROJECT, BRANCH, Collections.singleton(PAGE_PATH)));
		pageIndex.commit();
		pageIndex.refresh();
		assertEquals(0, pageIndex.getNumDocuments());
	}
	
	@Test
	@Ignore
	public void getAllTags() {
		// TODO: implement test
	}
}