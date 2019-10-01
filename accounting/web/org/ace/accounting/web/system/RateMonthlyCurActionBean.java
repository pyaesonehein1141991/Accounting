package org.ace.accounting.web.system;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.ace.accounting.common.validation.MessageId;
import org.ace.accounting.dto.MonthlyRateDto;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.currency.service.interfaces.ICurrencyService;
import org.ace.java.component.SystemException;
import org.ace.java.component.service.interfaces.IDataRepService;
import org.ace.java.web.common.BaseBean;

@ManagedBean(name = "RateMonthlyCurActionBean")
@ViewScoped
public class RateMonthlyCurActionBean extends BaseBean {

	@ManagedProperty(value = "#{CurrencyService}")
	private ICurrencyService currencyService;

	public void setCurrencyService(ICurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@ManagedProperty(value = "#{DataRepService}")
	private IDataRepService<Currency> dataRepService;

	public void setDataRepService(IDataRepService<Currency> dataRepService) {
		this.dataRepService = dataRepService;
	}

	private MonthlyRateDto currency;
	private List<MonthlyRateDto> currencyList;

	@PostConstruct
	public void init() {
		rebindData();
	}

	public void createNew() {
		rebindData();
	}

	private void rebindData() {
		currencyList = currencyService.findForeignCurrencyDto();
	}

	public void deleteCurrency(MonthlyRateDto currency) {
		currency.setAllZero();
	}

	public void deleteAllCurrency() {
		for (MonthlyRateDto cur : currencyList) {
			cur.setAllZero();
		}
	}

	public void saveMonthlyCurrencyRate() {
		try {
			currencyService.updateAllMonthlyRate(currencyList);
			addInfoMessage(null, MessageId.UPDATE_SUCCESS, "Rate Monthly Currency");
			rebindData();
		} catch (SystemException ex) {
			handleSysException(ex);
		}
	}

	public void setCurrency(MonthlyRateDto currency) {
		this.currency = currency;
	}

	public MonthlyRateDto getCurrency() {
		return currency;
	}

	public List<MonthlyRateDto> getCurrencyList() {
		return currencyList;
	}

}
