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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

@Component
public class UserStore {
	private static final String REPOSITORY_NAME = "_users"; //$NON-NLS-1$
	private static final String USER_SUFFIX = ".user"; //$NON-NLS-1$
	private static final String ROLE_SUFFIX = ".role"; //$NON-NLS-1$
	private static final String AUTHORITIES_SUFFIX = ".authorities"; //$NON-NLS-1$
	
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostConstruct
	public void init() throws IOException, GitAPIException {
		String passwordHash = passwordEncoder.encodePassword("admin", "admin"); //$NON-NLS-1$ //$NON-NLS-2$
		User adminUser = new User("admin", passwordHash, "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$

		ILockedRepository repo = null;
		boolean created = false;
		try {
			repo = repoManager.createProjectCentralRepository(REPOSITORY_NAME, false, adminUser);
			created = true;
		} catch (IllegalStateException e) {
			// okay
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		
		if (created) {
			createInitialAdmin(adminUser);
			createInitialRoles(adminUser);
		}
	}

	private void createInitialAdmin(User adminUser) throws IOException {
		saveUser(adminUser, adminUser);
	}
	
	private void createInitialRoles(User adminUser) throws IOException {
		saveRole(new Role("Administrator", EnumSet.of(Permission.ADMIN)), adminUser); //$NON-NLS-1$
		saveRole(new Role("Editor", EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE)), adminUser); //$NON-NLS-1$
		saveRole(new Role("Reader", EnumSet.of(Permission.VIEW)), adminUser); //$NON-NLS-1$

		Set<RoleGrantedAuthority> authorities = Collections.singleton(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "Administrator")); //$NON-NLS-1$
		saveUserAuthorities(adminUser.getLoginName(), authorities, adminUser);
	}

