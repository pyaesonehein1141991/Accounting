package org.ace.accounting.system.tlf.persistence.interfaces;

import java.util.List;

import org.ace.accounting.common.VoucherType;
import org.ace.accounting.system.tlf.TLF;
import org.ace.accounting.system.trantype.TranType;
import org.ace.java.component.persistence.exception.DAOException;

public interface ITLFDAO {
	public List<TLF> findAll() throws DAOException;

	public List<String> findVoucherNoByBranchIdAndVoucherType(String branchId, VoucherType voucherType) throws DAOException;

	public List<TLF> findVoucherListByReverseZero(String voucherNo) throws DAOException;

	public TranType findCashAccountByVoucherNo(String voucherNo) throws DAOException;

	public void updateReverseByID(String id, boolean reverse) throws DAOException;

}
