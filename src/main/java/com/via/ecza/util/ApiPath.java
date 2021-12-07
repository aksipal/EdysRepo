package com.via.ecza.util;

public class ApiPath {
	private static final String BASE_PATH = "/api";
	private static final String USER_PATH = "/user";
	private static final String DRUG_PATH = "/drug";
	private static final String ADMIN_PATH = "/admin";
	private static final String COUNTRY_PATH = "/country";
	private static final String CUSTOMER_PATH = "/customer";
	private static final String COMPANY_PATH = "/company";
	private static final String MYACCOUNT_PATH = "/my-account";
	private static final String ORDER_PATH = "/order";
	private static final String SUPPLIER_PATH = "/supplier";
	private static final String SUPERVISOR_PATH = "/supervisor";
	private static final String DISCOUNT_PATH = "/discount";
	private static final String BOX_PATH = "/box";
	private static final String DEPOT_PATH = "/depot";
	private static final String STOCK_PATH = "/stock";
	private static final String SUPPLY_PATH = "/supply";
	private static final String REFUND_PATH = "/refund";
	private static final String ORDER_ACCEPTANCE_PATH = "/order-acceptance";
	private static final String PRICE_PATH = "/price";
	private static final String PACKAGING_PATH = "/packaging";
	private static final String PHARMACY_PATH="/pharmacy";
	private static final String STOCK_COUNTING_PATH="/stock-counting";
	private static final String SMALL_BOX_PATH="/small-box";
	private static final String COMMUNICATION_PATH="/communication";
	private static final String REFUND_ACCEPTANCE_PATH = "/refund-acceptance";
	private static final String ORDER_CHANGING_PATH = "/order-changing";
	private static final String CAMPAIGN_PATH="/campaign";
	private static final String SHIPPING_PATH="/shipping-adress";
	private static final String BANK_PATH="/bank";
	private static final String CHECKING_CARD_PATH="/checking-card";
	private static final String ACCOUNTING_CODE_PATH="/accounting-code";
	private static final String CATEGORY_PATH="/category";
	private static final String ACCOUNTING_PATH="/accounting";
	private static final String REPORT_PATH="/report";
	private static final String RECEIPT_PATH="/receipt";
	private static final String LOGISTIC_PATH = "/logistic";
	private static final String ACCOUNT_PATH = "/account";
	private static final String INVOICE_PATH = "/invoice";
	private static final String FINAL_RECEIPT_WAREHOUSEMAN_PATH="/final-receipt-warehouseman";
	private static final String FINAL_RECEIPT_ACCOUNTING_PATH="/final-receipt-accounting";
	private static final String FINAL_RECEIPT_ADMIN_PATH="/final-receipt-admin";
	private static final String ACTIVITY_PATH="/activity";
	private static final String CUSTOMER_INVOICE_PATH="/customer-invoice";
	private static final String EXPORTER_PATH="/exporter";
	private static final String REPORTING_PATH="/reporting";
	private static final String VERSION_INFORMATION_PATH="/version-information";
	private static final String OTHER_COMPANY_PATH="/other-company";

