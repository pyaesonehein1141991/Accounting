/***************************************************************************************
 * @author <<Your Name>>
 * @Date 2013-02-11
 * @Version 1.0
 * @Purpose <<You have to write the comment the main purpose of this class>>
 * 
 *    
 ***************************************************************************************/
package org.ace.accounting.system.currency.persistence;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.ace.accounting.dto.MonthlyRateDto;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.currency.persistence.interfaces.ICurrencyDAO;
import org.ace.java.component.persistence.BasicDAO;
import org.ace.java.component.persistence.exception.DAOException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("CurrencyDAO")
public class CurrencyDAO extends BasicDAO implements ICurrencyDAO {

	/*
	 * @Resource(name = "DataRepService") private IDataRepService<Currency>
	 * currencyDataRepService;
	 * 
	 * @Resource(name = "DataRepService") private
	 * IDataRepService<CurrencyChartOfAccount> ccoaDataRepService;
	 * 
	 * @Resource(name = "DataRepService") private IDataRepService<RateInfo>
	 * rateInfoDataRepService;
	 * 
	 * @Resource(name = "CcoaService") private ICcoaService ccoaService;
	 * 
	 * @Resource(name = "RateInfoService") private IRateInfoService
	 * rateInfoService;
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	public Currency findCurrencyByCurrencyCode(String currencyCode) throws DAOException {
		Currency result = null;
		try {
			Query query = em.createNamedQuery("Currency.findByCurrencyCode");
			query.setParameter("currencyCode", currencyCode);
			result = (Currency) query.getSingleResult();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find Currency", pe);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<Currency> findAll() throws DAOException {
		List<Currency> result = null;
		try {
			Query q = em.createNamedQuery("Currency.findAll");
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of Currency", pe);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Currency> findForeignCurrency() throws DAOException {
		List<Currency> result = null;
		try {
			Query q = em.createNamedQuery("Currency.findForeignCurrency");
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of Currency", pe);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<MonthlyRateDto> findForeignCurrencyDto() throws DAOException {
		List<MonthlyRateDto> result = null;
		StringBuffer sf = new StringBuffer(
				"SELECT NEW org.ace.accounting.dto.MonthlyRateDto(c.id,c.currencyCode,c.description,c.m1,c.m2,c.m3,c.m4,c.m5,c.m6,c.m7,c.m8,c.m9,c.m10,c.m11,c.m12) FROM Currency c WHERE c.isHomeCur=false ORDER BY c.currencyCode ASC");
		try {
			Query q = em.createQuery(sf.toString());
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of Currency", pe);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateMonthlyRate(MonthlyRateDto cur) throws DAOException {
		StringBuffer sf = new StringBuffer("UPDATE Currency c SET c.m1=:m1, c.m2=:m2, c.m3=:m3, c.m4=:m4, c.m5=:m5, c.m6=:m6, c.m7=:m7, c.m8=:m8, c.m9=:m9,"
				+ " c.m10=:m10, c.m11=:m11, c.m12=:m12 WHERE c.id=:id");
		try {
			Query q = em.createQuery(sf.toString());
			q.setParameter("m1", cur.getM1());
			q.setParameter("m2", cur.getM2());
			q.setParameter("m3", cur.getM3());
			q.setParameter("m4", cur.getM4());
			q.setParameter("m5", cur.getM5());
			q.setParameter("m6", cur.getM6());
			q.setParameter("m7", cur.getM7());
			q.setParameter("m8", cur.getM8());
			q.setParameter("m9", cur.getM9());
			q.setParameter("m10", cur.getM10());
			q.setParameter("m11", cur.getM11());
			q.setParameter("m12", cur.getM12());
			q.setParameter("id", cur.getId());
			q.executeUpdate();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of Currency", pe);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Currency findHomeCurrency() throws DAOException {
		Currency currency = null;
		try {
			Query q = em.createNamedQuery("Currency.findHomeCurrency");
			currency = (Currency) q.getSingleResult();
		} catch (NoResultException re) {
			return null;
		} catch (PersistenceException pe) {
			throw translate("Failed to find Home Currency", pe);
		}
		return currency;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Currency currency) throws DAOException {
		Query query = null;
		StringBuffer buffer = null;
		try {
			buffer = new StringBuffer();
			buffer.append("DELETE FROM CurrencyChartOfAccount c WHERE c.currency.id = :currencyId");
			query = em.createQuery(buffer.toString());
			query.setParameter("currencyId", currency.getId());
			query.executeUpdate();
			em.flush();

			buffer = new StringBuffer();
			buffer.append("DELETE FROM RateInfo r WHERE r.currency.id = :currencyId");
			query = em.createQuery(buffer.toString());
			query.setParameter("currencyId", currency.getId());
			query.executeUpdate();
			em.flush();

			currency = em.merge(currency);
			em.remove(currency);
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to delete currency", pe);
		}
	}
}
