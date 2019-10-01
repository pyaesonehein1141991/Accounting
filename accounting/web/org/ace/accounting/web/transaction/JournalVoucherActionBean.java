package org.ace.accounting.web.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.ace.accounting.common.validation.MessageId;
import org.ace.accounting.dto.JVdto;
import org.ace.accounting.process.interfaces.IUserProcessService;
import org.ace.accounting.system.chartaccount.CurrencyChartOfAccount;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.currency.service.interfaces.ICurrencyService;
import org.ace.accounting.system.rateinfo.RateInfo;
import org.ace.accounting.system.rateinfo.service.interfaces.IRateInfoService;
import org.ace.accounting.system.tlf.service.interfaces.ITLFService;
import org.ace.accounting.system.trantype.TranCode;
import org.ace.java.component.SystemException;
import org.ace.java.web.common.BaseBean;
import org.ace.java.web.common.ParamId;
import org.primefaces.event.SelectEvent;

@ManagedBean(name = "JournalVoucherActionBean")
@ViewScoped
public class JournalVoucherActionBean extends BaseBean {

	@ManagedProperty(value = "#{CurrencyService}")
	private ICurrencyService currencyService;

	public void setCurrencyService(ICurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@ManagedProperty(value = "#{TLFService}")
	private ITLFService tlfService;

	public void setTlfService(ITLFService tlfService) {
		this.tlfService = tlfService;
	}

	@ManagedProperty(value = "#{RateInfoService}")
	private IRateInfoService rateInfoService;

	public void setRateInfoService(IRateInfoService rateInfoService) {
		this.rateInfoService = rateInfoService;
	}
	
	@ManagedProperty(value = "#{UserProcessService}")
	private IUserProcessService userProcessService;

	public void setUserProcessService(IUserProcessService userProcessService) {
		this.userProcessService = userProcessService;
	}


	private boolean createNew;
	private JVdto dto;
	private List<JVdto> dtoList;
	private Currency cur;
	private BigDecimal exchangeRate;
	private int dtoIndex;
	private boolean admin;
	private Date todayDate;

	@PostConstruct
	public void init() {
		rebindData();
		createNewDto();
	}

	@PreDestroy
	public void destroy() {
		removeParam(ParamId.COA_DATA);
	}

	private void rebindData() {
		dtoList = new ArrayList<JVdto>();
	}

	public void prepareUpdateDto(JVdto dto) {
		dtoIndex = dtoList.indexOf(dto);
		this.dto = dto;
		createNew = false;
	}

	public void createNewDto() {
		createNew = true;
		dto = new JVdto();
		if (cur != null) {
			dto.setCur(cur);
			dto.setExchangeRate(exchangeRate);
		} else {
			dto.setCur(currencyService.findHomeCurrency());
			exchangeRate = BigDecimal.ONE;
			dto.setExchangeRate(exchangeRate);
		}
		todayDate= new Date();
		
		admin= userProcessService.getLoginUser().isAdmin();
		if(null == dto.getSettlementDate()) {
			dto.setSettlementDate(new Date());
		}
		
	}

	public void selectCurrency() {
		if (cur.getIsHomeCur()) {
			exchangeRate = new BigDecimal(1);
		} else {
			RateInfo currentRate = rateInfoService.findCurrentRateInfo(cur);
			if (currentRate != null) {
				exchangeRate = currentRate.getExchangeRate();
			} else {
				String code = cur.getCurrencyCode();
				this.cur = null;
				addErrorMessage(null, MessageId.NO_EXCHANGE_RATE, code);
			}
		}
		dto.setCur(cur);
		dto.setExchangeRate(exchangeRate);
		dto.setCcoa(null);
	}

	public void saveDtos() {
		if (validateDtoList()) {
			try {
				String voucherNo = tlfService.addNewTlfByDto(dtoList);
				addInfoMessage(null, MessageId.INSERT_SUCCESS, voucherNo);
				createNewDto();
				rebindData();
			} catch (SystemException ex) {
				handleSysException(ex);
			}
		}
	}

	public void addNewDto() {
		try {
			dto.setHomeAmount(dto.getAmount().multiply(exchangeRate));
			dtoList.add(dto);
			createNewDto();
		} catch (SystemException ex) {
			handleSysException(ex);
		}
	}

	public void updateDto() {
		try {
			dto.setHomeAmount(dto.getAmount().multiply(exchangeRate));
			dtoList.set(dtoIndex, dto);
			createNewDto();
		} catch (SystemException ex) {
			handleSysException(ex);
		}
	}

	public String deleteDto(JVdto dto) {
		try {
			dtoList.remove(dto);
			createNewDto();
		} catch (SystemException ex) {
			handleSysException(ex);
		}
		return null;
	}

	private boolean validateDtoList() {
		double totalDebit = 0;
		double totalCredit = 0;
		if (cur != null) {
			for (JVdto dto : dtoList) {
				if (dto.getTranCode().equals(TranCode.TRCREDIT)) {
					totalCredit += dto.getAmount().doubleValue();
				} else if (dto.getTranCode().equals(TranCode.TRDEBIT)) {
					totalDebit += dto.getAmount().doubleValue();
				}
			}
			if (totalCredit == totalDebit && dtoList.stream().allMatch(dto->dtoList.get(0).getSettlementDate().equals(dto.getSettlementDate()))) {
				return true;
			}
		}
		addErrorMessage(null, MessageId.AMOUNT_DATE_INBALANCE);
		return false;
	}

	public void setCreateNew(boolean createNew) {
		this.createNew = createNew;
	}

	public boolean isCreateNew() {
		return createNew;
	}

	public List<Currency> getCurs() {
		return currencyService.findAllCurrency();
	}

	public void returnCcoa(SelectEvent event) {
		CurrencyChartOfAccount ccoa = (CurrencyChartOfAccount) event.getObject();
		dto.setCcoa(ccoa);
	}

	public JVdto getDto() {
		return dto;
	}

	public void setDto(JVdto dto) {
		this.dto = dto;
	}

	public List<JVdto> getDtoList() {
		return dtoList;
	}

	public void setDtoList(List<JVdto> dtoList) {
		this.dtoList = dtoList;
	}

	public List<TranCode> getCodes() {
		List<TranCode> list = new ArrayList<TranCode>();
		list.add(TranCode.TRCREDIT);
		list.add(TranCode.TRDEBIT);
		return list;
	}

	public Currency getCur() {
		return cur;
	}

	public void setCur(Currency cur) {
		this.cur = cur;
	}

	public List<JVdto> filteredList;

	public List<JVdto> getFilteredList() {
		return filteredList;
	}

	public void setFilteredList(List<JVdto> filteredList) {
		this.filteredList = filteredList;
	}

	public void selectCCOAAccountCode() {
		selectCCOAAccountCode(dto.getCur());
	}

	public boolean isCurDisabled() {
		return dtoList.size() != 0 ? true : false;
	}

	public boolean isAdmin() {
		return admin;
	}

	public Date getTodayDate() {
		return todayDate;
	}
	
}
