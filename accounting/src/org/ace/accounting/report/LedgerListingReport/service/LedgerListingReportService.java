package org.ace.accounting.report.LedgerListingReport.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.ace.accounting.common.CurrencyType;
import org.ace.accounting.common.utils.BusinessUtil;
import org.ace.accounting.common.utils.DateUtils;
import org.ace.accounting.dto.AccountLedgerDto;
import org.ace.accounting.dto.GLDBDto;
import org.ace.accounting.dto.GenLedgerDetail;
import org.ace.accounting.dto.GenLedgerDto;
import org.ace.accounting.dto.LedgerDto;
import org.ace.accounting.report.LedgerListingReport.persistence.interfaces.ILedgerListingReportDAO;
import org.ace.accounting.report.LedgerListingReport.service.interfaces.ILedgerListingReportService;
import org.ace.accounting.system.branch.Branch;
import org.ace.accounting.system.chartaccount.AccountType;
import org.ace.accounting.system.chartaccount.ChartOfAccount;
import org.ace.accounting.system.chartaccount.persistence.interfaces.ICoaDAO;
import org.ace.accounting.system.currency.Currency;
import org.ace.java.component.SystemException;
import org.ace.java.component.persistence.exception.DAOException;
import org.ace.java.component.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "LedgerListingReportService")
public class LedgerListingReportService extends BaseService implements ILedgerListingReportService {

	@Resource(name = "LedgerListingReportDAO")
	private ILedgerListingReportDAO ledgerListingReportDAO;

	@Resource(name = "CoaDAO")
	private ICoaDAO coaDAO;

	private BigDecimal findCCOAOpeningBalance(ChartOfAccount coa, LedgerDto ledgerDto) throws SystemException {
		String budgetYear = "";
		String openingField = "";
		BigDecimal dblBalance;
		StringBuffer sf;
		Date startDate = ledgerDto.getStartDate();
		CurrencyType currencyType = ledgerDto.getCurrencyType();
		Branch branch = ledgerDto.getBranch();
		Currency currency = ledgerDto.getCurrency();

		budgetYear = BusinessUtil.getBudgetInfo(ledgerDto.getStartDate(), 2);
		if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
			openingField = BusinessUtil.getMonthSRCStatusJPQLField(Integer.valueOf(BusinessUtil.getBudgetInfo(startDate, 3)) - 1);
		} else {
			if (ledgerDto.isHomeCurrencyConverted()) {
				openingField = BusinessUtil.getMonthSRCStatusJPQLField(Integer.valueOf(BusinessUtil.getBudgetInfo(startDate, 3)) - 1);
			} else {
				openingField = BusinessUtil.getMonthStatusJPQLField(Integer.valueOf(BusinessUtil.getBudgetInfo(startDate, 3)) - 1);
			}
		}

		if (openingField.equals("msrcRate.msrc0")) {
			openingField = "hOBal";
		} else if (openingField.equals("monthlyRate.m0")) {
			openingField = "oBal";
		}

		sf = new StringBuffer("SELECT Sum(c." + openingField + ") FROM VwCcoa c WHERE c.coa=:coa AND c.budget=:budgetYear");
		// if (coa == null) {
		// sf = new StringBuffer("SELECT Sum(c." + openingField
		// + ") FROM CurrencyChartOfAccount c WHERE
		// c.coa.acCodeType!=org.ace.accounting.system.chartaccount.AccountCodeType.HEAD
		// AND
		// c.coa.acCodeType!=org.ace.accounting.system.chartaccount.AccountCodeType.GROUP
		// AND c.budget=:budgetYear");
		// }