	public static final class ManagerCtrl {
		public static final String CTRL = BASE_PATH + "/manager";
	}
	public static final class MainCtrl {
		public static final String CTRL = BASE_PATH + "/main";
	}
	public static final class AdminCtrl {
		public static final String CTRL = BASE_PATH + ADMIN_PATH;
	}
	public static final class AdminUserCtrl {
		public static final String CTRL = BASE_PATH + ADMIN_PATH + USER_PATH;
	}
	public static final class CountryCtrl {
		public static final String CTRL = BASE_PATH  + COUNTRY_PATH;
	}
	public static final class AdminCustomerCtrl {
		public static final String CTRL = BASE_PATH + CUSTOMER_PATH;
	}
	public static final class AdminCompanyCtrl {
		public static final String CTRL = BASE_PATH  + COMPANY_PATH;
	}
	public static final class MyAccountCtrl {
		public static final String CTRL = BASE_PATH + MYACCOUNT_PATH   ;
	}
	public static final class UserCtrl {
		public static final String CTRL = BASE_PATH + "/user";
	}
	public static final class AdminDrugCtrl {
		public static final String CTRL = BASE_PATH  + DRUG_PATH;
	}
	public static final class AdminOrderCtrl {
		public static final String CTRL = BASE_PATH + ADMIN_PATH + ORDER_PATH;
	}
	public static final class AdminCustomerOrderCtrl {
		public static final String CTRL = BASE_PATH  + CUSTOMER_PATH + ORDER_PATH;
	}
	public static final class AdminSupplierCtrl {
		public static final String CTRL = BASE_PATH + SUPPLIER_PATH;
	}
	public static final class AdminSupplierSuperVisorCtrl {
		public static final String CTRL = BASE_PATH + SUPPLIER_PATH+ SUPERVISOR_PATH;
	}
	public static final class AdminSupplyOrderCtrl {
		public static final String CTRL = BASE_PATH + SUPPLY_PATH;
	}
	public static final class AdminDiscountCtrl {
		public static final String CTRL = BASE_PATH + DISCOUNT_PATH;
	}
	public static final class AdminDepotCtrl {
		public static final String CTRL = BASE_PATH + DEPOT_PATH;
	}
	public static final class AdminStockCtrl {
		public static final String CTRL = BASE_PATH + STOCK_PATH;
	}
	public static final class AdminCustomerOrderDrugCtrl {
		public static final String CTRL = BASE_PATH  + CUSTOMER_PATH + ORDER_PATH + DRUG_PATH;
	}
	public static final class AdminOrderAcceptanceCtrl {
		public static final String CTRL = BASE_PATH + ORDER_ACCEPTANCE_PATH;
	}
	public static final class AdminPriceCtrl {
		public static final String CTRL = BASE_PATH  + PRICE_PATH;
	}
	public static final class PharmacyCtrl {
		public static final String CTRL = BASE_PATH + PHARMACY_PATH;
	}
	public static final class  AdminRefundCtrl {
		public static final String CTRL = BASE_PATH + REFUND_PATH;
	}
	public static final class  AdminStockCountingCtrl {
		public static final String CTRL = BASE_PATH  + STOCK_COUNTING_PATH;
	}
	public static final class BoxCtrl {
		public static final String CTRL = BASE_PATH+ BOX_PATH;
	}
	public static final class SmallBoxCtrl {
		public static final String CTRL = BASE_PATH+ SMALL_BOX_PATH;
	}
	public static final class PackagingCtrl {
		public static final String CTRL = BASE_PATH+ PACKAGING_PATH;
	}
	public static final class CommunicationCtrl {
		public static final String CTRL = BASE_PATH+ COMMUNICATION_PATH;
	}
	public static final class AdminRefundAcceptanceCtrl {
		public static final String CTRL = BASE_PATH + REFUND_ACCEPTANCE_PATH;
	}
	public static final class OrderChangingCtrl {
		public static final String CTRL = BASE_PATH + ORDER_CHANGING_PATH;
	}
	public static final class CampaignCtrl {
		public static final String CTRL = BASE_PATH + CAMPAIGN_PATH;
	}
	public static final class CustomerOrderShippingAdressCtrl {
		public static final String CTRL = BASE_PATH + CUSTOMER_PATH + ORDER_PATH + SHIPPING_PATH;
	}
	public static final class CustomerOrderBankDetailCtrl {
		public static final String CTRL = BASE_PATH +CUSTOMER_PATH+ORDER_PATH+ BANK_PATH;
	}
	public static final class AccountingCodeCtrl {
		public static final String CTRL = BASE_PATH + ACCOUNTING_CODE_PATH;
	}
	public static final class CategoryCtrl {
		public static final String CTRL = BASE_PATH + CATEGORY_PATH;
	}
	public static final class AccountingReportCtrl {
		public static final String CTRL = BASE_PATH + ACCOUNTING_PATH+ REPORT_PATH;
	}
	public static final class CheckingCardCtrl {
		public static final String CTRL = BASE_PATH + CHECKING_CARD_PATH;
	}
	public static final class ReceiptCtrl {
		public static final String CTRL = BASE_PATH + RECEIPT_PATH;
	}
	public static final class AdminLogisticCtrl {
		public static final String CTRL = BASE_PATH  + LOGISTIC_PATH;
	}
	public static final class AccountCtrl {
		public static final String CTRL = BASE_PATH  + ACCOUNT_PATH;
	}
	public static final class InvoiceCtrl {
		public static final String CTRL = BASE_PATH  + INVOICE_PATH;
	}
	public static final class FinalReceiptWarehousemanCtrl {
		public static final String CTRL = BASE_PATH + FINAL_RECEIPT_WAREHOUSEMAN_PATH;
	}
	public static final class FinalReceiptAccoutingCtrl {
		public static final String CTRL = BASE_PATH + FINAL_RECEIPT_ACCOUNTING_PATH;
	}
	public static final class FinalReceiptAdminCtrl {
		public static final String CTRL = BASE_PATH + FINAL_RECEIPT_ADMIN_PATH;
	}
	public static final class InvoiceActivityCtrl {
		public static final String CTRL = BASE_PATH  + INVOICE_PATH + ACTIVITY_PATH;
	}
	public static final class CustomerInvoiceCtrl {
		public static final String CTRL = BASE_PATH + CUSTOMER_INVOICE_PATH;
	}
	public static final class CustomerOrderAccountingCtrl {
		public static final String CTRL = BASE_PATH + ACCOUNT_PATH + CUSTOMER_PATH + ORDER_PATH ;
	}
	public static final class AccountActivityCtrl {
		public static final String CTRL = BASE_PATH  + ACCOUNT_PATH + ACTIVITY_PATH;
	}
	public static final class ExporterCtrl {
		public static final String CTRL = BASE_PATH + EXPORTER_PATH;
	}
	public static final class AdminReportingCtrl {
		public static final String CTRL = BASE_PATH + REPORTING_PATH;
	}
	public static final class VersionInformationCtrl {
		public static final String CTRL = BASE_PATH + VERSION_INFORMATION_PATH;
	}
	public static final class OtherCompanyCtrl {
		public static final String CTRL = BASE_PATH + OTHER_COMPANY_PATH;
	}
}
