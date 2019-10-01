package org.ace.accounting.user.persistence.interfaces;

import java.util.List;

import org.ace.accounting.user.User;
import org.ace.java.component.persistence.exception.DAOException;

public interface IUserDAO {
	public List<User> findAll() throws DAOException;

	public User find(String username) throws DAOException;

}
