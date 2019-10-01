package org.ace.accounting.common;

public class TableName {

	/**
	 * Admin Management Table Name
	 */
	public static final String USER = "ACCUSERS";

	/**
	 * Permission management Table Name
	 */
	public static final String WEBPAGE = "ACCWEBPAGE";
	public static final String ROLE = "ACCROLE";
	public static final String USER_ROLE = "ACCUSERROLE";
	public static final String ROLE_WEBPAGE = "ACCROLEWEBPAGE";

	/**
	 * Branch Table Name
	 */
	public static final String BRANCH = "BRANCH";

	/**
	 * Chart Of Account Table Name
	 */
	public static final String COA = "COA";

	/**
	 * Currency Chart of Account Table Name
	 */
	public static final String CCOA = "CCOA";

	/**
	 * Previous Currency Chart Of Account History Table Name
	 */
	public static final String CCOA_HISTORY = "PREV_CCOA";

	/**
	 * Currency Table Name
	 */
	public static final String CUR = "CUR";

	/**
	 * Department Table Name
	 */
	public static final String DEPARTMENT = "DEPTCODE";

	/**
	 * Allocation table Name for viewing department profit and loss
	 */
	public static final String ALLOCATECODE = "ALLOCATE_CODE";

	/**
	 * COA Setup Table Name
	 */
	public static final String COASETUP = "COASETUP";

	/**
	 * Format file table Name for Report Format Entry.
	 */
	public static final String FORMATFILE = "FORMATFILE";

	/**
	 * Rate Info table Name
	 */
	public static final String RATEINFO = "RATEINFO";

	/**
	 * Voucher Transaction Type Table Name
	 */
	public static final String TRANTYPE = "TRANTYPE";

	/**
	 * TLF Table Name
	 */
	public static final String TLF = "TLF";

	/**
	 * TLF History Table Name
	 */
	public static final String TLFHIST = "TLF_HIST";

	/**
	 * System Post Table Name
	 */
	public static final String SYSTEMPOST = "SYSPOST";

	/**
	 * Setup Table Name
	 */
	public static final String SETUP = "SETUP";

	/**
	 * Clean Cash View Table Name
	 */
	public static final String CLEANCASHVIEW = "VWT_CLEANCASH";

	/**
	 * View table entity for bank cash scroll
	 */
	public static final String VW_BANKCASH = "VW_BANKCASH";

	/**
	 * View table entity for ccoa and prev_ccoa union all
	 */
	public static final String VW_CCOA = "VW_CCOA";

	/**
	 * View table entity for tlf and tlfhist union all
	 */
	public static final String VW_TLF = "VW_TLF";
}
