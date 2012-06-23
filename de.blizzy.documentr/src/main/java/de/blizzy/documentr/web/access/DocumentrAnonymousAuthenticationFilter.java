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
package de.blizzy.documentr.web.access;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.UserStore;

@Component("anonymousAuthFilter")
public class DocumentrAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;
	
	public DocumentrAnonymousAuthenticationFilter() {
		super(DocumentrConstants.ANONYMOUS_AUTH_KEY);
	}
	
	@Override
	protected Authentication createAuthentication(HttpServletRequest request) {
		try {
			Authentication auth = super.createAuthentication(request);
			AbstractAuthenticationToken authentication =
					authenticationFactory.create(DocumentrConstants.ANONYMOUS_AUTH_KEY, UserStore.ANONYMOUS_USER_LOGIN_NAME);
			authentication.setDetails(auth.getDetails());
			return authentication;
		} catch (IOException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	void setAnonymousAuthenticationFactory(DocumentrAnonymousAuthenticationFactory authenticationFactory) {
		this.authenticationFactory = authenticationFactory;
	}
}
