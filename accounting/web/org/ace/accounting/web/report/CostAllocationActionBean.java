package org.ace.accounting.web.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.ace.accounting.common.CurrencyType;
import org.ace.accounting.common.MonthNames;
import org.ace.accounting.common.utils.DateUtils;
import org.ace.accounting.common.validation.MessageId;
import org.ace.accounting.dto.CostAllocationReportDto;
import org.ace.accounting.process.interfaces.IUserProcessService;
import org.ace.accounting.report.costAllocation.service.interfaces.ICostAllocationService;
import org.ace.accounting.system.branch.Branch;
import org.ace.accounting.system.branch.service.interfaces.IBranchService;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.currency.service.interfaces.ICurrencyService;
import org.ace.accounting.system.tlf.service.interfaces.ITLFService;
import org.ace.java.web.common.BaseBean;
import org.apache.commons.io.FileUtils;
import org.primefaces.PrimeFaces;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@ManagedBean(name = "CostAllocationActionBean")
@ViewScoped
public class CostAllocationActionBean extends BaseBean {
	@ManagedProperty(value = "#{UserProcessService}")
	private IUserProcessService userProcessService;

	public void setUserProcessService(IUserProcessService userProcessService) {
		this.userProcessService = userProcessService;
	}

	@ManagedProperty(value = "#{CurrencyService}")
	private ICurrencyService currencyService;

	public void setCurrencyService(ICurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@ManagedProperty(value = "#{BranchService}")
	private IBranchService branchService;

	public void setBranchService(IBranchService branchService) {
		this.branchService = branchService;
	}

	@ManagedProperty(value = "#{CostAllocationService}")
	private ICostAllocationService allocationService;

	public void setAllocationService(ICostAllocationService allocationService) {
		this.allocationService = allocationService;
	}

	@ManagedProperty(value = "#{TLFService}")
	private ITLFService tlfService;

	public void setTlfService(ITLFService tlfService) {
		this.tlfService = tlfService;
	}

	// To choose currency type HomeCurrency or SourceCurrency.
	private CurrencyType currencyType;
	// To check HomeCurrency or not.
	private Branch branch;
	private Currency currency;
	// Convert SourceCurrency to HomeCurrency or not convert.
	private boolean isCurrencyConverted;
	// Allocated Year.
	private int allocateYear;
	// Allocated Month.
	private int allocateMonth;
	// Allocate amount or not.
	private boolean isAllocateProcess;
	// CostAllocationReportDto.
	private CostAllocationReportDto dto;
	// Lists of CostAllocationReportDto.
	private List<CostAllocationReportDto> dtoList;
	// report name.
	private final String reportName = "costAllocationReport";
	// pdf path.
	private final String pdfDirPath = "/pdf-report/" + reportName + "/" + System.currentTimeMillis() + "/";
	private final String dirPath = getWebRootPath() + pdfDirPath;
	// pdf name.
	private final String fileName = reportName + ".pdf";

	private boolean isBranchDisabled = true;
	private EnumSet<MonthNames> monthSet;
	private List<Integer> yearList;

	@PostConstruct
	public void init() {
		monthSet = EnumSet.allOf(MonthNames.class);
		yearList = DateUtils.getActiveYears();
		dto = new CostAllocationReportDto();
		isCurrencyConverted = false;
		currency = currencyService.findHomeCurrency();
		currencyType = CurrencyType.HOMECURRENCY;

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		allocateMonth = cal.get(Calendar.MONTH);
		allocateYear = cal.get(Calendar.YEAR);
		if (userProcessService.getLoginUser().isAdmin()) {
			isBranchDisabled = false;
		} else {
			isBranchDisabled = true;
			branch = userProcessService.getLoginUser().getBranch();
		}
	}

	/**
	 * To view Cost Allocation Report.
	 */
	public void preview() {
		dtoList = allocationService.findCostAllocationReport(currencyType, branch, currency, String.valueOf(allocateYear), String.valueOf(allocateMonth), isCurrencyConverted);
		if (dtoList.size() == 0) {
			addErrorMessage(null, MessageId.NO_RESULT);
		} else {
			try {
				if (previewReport(dtoList)) {
					PrimeFaces.current().executeScript("PF('costAllocationDialog').show();");
					/*
					 * RequestContext context = RequestContext.getCurrentInstance();
					 * context.execute("PF('costAllocationDialog').show();");
					 */
				}
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	/**
	 * To generate Cost Allocation Report.
	 * 
	 * @param dtoList[CostAllocationReportDto
	 *            List]
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean previewReport(List<CostAllocationReportDto> dtoList) throws IOException, JRException {
		try {
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("costAllocationReport.jrxml");
			Map parameters = new HashMap();
			String reportDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
			parameters.put("reportDate", reportDate);
			parameters.put("reportTime", DateUtils.formatDateToStringTime(new Date()));
			parameters.put("BRANCH", branch == null ? "All Branches" : branch.getName());
			parameters.put("CURRENCY", currency == null ? "All Currencies" : currency.getCurrencyCode() + (isCurrencyConverted ? " By Home Currency Converted" : ""));
			JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(dtoList));
			FileUtils.forceMkdir(new File(dirPath));
			JasperExportManager.exportReportToPdfFile(jasperPrint, dirPath + fileName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			addErrorMessage(null, MessageId.REPORT_ERROR);
			return false;
		}

	}

	public String getStream() {
		String filePath = pdfDirPath + fileName;
		return filePath;
	}

	/**
	 * To allocate amount.
	 */
	public void allocationProcess() {
		if (isAllocateProcess == true) {
			allocationService.allocateProcess(currencyType, branch, currency, String.valueOf(allocateYear), String.valueOf(allocateMonth), isCurrencyConverted);
		}
	}

	/**
	 * To get all currency lists.
	 * 
	 * @return List[all currency]
	 */
	public List<Currency> getCurrencyList() {
		return currencyService.findAllCurrency();
	}

	/**
	 * To get all branch lists.
	 * 
	 * @return List[all branch]
	 */
	public List<Branch> getBranchList() {
		return branchService.findAllBranch();
	}

	public CostAllocationReportDto getDto() {
		return dto;
	}

	public List<CostAllocationReportDto> getDtoList() {
		return dtoList;
	}

	public boolean isAllocateProcess() {
		return isAllocateProcess;
	}

	public void setAllocateProcess(boolean isAllocateProcess) {
		this.isAllocateProcess = isAllocateProcess;
	}

	public CurrencyType[] getCurrencyTypes() {
		return CurrencyType.values();
	}

	public CurrencyType getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(CurrencyType currencyType) {
		this.currencyType = currencyType;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public int getAllocateYear() {
		return allocateYear;
	}

	public void setAllocateYear(int allocateYear) {
		this.allocateYear = allocateYear;
	}

	public int getAllocateMonth() {
		return allocateMonth;
	}

	public void setAllocateMonth(int allocateMonth) {
		this.allocateMonth = allocateMonth;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public boolean isCurrencyConverted() {
		return isCurrencyConverted;
	}

	public void setCurrencyConverted(boolean isCurrencyConverted) {
		this.isCurrencyConverted = isCurrencyConverted;
	}

	public boolean isBranchDisabled() {
		return isBranchDisabled;
	}

	public List<Integer> getYearList() {
		return yearList;
	}

	public EnumSet<MonthNames> getMonthSet() {
		return monthSet;
	}
}
