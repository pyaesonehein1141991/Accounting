package org.ace.accounting.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.ace.accounting.system.branch.Branch;
import org.ace.accounting.system.chartaccount.ChartOfAccount;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.trantype.TranCode;
import org.ace.accounting.system.trantype.TranType;

public class AccountLedgerDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ChartOfAccount coa;
	private BigDecimal debit;
	private BigDecimal credit;
	private BigDecimal homeDebit;
	private BigDecimal homeCredit;
	private Currency currency;
	private Branch branch;
	private String narration;
	private BigDecimal dblBalance;
	private Date settlementDate;
	private int srNo;
	private TranType tranType;

	public AccountLedgerDto() {
		super();
	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, String narration, BigDecimal dblBalance, Date settlementDate, int srNo) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.narration = narration;
		this.dblBalance = dblBalance;
		this.settlementDate = settlementDate;
		this.srNo = srNo;
	}

	// customized to get debit, credit, homeDebit, homeCredit according to
	// TransType's TranCode
	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, BigDecimal homeDebit, BigDecimal homeCredit, String narration, Currency currency,
			Branch branch, TranType tranType) {
		super();
		this.coa = coa;
		this.currency = currency;
		this.branch = branch;
		this.narration = narration;
		this.tranType = tranType;
		this.debit = debit;
		this.credit = credit;
		this.homeDebit = homeDebit;
		this.homeCredit = homeCredit;
		if (!(tranType.getTranCode().equals(TranCode.CSCREDIT) || tranType.getTranCode().equals(TranCode.TRCREDIT))) {
			this.credit = BigDecimal.ZERO;
			this.homeCredit = BigDecimal.ZERO;
		}
		if (!(tranType.getTranCode().equals(TranCode.CSDEBIT) || tranType.getTranCode().equals(TranCode.TRDEBIT))) {
			this.debit = BigDecimal.ZERO;
			this.homeDebit = BigDecimal.ZERO;

		}
	}

	// customized to get debit, credit, homeDebit, homeCredit according to
	// TransType's TranCode
	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, BigDecimal homeDebit, BigDecimal homeCredit, String narration, Date settlementDate,
			Currency currency, Branch branch, TranType tranType) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.homeDebit = homeDebit;
		this.homeCredit = homeCredit;
		this.currency = currency;
		this.branch = branch;
		this.narration = narration;
		this.settlementDate = settlementDate;
		this.tranType = tranType;
		if (!(tranType.getTranCode().equals(TranCode.CSCREDIT) || tranType.getTranCode().equals(TranCode.TRCREDIT))) {
			this.credit = BigDecimal.ZERO;
			this.homeCredit = BigDecimal.ZERO;
		}
		if (!(tranType.getTranCode().equals(TranCode.CSDEBIT) || tranType.getTranCode().equals(TranCode.TRDEBIT))) {
			this.debit = BigDecimal.ZERO;
			this.homeDebit = BigDecimal.ZERO;
		}
	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, BigDecimal homeDebit, BigDecimal homeCredit, String narration, Date settlementDate) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.homeDebit = homeDebit;
		this.homeCredit = homeCredit;
		this.narration = narration;
		this.settlementDate = settlementDate;
	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, String narration, Date settlementDate) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.narration = narration;
		this.settlementDate = settlementDate;
	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, BigDecimal homeDebit, BigDecimal homeCredit) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.homeDebit = homeDebit;
		this.homeCredit = homeCredit;
	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;

	}

	public AccountLedgerDto(ChartOfAccount coa, BigDecimal debit, BigDecimal credit, BigDecimal homeDebit, BigDecimal homeCredit, String narration) {
		super();
		this.coa = coa;
		this.debit = debit;
		this.credit = credit;
		this.homeDebit = homeDebit;
		this.homeCredit = homeCredit;
		this.narration = narration;
	}

	public ChartOfAccount getCoa() {
		return coa;
	}

	public void setCoa(ChartOfAccount coa) {
		this.coa = coa;
	}

	public BigDecimal getDebit() {
		return debit;
	}

	public void setDebit(BigDecimal debit) {
		this.debit = debit;
	}

	public BigDecimal getCredit() {
		return credit;
	}

	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}

	public BigDecimal getHomeDebit() {
		return homeDebit;
	}

	public void setHomeDebit(BigDecimal homeDebit) {
		this.homeDebit = homeDebit;
	}

	public BigDecimal getHomeCredit() {
		return homeCredit;
	}

	public void setHomeCredit(BigDecimal homeCredit) {
		this.homeCredit = homeCredit;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public BigDecimal getDblBalance() {
		return dblBalance;
	}

	public void setDblBalance(BigDecimal dblBalance) {
		this.dblBalance = dblBalance;
	}

	public Date getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(Date settlementDate) {
		this.settlementDate = settlementDate;
	}

	public int getSrNo() {
		return srNo;
	}

	public void setSrNo(int srNo) {
		this.srNo = srNo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
		result = prime * result + ((coa == null) ? 0 : coa.hashCode());
		result = prime * result + ((credit == null) ? 0 : credit.hashCode());
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + ((dblBalance == null) ? 0 : dblBalance.hashCode());
		result = prime * result + ((debit == null) ? 0 : debit.hashCode());
		result = prime * result + ((homeCredit == null) ? 0 : homeCredit.hashCode());
		result = prime * result + ((homeDebit == null) ? 0 : homeDebit.hashCode());
		result = prime * result + ((narration == null) ? 0 : narration.hashCode());
		result = prime * result + ((settlementDate == null) ? 0 : settlementDate.hashCode());
		result = prime * result + srNo;
		result = prime * result + ((tranType == null) ? 0 : tranType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountLedgerDto other = (AccountLedgerDto) obj;
		if (branch == null) {
			if (other.branch != null)
				return false;
		} else if (!branch.equals(other.branch))
			return false;
		if (coa == null) {
			if (other.coa != null)
				return false;
		} else if (!coa.equals(other.coa))
			return false;
		if (credit == null) {
			if (other.credit != null)
				return false;
		} else if (!credit.equals(other.credit))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		if (dblBalance == null) {
			if (other.dblBalance != null)
				return false;
		} else if (!dblBalance.equals(other.dblBalance))
			return false;
		if (debit == null) {
			if (other.debit != null)
				return false;
		} else if (!debit.equals(other.debit))
			return false;
		if (homeCredit == null) {
			if (other.homeCredit != null)
				return false;
		} else if (!homeCredit.equals(other.homeCredit))
			return false;
		if (homeDebit == null) {
			if (other.homeDebit != null)
				return false;
		} else if (!homeDebit.equals(other.homeDebit))
			return false;
		if (narration == null) {
			if (other.narration != null)
				return false;
		} else if (!narration.equals(other.narration))
			return false;
		if (settlementDate == null) {
			if (other.settlementDate != null)
				return false;
		} else if (!settlementDate.equals(other.settlementDate))
			return false;
		if (srNo != other.srNo)
			return false;
		if (tranType == null) {
			if (other.tranType != null)
				return false;
		} else if (!tranType.equals(other.tranType))
			return false;
		return true;
	}

}
