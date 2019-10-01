package org.ace.accounting.report.trialBalanceDetail.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.ace.accounting.common.CurrencyType;
import org.ace.accounting.common.utils.BusinessUtil;
import org.ace.accounting.dto.TrialBalanceCriteriaDto;
import org.ace.accounting.dto.TrialBalanceReportDto;
import org.ace.accounting.report.TrialBalanceAccountType;
import org.ace.accounting.report.trialBalanceDetail.persistence.interfaces.ITrialBalanceDetailDAO;
import org.ace.accounting.system.setup.persistence.interfaces.ISetupDAO;
import org.ace.java.component.persistence.BasicDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Aung
 *
 */

@Repository("TrialBalanceDetailDAO")
public class TrialBalanceDetailDAO extends BasicDAO implements ITrialBalanceDetailDAO {

	@Resource(name = "SetupDAO")
	private ISetupDAO setupDAO;

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<TrialBalanceReportDto> findTrialBalanceDetailList(TrialBalanceCriteriaDto dto) {
		List<TrialBalanceReportDto> result = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.set(dto.getRequiredYear(), dto.getRequiredMonth(), 1);
			Date reportDate = cal.getTime();
			String reportBudgetMonth = BusinessUtil.getBudgetInfo(reportDate, 3);

			if (dto.getCurrencyType().equals(CurrencyType.HOMECURRENCY) || dto.isHomeCurrencyConverted()) {
				reportBudgetMonth = "MSRC" + reportBudgetMonth;
			} else {
				reportBudgetMonth = "M" + reportBudgetMonth;
			}

			StringBuffer queryString = new StringBuffer("SELECT ACODE,ACNAME,SUM(DEBIT),SUM(CREDIT) FROM (");
			queryString.append(" SELECT C.ACTYPE, CC.DEPARTMENTID ");
			if (dto.isGroup()) {
				if (dto.getAccountType().equals(TrialBalanceAccountType.Gl_ACODE)) {
					queryString.append(" , CASE WHEN ACTYPE IN('A','L') THEN C.ACCODE ELSE ");
					queryString.append(" (SELECT ACCODE FROM COA C2 WHERE C2.ID=C.HEADID) END AS ACODE ");
				} else {
					queryString.append(" , C.IBSBACODE AS ACODE ");
				}
				queryString.append(" , CASE WHEN ACTYPE IN('A','L') THEN C.ACNAME ");
				queryString.append(" ELSE (SELECT ACNAME FROM COA C2 WHERE C2.ID = C.HEADID ) END AS ACNAME ");
			} else {
				if (dto.getAccountType().equals(TrialBalanceAccountType.Gl_ACODE)) {
					queryString.append(" , C.ACCODE AS ACODE ");
				} else {
					queryString.append(" , C.IBSBACODE AS ACODE ");
				}
				if (dto.getBranch() == null) {
					queryString.append(" , C.ACNAME AS ACNAME ");
				} else {
					queryString.append(" , CC.ACNAME AS ACNAME ");
				}
			}
			queryString.append(" ,CASE WHEN ( ");
			queryString.append(" (ACTYPE IN ('I','L')  AND CC." + reportBudgetMonth + " < 0 ) OR ");
			queryString.append(" (ACTYPE IN ('A','E')  AND CC." + reportBudgetMonth + " > 0 ) ");
			queryString.append(" ) THEN ABS(CC." + reportBudgetMonth + ") ELSE 0 END AS DEBIT ");
			queryString.append(" ,CASE WHEN ( ");
			queryString.append(" (ACTYPE IN ('I','L')  AND CC." + reportBudgetMonth + " > 0 ) OR ");
			queryString.append(" (ACTYPE IN ('A','E')  AND CC." + reportBudgetMonth + " < 0 ) ");
			queryString.append(" ) THEN ABS(CC." + reportBudgetMonth + ") ELSE 0 END AS CREDIT ");
			queryString.append(" FROM COA AS C INNER JOIN VW_CCOA AS CC ON C.ID=CC.COAID WHERE CC.BUDGET=?budget ");
			queryString.append(" AND " + reportBudgetMonth + "<> 0 ");
			if (dto.isGroup()) {
				queryString.append(" AND C.ACCODETYPE='GROUP' ");
			} else {
				queryString.append(" AND C.ACCODETYPE='DETAIL' ");
			}

			if (dto.getCurrencyType().equals(CurrencyType.SOURCECURRENCY) && dto.getCurrency() != null) {
				queryString.append(" AND CC.CURRENCYID=?currencyid ");
			}

			if (dto.getBranch() != null) {
				queryString.append(" AND CC.BRANCHID=?branchid ");
			}
			queryString.append(" ) T");
			queryString.append(" GROUP BY ACODE,ACNAME ORDER BY ACODE");

			Query q = em.createNativeQuery(queryString.toString());

			q.setParameter("budget", BusinessUtil.getBudgetInfo(reportDate, 2));

			if (dto.getCurrencyType().equals(CurrencyType.SOURCECURRENCY) && dto.getCurrency() != null) {
				q.setParameter("currencyid", dto.getCurrency().getId());
			}
			if (dto.getBranch() != null) {
				q.setParameter("branchid", dto.getBranch().getId());
			}
			List<Object[]> objList = q.getResultList();
			if (objList != null) {
				result = new ArrayList<>();
				for (Object[] obj : objList) {
					TrialBalanceReportDto reportDto = new TrialBalanceReportDto();
					reportDto.setAcode(obj[0].toString());
					reportDto.setAcname(obj[1].toString());
					reportDto.setDebit(new BigDecimal(obj[2].toString()));
					reportDto.setCredit(new BigDecimal(obj[3].toString()));
					result.add(reportDto);
				}
			}
		} catch (PersistenceException pe) {
			throw translate("Failed to findTrialBalanceDetailList", pe);
		}
		return result;
	}
}