	public void saveUser(User user, User currentUser) throws IOException {
		Assert.notNull(user);
		Assert.notNull(currentUser);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("loginName", user.getLoginName()); //$NON-NLS-1$
			userMap.put("password", user.getPassword()); //$NON-NLS-1$
			userMap.put("email", user.getEmail()); //$NON-NLS-1$
			userMap.put("disabled", Boolean.valueOf(user.isDisabled())); //$NON-NLS-1$

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(userMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, user.getLoginName() + USER_SUFFIX);
			FileUtils.write(workingFile, json, DocumentrConstants.ENCODING);

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(user.getLoginName() + USER_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(user.getLoginName())
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	public User getUser(String loginName) throws IOException {
		Assert.notNull(loginName);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), loginName + USER_SUFFIX);
			if (json == null) {
				throw new UserNotFoundException(loginName);
			}
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Object> userMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
			String password = (String) userMap.get("password"); //$NON-NLS-1$
			String email = (String) userMap.get("email"); //$NON-NLS-1$
			boolean disabled = ((Boolean) userMap.get("disabled")).booleanValue(); //$NON-NLS-1$
			User user = new User(loginName, password, email, disabled);
			return user;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public List<String> listUsers() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(USER_SUFFIX);
				}
			};
			List<File> files = Arrays.asList(workingDir.listFiles(filter));
			Function<File, String> function = new Function<File, String>() {
				@Override
				public String apply(File file) {
					return StringUtils.substringBeforeLast(file.getName(), USER_SUFFIX);
				}
			};
			List<String> users = new ArrayList<String>(Lists.transform(files, function));
			Collections.sort(users);
			return users;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public void saveRole(Role role, User currentUser) throws IOException {
		Assert.notNull(role);
		Assert.notNull(currentUser);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			
			Map<String, Object> roleMap = new HashMap<String, Object>();
			roleMap.put("name", role.getName()); //$NON-NLS-1$
			Set<String> permissions = new HashSet<String>();
			for (Permission permission : role.getPermissions()) {
				permissions.add(permission.name());
			}
			roleMap.put("permissions", permissions); //$NON-NLS-1$
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(roleMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, role.getName() + ROLE_SUFFIX);
			FileUtils.write(workingFile, json, DocumentrConstants.ENCODING);

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(role.getName() + ROLE_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(role.getName())
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public List<String> listRoles() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(ROLE_SUFFIX);
				}
			};
			List<File> files = Arrays.asList(workingDir.listFiles(filter));
			Function<File, String> function = new Function<File, String>() {
				@Override
				public String apply(File file) {
					return StringUtils.substringBeforeLast(file.getName(), ROLE_SUFFIX);
				}
			};
			List<String> users = new ArrayList<String>(Lists.transform(files, function));
			Collections.sort(users);
			return users;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public Role getRole(String roleName) throws IOException {
		Assert.notNull(roleName);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), roleName + ROLE_SUFFIX);
			if (json == null) {
				throw new RoleNotFoundException(roleName);
			}
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Object> roleMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
			@SuppressWarnings("unchecked")
			Collection<String> permissions = (Collection<String>) roleMap.get("permissions"); //$NON-NLS-1$
			EnumSet<Permission> rolePermissions = EnumSet.noneOf(Permission.class);
			for (String permission : permissions) {
				rolePermissions.add(Permission.valueOf(permission));
			}
			Role role = new Role(roleName, rolePermissions);
			return role;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	public void saveUserAuthorities(String loginName, Set<RoleGrantedAuthority> authorities, User currentUser)
			throws IOException {
		
		Assert.notNull(loginName);
		Assert.notNull(authorities);
		Assert.notNull(currentUser);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);

			Map<String, Set<String>> authoritiesMap = new HashMap<String, Set<String>>();
			for (RoleGrantedAuthority rga : authorities) {
				GrantedAuthorityTarget target = rga.getTarget();
				String targetStr = target.getType().name() + ":" + target.getTargetId(); //$NON-NLS-1$
				Set<String> roleNames = authoritiesMap.get(targetStr);
				if (roleNames == null) {
					roleNames = new HashSet<String>();
					authoritiesMap.put(targetStr, roleNames);
				}
				roleNames.add(rga.getRoleName());
			}
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(authoritiesMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, loginName + AUTHORITIES_SUFFIX);
			FileUtils.write(workingFile, json, DocumentrConstants.ENCODING);

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(loginName + AUTHORITIES_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(loginName)
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public List<RoleGrantedAuthority> getUserAuthorities(String loginName) throws IOException {
		Assert.notNull(loginName);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), loginName + AUTHORITIES_SUFFIX);
			if (json == null) {
				throw new UserNotFoundException(loginName);
			}
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Set<String>> authoritiesMap = gson.fromJson(
					json, new TypeToken<Map<String, Set<String>>>(){}.getType());
			List<RoleGrantedAuthority> authorities = new ArrayList<RoleGrantedAuthority>();
			for (Map.Entry<String, Set<String>> entry : authoritiesMap.entrySet()) {
				String targetStr = entry.getKey();
				Type type = Type.valueOf(StringUtils.substringBefore(targetStr, ":")); //$NON-NLS-1$
				String targetId = StringUtils.substringAfter(targetStr, ":"); //$NON-NLS-1$
				for (String roleName : entry.getValue()) {
					authorities.add(new RoleGrantedAuthority(new GrantedAuthorityTarget(targetId, type), roleName));
				}
			}
			
			Collections.sort(authorities, new Comparator<RoleGrantedAuthority>() {
				@Override
				public int compare(RoleGrantedAuthority rga1, RoleGrantedAuthority rga2) {
					GrantedAuthorityTarget target1 = rga1.getTarget();
					GrantedAuthorityTarget target2 = rga2.getTarget();
					Type type1 = target1.getType();
					Type type2 = target2.getType();
					int result = Integer.valueOf(type1.ordinal()).compareTo(Integer.valueOf(type2.ordinal()));
					if (result != 0) {
						return result;
					}
					
					String targetId1 = target1.getTargetId();
					String targetId2 = target2.getTargetId();
					result = targetId1.compareToIgnoreCase(targetId2);
					if (result != 0) {
						return result;
					}
					
					return rga1.getRoleName().compareToIgnoreCase(rga2.getRoleName());
				}
			});
			
			return authorities;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	void setGlobalRepositoryManager(GlobalRepositoryManager repoManager) {
		this.repoManager = repoManager;
	}

	void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
}
