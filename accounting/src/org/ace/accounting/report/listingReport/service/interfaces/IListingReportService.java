package org.ace.accounting.report.listingReport.service.interfaces;

import java.util.Date;
import java.util.List;

import org.ace.accounting.dto.CashBookDTO;
import org.ace.accounting.dto.CoaListingDto;
import org.ace.accounting.dto.VLdto;
import org.ace.accounting.dto.VPFTdto;
import org.ace.accounting.dto.VPdto;
import org.ace.accounting.report.CashBookCriteria;
import org.ace.accounting.system.branch.Branch;
import org.ace.accounting.system.currency.Currency;
import org.ace.accounting.web.listing.VoucherListingType;

/**
 * This interface serves as the Service Layer to manipulate the
 * <code>Listing report DTO</code> object.
 * 
 * @author HS
 * @since 1.0.0
 * @date 2015/04/08
 */
public interface IListingReportService {
	public List<CoaListingDto> findAllCoaList();


	/**
	 * @param {@link
	 * 			CashBookCriteria} criteria
	 * @return {@link List} of {@link CashBookDTO} instance
	 * @throws SystemException
	 *             An Exception which occurs during the operation
	 */
	public List<CashBookDTO> findCashbookListingReport(CashBookCriteria criteria);

	public List<VPFTdto> findFromVoucherNo(Date startDate, Date endDate, Branch branch);

	public List<VPdto> findVoucherPrintingByVoucherNo(Date startDate, Date endDate, String fromId, String toId);

	public List<VPdto> findVoucherPrinting(Date startDate, Date endDate);



	List<VLdto> findVoucherListingReport(VoucherListingType type, Date startDate, Date endDate, Branch branch,
			Currency currency, Boolean isOnlyVoucher,boolean reverse);

}
