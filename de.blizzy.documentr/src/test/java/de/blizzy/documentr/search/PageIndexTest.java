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

import org.apache.lucene.queryParser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.Page;
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
	
	@Before
	public void setUp() throws IOException {
		File dataDir = createTempDir();
		when(settings.getDocumentrDataDir()).thenReturn(dataDir);
		
		when(anonymousAuthenticationFactory.create("dummy")).thenReturn(authentication); //$NON-NLS-1$
		
		pageIndex.init();
	}
	
	@After
	public void tearDown() {
		pageIndex.destroy();
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void addAndFindPage() throws ParseException, IOException {
		when(markdownProcessor.markdownToHTML("markdown", PROJECT, BRANCH, PAGE_PATH, authentication, false)) //$NON-NLS-1$
			.thenReturn("html"); //$NON-NLS-1$
		
		Page page = Page.fromText("title", "markdown"); //$NON-NLS-1$ //$NON-NLS-2$
		pageIndex.addPage(PROJECT, BRANCH, PAGE_PATH, page);

		when(permissionEvaluator.hasPagePermission(authentication, PROJECT, BRANCH, PAGE_PATH, Permission.VIEW))
			.thenReturn(true);
		
		// wait for page to get indexed
		while (pageIndex.getNumDocuments() == 0) {
			sleep(10);
		}
		
		SearchResult result = pageIndex.findPages("html", 1, authentication); //$NON-NLS-1$
		assertEquals(1, result.getTotalHits());
		assertEquals("<strong>html</strong>", result.getHits().get(0).getTextHtml()); //$NON-NLS-1$
	}

	@Test
	@SuppressWarnings("boxing")
	public void deletePages() throws IOException {
		when(markdownProcessor.markdownToHTML("markdown", PROJECT, BRANCH, PAGE_PATH, authentication, false)) //$NON-NLS-1$
			.thenReturn("html"); //$NON-NLS-1$
		
		Page page = Page.fromText("title", "markdown"); //$NON-NLS-1$ //$NON-NLS-2$
		pageIndex.addPage(PROJECT, BRANCH, PAGE_PATH, page);
		
		when(permissionEvaluator.hasPagePermission(authentication, PROJECT, BRANCH, PAGE_PATH, Permission.VIEW))
			.thenReturn(true);
		
		// wait for page to get indexed
		while (pageIndex.getNumDocuments() == 0) {
			sleep(10);
		}

		pageIndex.deletePages(PROJECT, BRANCH, Collections.singleton(PAGE_PATH));

		assertEquals(0, pageIndex.getNumDocuments());
	}

	private void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// ignore
		}
	}
}