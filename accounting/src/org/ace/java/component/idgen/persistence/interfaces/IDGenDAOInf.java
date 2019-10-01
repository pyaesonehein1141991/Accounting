package org.ace.java.component.idgen.persistence.interfaces;

import org.ace.accounting.system.branch.Branch;
import org.ace.java.component.idgen.IDGen;
import org.ace.java.component.persistence.exception.DAOException;

public interface IDGenDAOInf {
	public IDGen getNextId(String genName) throws DAOException;
	public IDGen getNextId(String generateItem, Branch branch) throws DAOException;
}
