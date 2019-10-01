package org.ace.accounting.system.rateinfo.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.rateinfo.RateInfo;
import org.ace.accounting.system.rateinfo.RateType;
import org.ace.accounting.system.rateinfo.persistence.interfaces.IRateInfoDAO;
import org.ace.java.component.persistence.BasicDAO;
import org.ace.java.component.persistence.exception.DAOException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("RateInfoDAO")
public class RateInfoDAO extends BasicDAO implements IRateInfoDAO {

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<RateInfo> findAll() throws DAOException {
		List<RateInfo> result = null;
		try {
			Query q = em.createNamedQuery("RateInfo.findAll");
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of RateInfo", pe);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public RateInfo findRateInfoBy(Currency reqCur, RateType reqRateType, Date rDate) throws DAOException {
		RateInfo result = null;
		try {
			Query q = em.createNamedQuery("RateInfo.findRateInfoByCurRTypeRDate");
			q.setParameter("currency", reqCur);
			q.setParameter("rateType", reqRateType);
			q.setParameter("rDate", rDate);
			result = (RateInfo) q.getSingleResult();
			em.flush();
		} catch (NoResultException re) {
			return null;
		} catch (PersistenceException pe) {
			throw translate("Failed to findRateInfoByCurRTypeRDate", pe);
		}

		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateLastModifyBy(Currency cur, RateType rateType) throws DAOException {
		try {
			Query q = em.createNamedQuery("RateInfo.updateLastModifyBy");
			q.setParameter("currency", cur);
			q.setParameter("rateType", rateType);
			q.executeUpdate();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Faile to update LastModify 0 By Daily Currency Rate", pe);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<RateInfo> findRateInfoByCurrencyID(String currencyID) throws DAOException {
		List<RateInfo> list = null;
		try {
			Query q = em.createNamedQuery("RateInfo.findRateInfoByCurrencyID");
			q.setParameter("currency", currencyID);
			list = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of RateInfo", pe);
		}
		return list;
	}
}
