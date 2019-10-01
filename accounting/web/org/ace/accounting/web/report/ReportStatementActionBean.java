package org.ace.accounting.web.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.ace.accounting.common.CurrencyType;
import org.ace.accounting.common.FormatStyle;
import org.ace.accounting.common.MonthNames;
import org.ace.accounting.common.ReportFormat;
import org.ace.accounting.common.utils.DateUtils;
import org.ace.accounting.common.utils.StringUtil;
import org.ace.accounting.common.validation.MessageId;
import org.ace.accounting.dto.LiabilitiesACDto;
import org.ace.accounting.dto.ReportFormatDto;
import org.ace.accounting.dto.ReportStatementDto;
import org.ace.accounting.process.interfaces.IUserProcessService;
import org.ace.accounting.report.ReportType;
import org.ace.accounting.report.reportStatement.service.interfaces.IReportStatementService;
import org.ace.accounting.system.branch.Branch;
import org.ace.accounting.system.branch.service.interfaces.IBranchService;
import org.ace.accounting.system.chartaccount.AccountType;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.currency.service.interfaces.ICurrencyService;
import org.ace.accounting.system.formatfile.service.interfaces.IFormatFileService;
import org.ace.java.component.SystemException;
import org.ace.java.web.common.BaseBean;
import org.apache.commons.io.FileUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CloseEvent;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@ManagedBean(name = "ReportStatementActionBean")
@ViewScoped
public class ReportStatementActionBean extends BaseBean {
	//
	// @ManagedProperty(value = "#{CoaService}")
	// private ICoaService coaService;
	//
	// public void setCoaService(ICoaService coaService) {
	// this.coaService = coaService;
	// }

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

	@ManagedProperty(value = "#{UserProcessService}")
	private IUserProcessService userProcessService;

	public void setUserProcessService(IUserProcessService userProcessService) {
		this.userProcessService = userProcessService;
	}

	@ManagedProperty(value = "#{FormatFileService}")
	private IFormatFileService formatFileService;

	public void setFormatFileService(IFormatFileService formatFileService) {
		this.formatFileService = formatFileService;
	}

	@ManagedProperty(value = "#{ReportStatementService}")
	private IReportStatementService reportStatementService;

	public void setReportStatementService(IReportStatementService reportStatementService) {
		this.reportStatementService = reportStatementService;
	}

	private boolean isReportTypeSelection;
	private ReportType reportType;

	private boolean isCurrencySelection;
	private CurrencyType currencyType;
	private Currency currency;

	private boolean isReportSelection;
	private boolean isBranchDisabled;
	private Branch branch;
	private int reportMonth;
	private int reportYear;
	private EnumSet<MonthNames> monthSet;
	private List<Integer> yearList;
	private String heading;
	private ReportFormat reportFormat;
	private FormatStyle formatStyle;
	private ReportFormatDto reportFormatDto;
	private List<ReportFormatDto> reportFormatDtoList = null;
	private List<ReportFormatDto> filteredList = null;
	private List<ReportStatementDto> reportStatementDtoList = null;
	private boolean isIncludeObal;

	private boolean isPreviewDone = false;
	private boolean isLacSelection;
	private List<LiabilitiesACDto> lAcodeDtos;
	private LiabilitiesACDto plAcode;
	private LiabilitiesACDto taxAcode;

	private final String reportName = "ReportStatement";
	private final String pdfDirPath = "/pdf-report/" + reportName + "/" + System.currentTimeMillis() + "/";
	private final String dirPath = getWebRootPath() + pdfDirPath;
	private final String fileName = reportName + ".pdf";

	@PostConstruct
	public void init() {
		monthSet = EnumSet.allOf(MonthNames.class);
		yearList = DateUtils.getActiveYears();
		reportType = ReportType.PL;
		isReportTypeSelection = true;
		// lAcodeDtos = coaService.findLiabilitiesACDtos();
	}

	public void selectReportType() {
		currencyType = CurrencyType.HOMECURRENCY;
		isCurrencySelection = true;
		isReportTypeSelection = false;
		isReportSelection = false;
		createNewReport();
	}

	public void selectCurrency() {
		isReportSelection = true;
		isCurrencySelection = false;
		isReportTypeSelection = false;
	}

