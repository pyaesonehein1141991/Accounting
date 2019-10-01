package org.ace.accounting.system.tlf.persistence;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.ace.accounting.common.VoucherType;
import org.ace.accounting.dto.EditVoucherDto;
import org.ace.accounting.system.tlf.TLF;
import org.ace.accounting.system.tlf.persistence.interfaces.ITLFDAO;
import org.ace.accounting.system.trantype.TranType;
import org.ace.java.component.persistence.BasicDAO;
import org.ace.java.component.persistence.exception.DAOException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("TLFDAO")
public class TLFDAO extends BasicDAO implements ITLFDAO {

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<TLF> findAll() throws DAOException {
		List<TLF> result = null;
		try {
			Query q = em.createNamedQuery("TLF.findAll");
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of TLF", pe);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<String> findVoucherNoByBranchIdAndVoucherType(String branchId, VoucherType voucherType) throws DAOException {
		List<String> result = null;
		try {
			Query q = em.createNamedQuery("TLF.findVoucherNoByBranchId");
			q.setParameter("branchId", branchId);
			if (voucherType.equals(VoucherType.CASH)) {
				q.setParameter("status", "C%");
			} else {
				q.setParameter("status", "T%");
			}
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find all of Voucher No by " + branchId, pe);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<TLF> findVoucherListByReverseZero(String voucherNo) throws DAOException {
		List<TLF> result = null;
		try {
			Query q = em.createNamedQuery("TLF.findVoucherListByVoucherNo");
			q.setParameter("voucherNo", voucherNo);
			result = q.getResultList();
			em.flush();
		} catch (PersistenceException pe) {
			throw translate("Failed to find VoucherListByReverseZero", pe);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public TranType findCashAccountByVoucherNo(String voucherNo) throws DAOException {
		TranType result = null;
		EditVoucherDto dto = null;
		try {
			Query q = em.createNamedQuery("TLF.findCashAccountByVoucherNo");
			q.setParameter("voucherNo", voucherNo);
			q.setMaxResults(1);
			dto = (EditVoucherDto) q.getSingleResult();
			em.flush();
			if (dto != null) {
				result = dto.getTranType();
			}
		} catch (NoResultException e) {
			return null;
		} catch (PersistenceException pe) {
			throw translate("Failed to find CashAccountByVoucherNo", pe);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateReverseByID(String id, boolean reverse) throws DAOException {
		try {
			Query q = em.createNamedQuery("TLF.updateReverseByID");
			q.setParameter("id", id);
			q.setParameter("reverse", reverse);
			q.executeUpdate();
		} catch (PersistenceException pe) {
			throw translate("Failed to update ReverseByID " + id, pe);
		}
	}

}
