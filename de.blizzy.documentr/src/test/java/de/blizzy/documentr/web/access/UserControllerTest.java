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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;

import de.blizzy.documentr.TestUtil;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;

public class UserControllerTest {
	private UserStore userStore;
	private ShaPasswordEncoder passwordEncoder;
	private UserController userController;

	@Before
	public void setUp() {
		userStore = mock(UserStore.class);

		passwordEncoder = new ShaPasswordEncoder();
		
		userController = new UserController();
		userController.setUserStore(userStore);
		userController.setPasswordEncoder(passwordEncoder);
	}
	
	@Test
	public void saveUser() throws IOException {
		UserForm user = new UserForm("user", "pw", "pw", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		when(userStore.getUser("user")).thenThrow(new UserNotFoundException("user")); //$NON-NLS-1$ //$NON-NLS-2$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/users", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertFalse(bindingResult.hasErrors());

		String passwordHash = passwordEncoder.encodePassword("pw", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, true, false)); //$NON-NLS-1$
	}
	
	@Test
	public void saveExistingUser() throws IOException {
		UserForm user = new UserForm("user", "newPW", "newPW", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		User oldUser = new User("user", "oldPW", false, false); //$NON-NLS-1$ //$NON-NLS-2$
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/users", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertFalse(bindingResult.hasErrors());
		
		String passwordHash = passwordEncoder.encodePassword("newPW", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, true, false)); //$NON-NLS-1$
	}
	
	@Test
	public void saveExistingUserButKeepPassword() throws IOException {
		UserForm user = new UserForm("user", StringUtils.EMPTY, StringUtils.EMPTY, true, false); //$NON-NLS-1$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		User oldUser = new User("user", "oldPW", false, false); //$NON-NLS-1$ //$NON-NLS-2$
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/users", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertFalse(bindingResult.hasErrors());
		
		verify(userStore).saveUser(argUser("user", "oldPW", true, false)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void saveUserPassword1Blank() throws IOException {
		UserForm user = new UserForm("user", StringUtils.EMPTY, "pw", true, false); //$NON-NLS-1$ //$NON-NLS-2$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/user/edit", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
	}

	@Test
	public void saveUserPassword2Blank() throws IOException {
		UserForm user = new UserForm("user", "pw", StringUtils.EMPTY, true, false); //$NON-NLS-1$ //$NON-NLS-2$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/user/edit", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}
	
	@Test
	public void saveUserPasswordsDiffer() throws IOException {
		UserForm user = new UserForm("user", "pw", "pw2", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult);
		assertEquals("/user/edit", TestUtil.removeViewPrefix(view)); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}
	
	@Test
	public void editUser() throws IOException {
		User user = new User("user", "pw", false, false); //$NON-NLS-1$ //$NON-NLS-2$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		Model model = mock(Model.class);
		String view = userController.editUser("user", model); //$NON-NLS-1$
		assertEquals("/user/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("userForm"), //$NON-NLS-1$
				argUserForm("user", StringUtils.EMPTY, StringUtils.EMPTY, false, false)); //$NON-NLS-1$
	}
	
	private User argUser(final String loginName, final String password, final boolean disabled, final boolean admin) {
		ArgumentMatcher<User> matcher = new ArgumentMatcher<User>() {
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
	
	private UserForm argUserForm(final String loginName, final String password1, final String password2,
			final boolean disabled, final boolean admin) {
		
		ArgumentMatcher<UserForm> matcher = new ArgumentMatcher<UserForm>() {
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
}