	public void back() {
		if (isCurrencySelection) {
			reportType = ReportType.PL;
			isReportTypeSelection = true;
			isCurrencySelection = false;
			isReportSelection = false;
		} else if (isReportSelection) {
			if (isPreviewDone && reportType.equals(ReportType.PL)) {
				isLacSelection = true;
				isReportTypeSelection = false;
				isCurrencySelection = false;
				isReportSelection = false;
				lAcodeDtos = new ArrayList<>();
				for (ReportStatementDto dto : reportStatementDtoList) {
					if (dto.getAcType() != null && dto.getAcType().equals(AccountType.L)) {
						LiabilitiesACDto lacDto = new LiabilitiesACDto(dto.getAcCode(), dto.getdCode());
						if (!lAcodeDtos.contains(lacDto)) {
							lAcodeDtos.add(lacDto);
						}
					}
					if (dto.getRacType() != null && dto.getRacType().equals(AccountType.L)) {
						LiabilitiesACDto lacDto = new LiabilitiesACDto(dto.getrAcCode(), dto.getdCode());
						if (!lAcodeDtos.contains(lacDto)) {
							lAcodeDtos.add(lacDto);
						}
					}
				}
				lAcodeDtos.sort(new Comparator<LiabilitiesACDto>() {
					@Override
					public int compare(LiabilitiesACDto dto1, LiabilitiesACDto dto2) {
						String acCode1 = dto1.getAcCode().toUpperCase();
						String acCode2 = dto2.getAcCode().toUpperCase();
						return acCode1.compareTo(acCode2);
					}
				});
				if (lAcodeDtos.size() == 0) {
					isLacSelection = false;
					reportType = ReportType.PL;
					isReportTypeSelection = true;
					addErrorMessage(null, MessageId.NO_SUITABLE_ACCOUNT);
				}
			} else {
				isCurrencySelection = true;
				currencyType = CurrencyType.HOMECURRENCY;
				currency = null;
				isReportTypeSelection = false;
				isReportSelection = false;
			}
		} else if (isLacSelection) {
			reportType = ReportType.PL;
			isReportTypeSelection = true;
			isCurrencySelection = false;
			isLacSelection = false;
			isReportSelection = false;
			isPreviewDone = false;
			reportStatementDtoList = null;
		}

	}

	public void preview() {
		if (validate()) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.set(reportYear, reportMonth, 1);
				Date reportDate = cal.getTime();

				reportStatementDtoList = reportStatementService.previewProcedure(isIncludeObal, reportType, currencyType, currency, branch, reportDate,
						reportFormatDto.getFormatType());

				/*
				 * if (reportType.equals(ReportType.BS)) {
				 * reportStatementDtoList =
				 * reportStatementService.liabilitiesAcodeCheck(plAcode,
				 * taxAcode, reportStatementDtoList); }
				 */
				reportStatementDtoList.forEach(System.out::println);

				generateReport(reportStatementDtoList);
				PrimeFaces.current().executeScript("PF('reportStatementPdfDialog').show();");
				isPreviewDone = true;
			} catch (SystemException se) {
				handleSysException(se);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	public void submitLAC() {
		// TODO -- to consult
		// here , taxAcode and plAcode dtos just have to hold the cbal for BS
		// reports
		// -----
		// get the first ever match instead of total throughout the list
		// even though there are duplicate acode in same formattype
		// try following query
		/*
		 * select * from ( SELECT FORMATSTATUS,FORMATTYPE,ACODE,COUNT(ACODE) as
		 * c FROM FORMATFILE where ACODE!='' GROUP By FORMATTYPE,ACODE
		 * ,FORMATSTATUS )x where c>1 order by FORMATTYPE
		 */
		// and only match the ACODE ( RACODE not included)
		for (ReportStatementDto dto : reportStatementDtoList) {
			if (plAcode.getAcCode().equals(dto.getAcCode())) {
				plAcode.setcBal(dto.getcBal());
				break;
			}
		}
		// did two time , to get first result for each
		for (ReportStatementDto dto : reportStatementDtoList) {
			if (taxAcode.getAcCode().equals(dto.getAcCode())) {
				taxAcode.setcBal(dto.getcBal());
				break;
			}
		}
		reportType = ReportType.BS;
		isReportTypeSelection = true;
		isCurrencySelection = false;
		isReportSelection = false;
		isPreviewDone = false;
		isLacSelection = false;
		createNewReport();
	}

	private boolean validate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		// if reportYear is greater than current year
		if (reportYear == cal.get(Calendar.YEAR) && reportMonth > cal.get(Calendar.MONTH)) {
			addErrorMessage(null, MessageId.MONTH_EXCEEDED);
		} else {
			return true;
		}
		return false;
	}

	private void generateReport(List<ReportStatementDto> reportStatementDtoList2) throws Exception {
		try {
			InputStream inputStream;
			if (formatStyle.equals(FormatStyle.VF)) {
				if (isIncludeObal) {
					inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("reportStatementReportWithObal.jrxml");
				} else {
					inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("reportStatementReport.jrxml");
				}
			} else {
				inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("reportStatementReportHorizontal.jrxml");
			}
			
//			System.out.println((reportStatementDtoList2.get(reportStatementDtoList2.size()-1).getrAcCode()!=null || reportStatementDtoList2.get(reportStatementDtoList2.size()-1).getrAcCode().equals("")));

			Map<String, Object> parameters = new HashMap<>();
			String newDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			String branchString = branch == null ? branchString = "All Branches" : branch.getName();
			String currencyString = currency == null ? "All Currencies" : currency.getCurrencyCode();
			if (currencyType.equals(CurrencyType.HOMECURRENCY)) {
				currencyString = currencyString + " By Home Amount";
			}
			String image = FacesContext.getCurrentInstance().getExternalContext().getRealPath("pdf-report/fni-logo.png");
			parameters.put("FORMAT_NAME", reportFormatDto.getFormatName());
			parameters.put("BRANCH", branchString);
			parameters.put("CURRENCY", currencyString);
			parameters.put("NEWDATE", newDate);
			parameters.put("LOGO", image);
			parameters.put("REPORT_MONTH", StringUtil.getMonthName(reportMonth + 1) + " - " + reportYear);
			parameters.put("HEADING", heading);
			parameters.put("DTOLIST", new JRBeanCollectionDataSource(reportStatementDtoList2));
			JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
			FileUtils.forceMkdir(new File(dirPath));
			JasperExportManager.exportReportToPdfFile(jasperPrint, dirPath + fileName);

		} catch (SystemException e) {
			throw e;
		} catch (Exception ee) {
			throw ee;
		}
	}