		if (ledgerDto.isAllBranch() && ledgerDto.isAdmin()) {
			if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
				dblBalance = ledgerListingReportDAO.finddblBalance(sf, coa, budgetYear, null, null);
			} else {
				sf.append(" AND c.currency=:currency");
				dblBalance = ledgerListingReportDAO.finddblBalance(sf, coa, budgetYear, currency, null);
			}
		} else {
			if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
				sf.append(" AND c.branch=:branch");
				dblBalance = ledgerListingReportDAO.finddblBalance(sf, coa, budgetYear, null, branch);
			} else {
				sf.append(" AND c.currency=:currency AND c.branch=:branch");
				dblBalance = ledgerListingReportDAO.finddblBalance(sf, coa, budgetYear, currency, branch);
			}
		}
		return dblBalance;
	}

	public List<AccountLedgerDto> findAccLedgerListing(LedgerDto ledgerDto, List<ChartOfAccount> coaList) throws SystemException {
		int sDate;
		List<AccountLedgerDto> debitCreditList = new ArrayList<AccountLedgerDto>();
		List<AccountLedgerDto> debitCreditList1 = new ArrayList<AccountLedgerDto>();
		List<AccountLedgerDto> accLedgerList = new ArrayList<AccountLedgerDto>();
		StringBuffer sf;
		Date startDate = ledgerDto.getStartDate();
		Date endDate = ledgerDto.getEndDate();
		endDate = DateUtils.resetEndDate(endDate);
		CurrencyType currencyType = ledgerDto.getCurrencyType();
		Branch branch = ledgerDto.getBranch();
		Currency currency = ledgerDto.getCurrency();
		boolean allBranch = ledgerDto.isAllBranch();
		boolean homeCurrencyConverted = ledgerDto.isHomeCurrencyConverted();
		boolean admin = ledgerDto.isAdmin();

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		sDate = cal.get(Calendar.DATE);

		for(ChartOfAccount coa:coaList) {
			
		
		BigDecimal dblBalance = findCCOAOpeningBalance(coa, ledgerDto);

		int srNo = 0;
		if (sDate > 1) {
			sf = new StringBuffer(
					"SELECT NEW org.ace.accounting.dto.AccountLedgerDto(t.ccoa.coa, t.localAmount, t.localAmount, t.homeAmount, t.homeAmount,t.narration, t.currency, t.branch, t.tranType) FROM TLF t WHERE t.reverse=0 AND t.paid=1 AND t.settlementDate BETWEEN "
							+ ":startDate AND :endDate" + " AND t.ccoa.coa=:coa ORDER BY t.settlementDate");
			accLedgerList = findDebitCreditByStartDate(sf, coa, startDate);
			if (allBranch && admin) { // For All Branch
				if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
					// if Currency is homecurrency
					// home debit, home credit
					debitCreditList = accLedgerList.stream()
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				} else { // Not Home Currency
					if (homeCurrencyConverted) { // By Home Currency Converted
						//// homeDebit, homeCredit
						debitCreditList = accLedgerList.stream().filter(p -> p.getCurrency().equals(currency))
								.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate()))
								.collect(Collectors.toList());
					} else {
						//// Debit, Credit
						debitCreditList = accLedgerList.stream().filter(p -> p.getCurrency().equals(currency))
								.map(p -> new AccountLedgerDto(p.getCoa(), p.getDebit(), p.getCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
					}
				}
			} else { // Not All Branch
				if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
					// homeDebit, homeCredit
					debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch))
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				} else {// Not Home currency
					if (homeCurrencyConverted) {// By Home Currency Converted
						// homeDebit, homeCredit
						debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch) && p.getCurrency().equals(currency))
								.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate()))
								.collect(Collectors.toList());
					} else {
						// Debit, Credit
						debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch) && p.getCurrency().equals(currency))
								.map(p -> new AccountLedgerDto(p.getCoa(), p.getDebit(), p.getCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
					}
				}
			}
			for (AccountLedgerDto dto : debitCreditList) {
				if (dto.getCoa().getAcType().equals(AccountType.A) || dto.getCoa().getAcType().equals(AccountType.E)) {
					dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getCredit().doubleValue());
					dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getDebit().doubleValue());
				} else if (dto.getCoa().getAcType().equals(AccountType.I) || dto.getCoa().getAcType().equals(AccountType.L)) {
					dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getCredit().doubleValue());
					dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getDebit().doubleValue());
				}
				dto.setDblBalance(dblBalance);
				dto.setSrNo(srNo);
			}
		} // End of if sDate>1

		srNo = 1;
		debitCreditList1.add(new AccountLedgerDto(coa, BigDecimal.ZERO, BigDecimal.ZERO, "Opening Balance", dblBalance, startDate, srNo));
		sf = new StringBuffer(
				"SELECT NEW org.ace.accounting.dto.AccountLedgerDto(t.ccoa.coa, t.localAmount, t.localAmount, t.homeAmount, t.homeAmount, t.narration, t.settlementDate, t.currency, t.branch, t.tranType) FROM TLF t WHERE t.reverse=0 AND t.paid=1 AND t.settlementDate BETWEEN "
						+ ":startDate" + " AND " + ":endDate" + " AND t.ccoa.coa=:coa ORDER BY t.settlementDate");
		accLedgerList = new ArrayList<AccountLedgerDto>();
		debitCreditList = new ArrayList<AccountLedgerDto>();
		accLedgerList = findDebitCreditBySDateAndEDate(sf, coa, startDate, endDate);

		// Collections.sort(accLedgerList);

		// Find debit credit by startDate and EndDate
		if (allBranch && admin) { // For All Branch
			if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
				debitCreditList = accLedgerList.stream().map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate()))
						.collect(Collectors.toList());
			} else {
				if (homeCurrencyConverted) {
					debitCreditList = accLedgerList.stream().filter(p -> p.getCurrency().equals(currency))
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				} else {
					debitCreditList = accLedgerList.stream().filter(p -> p.getCurrency().equals(currency))
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getDebit(), p.getCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				}
			}
		} else {
			if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
				debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch))
						.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
			} else {
				if (homeCurrencyConverted) {
					debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch) && p.getCurrency().equals(currency))
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getHomeDebit(), p.getHomeCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				} else {
					debitCreditList = accLedgerList.stream().filter(p -> p.getBranch().equals(branch) && p.getCurrency().equals(currency))
							.map(p -> new AccountLedgerDto(p.getCoa(), p.getDebit(), p.getCredit(), p.getNarration(), p.getSettlementDate())).collect(Collectors.toList());
				}
			}
		}

		for (AccountLedgerDto dto : debitCreditList) {
			srNo += 1;
			if (dto.getCoa().getAcType().equals(AccountType.A) || dto.getCoa().getAcType().equals(AccountType.E)) {
				dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getCredit().doubleValue());
				dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getDebit().doubleValue());
			} else if (dto.getCoa().getAcType().equals(AccountType.I) || dto.getCoa().getAcType().equals(AccountType.L)) {
				dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getCredit().doubleValue());
				dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getDebit().doubleValue());
			}
			dto.setDblBalance(dblBalance);
			dto.setSrNo(srNo);
			debitCreditList1.add(dto);
		}
		srNo += 1;
		debitCreditList1.add(new AccountLedgerDto(coa, BigDecimal.ZERO, BigDecimal.ZERO, "Closing Balance", dblBalance, endDate, srNo));
		}
		return debitCreditList1;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<AccountLedgerDto> findDebitCreditByStartDate(StringBuffer sf, ChartOfAccount coa, Date sDate) throws SystemException {
		List<AccountLedgerDto> result = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(sDate);

		int month = (cal.get(Calendar.MONTH)) == 0 ? 1 : (cal.get(Calendar.MONTH));
		month = month + 1;
		Date startDate = DateUtils.formatStringToDate("1" + "-" + month + "-" + (cal.get(Calendar.YEAR)));
		Date endDate = DateUtils.minusDays(sDate, 1);

		try {
			result = ledgerListingReportDAO.findDebitCreditByStartDate(sf, coa, startDate, endDate);
			if (result == null) {
				sf = new StringBuffer(sf.toString().replace("FROM TLF", "FROM TLFHIST"));
				result = ledgerListingReportDAO.findDebitCreditByStartDate(sf, coa, startDate, endDate);
			}
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), "Failed to find COA By Branch ID.", e);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<AccountLedgerDto> findDebitCreditBySDateAndEDate(StringBuffer sf, ChartOfAccount coa, Date startDate, Date endDate) throws SystemException {
		List<AccountLedgerDto> result = null;
		try {
			result = ledgerListingReportDAO.findDebitCreditBySDateAndEDate(sf, coa, startDate, endDate);
			if (result == null) {
				sf = new StringBuffer(sf.toString().replace("FROM TLF", "FROM TLFHIST"));
				result = ledgerListingReportDAO.findDebitCreditBySDateAndEDate(sf, coa, startDate, endDate);
			}
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), "Failed to find COA By Branch ID.", e);
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<GenLedgerDto> findGenLedgerListing(LedgerDto ledgerDto, ChartOfAccount targetCoa) throws SystemException {
		List<GenLedgerDto> result = new ArrayList<GenLedgerDto>();
		try {
			// List<AccountLedgerDto> debitCreditList = new
			// ArrayList<AccountLedgerDto>();
			// List<AccountLedgerDto> accLedgerList = new
			// ArrayList<AccountLedgerDto>();

			List<ChartOfAccount> coaList = new ArrayList<ChartOfAccount>();

			Date startDate = DateUtils.resetStartDate(ledgerDto.getStartDate());
			Date endDate = DateUtils.resetEndDate(ledgerDto.getEndDate());
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			int sDate = cal.get(Calendar.DATE);

			if (targetCoa == null) {
				coaList = coaDAO.findAllCOAByAccountCodeType();
			} else {
				coaList.add(targetCoa);
			}

			for (ChartOfAccount coa : coaList) {
				int srNo = 0;
				List<GenLedgerDetail> detailList = new ArrayList<GenLedgerDetail>();
				GenLedgerDto genLedgerdto = new GenLedgerDto();
				GenLedgerDetail genLedgerDetail = new GenLedgerDetail();

				genLedgerdto.setCoa(coa);

				// TODO FIXME
				BigDecimal dblBalance = findCCOAOpeningBalance(coa, ledgerDto);

				// --- between first to 1 day before start date start ---
				if (sDate > 1) {
					BigDecimal unpostedBalance = getUnpostedBalance(ledgerDto, coa);
					dblBalance = dblBalance.add(unpostedBalance);
				}
				// --- between first to 1 day before start date end ---

				srNo = 1;

				genLedgerDetail = new GenLedgerDetail(BigDecimal.ZERO, BigDecimal.ZERO, dblBalance, startDate, srNo);
				detailList.add(genLedgerDetail);

				// --- between start date and end date start ---

				List<GLDBDto> ledgerList = ledgerListingReportDAO.findGeneralLedgerList(ledgerDto, coa, startDate, endDate);
				for (GLDBDto dto : ledgerList) {
					srNo += 1;
					if (coa.getAcType().equals(AccountType.A) || coa.getAcType().equals(AccountType.E)) {
						dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getCredit().doubleValue());
						dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getDebit().doubleValue());
					} else if (coa.getAcType().equals(AccountType.I) || coa.getAcType().equals(AccountType.L)) {
						dblBalance = new BigDecimal(dblBalance.doubleValue() + dto.getCredit().doubleValue());
						dblBalance = new BigDecimal(dblBalance.doubleValue() - dto.getDebit().doubleValue());
					}
					detailList.add(new GenLedgerDetail(dto.getDebit(), dto.getCredit(), dblBalance, dto.getSettlementDate(), srNo));
				}
				srNo += 1;
				detailList.add(new GenLedgerDetail(BigDecimal.ZERO, BigDecimal.ZERO, dblBalance, endDate, srNo));

				genLedgerdto.setDetailList(detailList);
				result.add(genLedgerdto);
			}
		} catch (DAOException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		}
		return result;
	}

	private BigDecimal getUnpostedBalance(LedgerDto ledgerDto, ChartOfAccount coa) {
		BigDecimal result = BigDecimal.ZERO;

		Calendar cal = Calendar.getInstance();
		cal.setTime(DateUtils.resetStartDate(ledgerDto.getStartDate()));
		Date startDate = DateUtils.formatStringToDate("1" + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + (cal.get(Calendar.YEAR)));
		Date endDate = DateUtils.resetEndDate(DateUtils.minusDays(ledgerDto.getStartDate(), 1));

		List<GLDBDto> unpostedList = ledgerListingReportDAO.findGeneralLedgerList(ledgerDto, coa, startDate, endDate);

		for (GLDBDto dto : unpostedList) {
			if (coa.getAcType().equals(AccountType.A) || coa.getAcType().equals(AccountType.E)) {
				result = new BigDecimal(result.doubleValue() - dto.getCredit().doubleValue());
				result = new BigDecimal(result.doubleValue() + dto.getDebit().doubleValue());
			} else if (coa.getAcType().equals(AccountType.I) || coa.getAcType().equals(AccountType.L)) {
				result = new BigDecimal(result.doubleValue() + dto.getCredit().doubleValue());
				result = new BigDecimal(result.doubleValue() - dto.getDebit().doubleValue());
			}
		}

		return result;
	}
}
