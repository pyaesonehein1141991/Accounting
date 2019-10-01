package org.ace.accounting.system.rateinfo.service.interfaces;

import java.util.List;

import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.system.rateinfo.RateInfo;

public interface IRateInfoService {
	public List<RateInfo> findAllRateInfo();

	public void addNewDailyRateInfo(RateInfo rateInfo);

	public RateInfo findCurrentRateInfo(Currency cur);

	public List<RateInfo> findRateInfoByCurrencyID(String currencyID);
}
