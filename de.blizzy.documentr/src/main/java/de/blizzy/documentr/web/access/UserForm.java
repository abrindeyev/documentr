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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class UserForm {
	@NotNull(message="{user.loginName.blank}")
	@NotBlank(message="{user.loginName.blank}")
	@ValidLoginName
	private String loginName;
	private String password1;
	private String password2;
	@NotNull(message="{user.email.blank}")
	@NotBlank(message="{user.email.blank}")
	private String email;
	private boolean disabled;
	private String authorities;

	public UserForm(String loginName, String password1, String password2, String email,
			boolean disabled, String authorities) {
		
		this.loginName = loginName;
		this.password1 = password1;
		this.password2 = password2;
		this.email = email;
		this.disabled = disabled;
		this.authorities = authorities;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public String getPassword1() {
		return password1;
	}
	
	public String getPassword2() {
		return password2;
	}
	
	public String getEmail() {
		return email;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public String getAuthorities() {
		return authorities;
	}
}