	public void rebindData() {
		filteredList = null;
		reportFormatDtoList = formatFileService.findByReportType(reportType);
	}

	public void createNewReport() {
		reportFormat = ReportFormat.AF;
		formatStyle = FormatStyle.HF;
		isIncludeObal = false;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		reportMonth = cal.get(Calendar.MONTH);
		reportYear = cal.get(Calendar.YEAR);
		if (userProcessService.getLoginUser().isAdmin()) {
			isBranchDisabled = false;
			branch = null;
		} else {
			isBranchDisabled = true;
			branch = userProcessService.getLoginUser().getBranch();
		}
		if (!yearList.contains(reportYear)) {
			reportYear = yearList.get(yearList.size() - 1);
		}
		heading = null;
		reportFormatDto = null;
		rebindData();
	}

	public boolean isReportTypeSelection() {
		return isReportTypeSelection;
	}

	public boolean isCurrencySelection() {
		return isCurrencySelection;
	}

	public boolean isReportSelection() {
		return isReportSelection;
	}

	public List<ReportType> getReportTypes() {
		return Arrays.asList(ReportType.values());
	}

	public ReportType getReportType() {
		return reportType;
	}

	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	public void setCurrencyType(CurrencyType currencyType) {
		this.currencyType = currencyType;
	}

	public CurrencyType getCurrencyType() {
		return currencyType;
	}

	public List<CurrencyType> getCurTypes() {
		return Arrays.asList(CurrencyType.values());
	}

	public List<Currency> getCurrencies() {
		return currencyService.findAllCurrency();
	}

	public List<Branch> getBranches() {
		return branchService.findAllBranch();
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Currency getCurrency() {
		return currency;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public boolean isBranchDisabled() {
		return isBranchDisabled;
	}

	public void setReportFormat(ReportFormat reportFormat) {
		this.reportFormat = reportFormat;
	}

	public ReportFormat getReportFormat() {
		return reportFormat;
	}

	public List<ReportFormat> getReportFormats() {
		return Arrays.asList(ReportFormat.values());
	}

	public boolean isIncludeObal() {
		return isIncludeObal;
	}

	public void setIncludeObal(boolean isIncludeObal) {
		this.isIncludeObal = isIncludeObal;
	}

	public void setFormatStyle(FormatStyle formatStyle) {
		this.formatStyle = formatStyle;
	}

	public FormatStyle getFormatStyle() {
		return formatStyle;
	}

	public List<FormatStyle> getFormatStyles() {
		return Arrays.asList(FormatStyle.values());
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getHeading() {
		return heading;
	}

	public ReportFormatDto getReportFormatDto() {
		return reportFormatDto;
	}

	public void setReportFormatDto(ReportFormatDto dto) {
		this.reportFormatDto = dto;
	}

	public List<ReportFormatDto> getReportFormatDtoList() {
		return reportFormatDtoList == null ? new ArrayList<>() : reportFormatDtoList;
	}

	public void selectFormatFileDto(ReportFormatDto dto) {
		this.reportFormatDto = dto;
	}

	public int getReportMonth() {
		return reportMonth;
	}

	public void setReportMonth(int reportMonth) {
		this.reportMonth = reportMonth;
	}

	public int getReportYear() {
		return reportYear;
	}

	public void setReportYear(int reportYear) {
		this.reportYear = reportYear;
	}

	public EnumSet<MonthNames> getMonthSet() {
		return monthSet;
	}

	public List<Integer> getYearList() {
		return yearList;
	}

	public boolean getIsLacSelection() {
		return isLacSelection;
	}

	public String getStream() {
		String fullFilePath = pdfDirPath + fileName;
		return fullFilePath;
	}

	public List<ReportFormatDto> getFilteredList() {
		return filteredList;
	}

	public void setFilteredList(List<ReportFormatDto> filteredList) {
		this.filteredList = filteredList;
	}

	public List<LiabilitiesACDto> getlAcodeDtos() {
		return lAcodeDtos;
	}

	public void setPlAcode(LiabilitiesACDto plAcode) {
		this.plAcode = plAcode;
	}

	public LiabilitiesACDto getPlAcode() {
		return plAcode;
	}

	public void setTaxAcode(LiabilitiesACDto taxAcode) {
		this.taxAcode = taxAcode;
	}

	public LiabilitiesACDto getTaxAcode() {
		return taxAcode;
	}

	public void handleClose(CloseEvent event) {
		try {
			FileUtils.forceDelete(new File(dirPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
