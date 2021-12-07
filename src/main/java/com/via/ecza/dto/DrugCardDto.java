package com.via.ecza.dto;

import com.via.ecza.entity.SinglePriceDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import java.util.Date;

@Getter
@Setter
@Data
public class DrugCardDto {


    private Long drugCardId;
    @Lob
    private String drugName;
    private Long drugCode;
    private String atcCode;
    @Lob
    private String atcName;
    @Lob
    private String drugCompany;
    private String reference;
    private String priceProtectedUnprotected;
    private Integer priceDecReqCode;
    private Integer equivalentCode;
    private Integer referenceStatusCode;
    @Lob
    private String activeMatter;
    private String unitQuantity;
    private String unitType;
    private String packageQuantitySize;
    private String recipeType;
    private String importedManufactured;
    private String sourceCountry;
    private Date dateOfChange;
    private Date validityDate;
    private String typeOfChangeMadeInList;
    @Lob
    private String explanation;
    @Lob
    private String explanationOfAllTransactions;
    @Lob
    private String preDecisionFdkIncreases;
    @Lob
    private String afterDecisionFdkIncreases;
    private Integer itsInformation;
    private String companyGln;
    private String companyTaxNo;
    private Double drugVat;
    private int status;
    private DrugBoxPropertiesDto drugBoxProperties;
    private SinglePriceDto price;
    private SingleDiscountDto discount;
    private String sourceCountryForCalculation;
    private Boolean isActive;

    public DrugCardDto() {
    }

    public DrugCardDto( String drugName, Long drugCode, String atcCode, String atcName, String drugCompany, String reference, String priceProtectedUnprotected, Integer priceDecReqCode, Integer equivalentCode, Integer referenceStatusCode, String activeMatter, String unitQuantity, String unitType, String packageQuantitySize, String recipeType, String importedManufactured, String sourceCountry, Date dateOfChange, Date validityDate, String typeOfChangeMadeInList, String explanation, String explanationOfAllTransactions, String preDecisionFdkIncreases, String afterDecisionFdkIncreases, Integer itsInformation, String companyGln, String companyTaxNo, String sourceCountryForCalculation) {
        this.drugName = drugName;
        this.drugCode = drugCode;
        this.atcCode = atcCode;
        this.atcName = atcName;
        this.drugCompany = drugCompany;
        this.reference = reference;
        this.priceProtectedUnprotected = priceProtectedUnprotected;
        this.priceDecReqCode = priceDecReqCode;
        this.equivalentCode = equivalentCode;
        this.referenceStatusCode = referenceStatusCode;
        this.activeMatter = activeMatter;
        this.unitQuantity = unitQuantity;
        this.unitType = unitType;
        this.packageQuantitySize = packageQuantitySize;
        this.recipeType = recipeType;
        this.importedManufactured = importedManufactured;
        this.sourceCountry = sourceCountry;
        this.dateOfChange = dateOfChange;
        this.validityDate = validityDate;
        this.typeOfChangeMadeInList = typeOfChangeMadeInList;
        this.explanation = explanation;
        this.explanationOfAllTransactions = explanationOfAllTransactions;
        this.preDecisionFdkIncreases = preDecisionFdkIncreases;
        this.afterDecisionFdkIncreases = afterDecisionFdkIncreases;
        this.itsInformation = itsInformation;
        this.companyGln = companyGln;
        this.companyTaxNo = companyTaxNo;
        this.sourceCountryForCalculation = sourceCountryForCalculation;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrugCardDto)) return false;

        DrugCardDto that = (DrugCardDto) o;

        if (status != that.status) return false;
        if (drugCardId != null ? !drugCardId.equals(that.drugCardId) : that.drugCardId != null) return false;
        if (drugName != null ? !drugName.equals(that.drugName) : that.drugName != null) return false;
        if (drugCode != null ? !drugCode.equals(that.drugCode) : that.drugCode != null) return false;
        if (atcCode != null ? !atcCode.equals(that.atcCode) : that.atcCode != null) return false;
        if (atcName != null ? !atcName.equals(that.atcName) : that.atcName != null) return false;
        if (drugCompany != null ? !drugCompany.equals(that.drugCompany) : that.drugCompany != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (priceProtectedUnprotected != null ? !priceProtectedUnprotected.equals(that.priceProtectedUnprotected) : that.priceProtectedUnprotected != null)
            return false;
        if (priceDecReqCode != null ? !priceDecReqCode.equals(that.priceDecReqCode) : that.priceDecReqCode != null)
            return false;
        if (equivalentCode != null ? !equivalentCode.equals(that.equivalentCode) : that.equivalentCode != null)
            return false;
        if (referenceStatusCode != null ? !referenceStatusCode.equals(that.referenceStatusCode) : that.referenceStatusCode != null)
            return false;
        if (activeMatter != null ? !activeMatter.equals(that.activeMatter) : that.activeMatter != null) return false;
        if (unitQuantity != null ? !unitQuantity.equals(that.unitQuantity) : that.unitQuantity != null) return false;
        if (unitType != null ? !unitType.equals(that.unitType) : that.unitType != null) return false;
        if (packageQuantitySize != null ? !packageQuantitySize.equals(that.packageQuantitySize) : that.packageQuantitySize != null)
            return false;
        if (recipeType != null ? !recipeType.equals(that.recipeType) : that.recipeType != null) return false;
        if (importedManufactured != null ? !importedManufactured.equals(that.importedManufactured) : that.importedManufactured != null)
            return false;
        if (sourceCountry != null ? !sourceCountry.equals(that.sourceCountry) : that.sourceCountry != null)
            return false;
        if (dateOfChange != null ? !dateOfChange.equals(that.dateOfChange) : that.dateOfChange != null) return false;
        if (validityDate != null ? !validityDate.equals(that.validityDate) : that.validityDate != null) return false;
        if (typeOfChangeMadeInList != null ? !typeOfChangeMadeInList.equals(that.typeOfChangeMadeInList) : that.typeOfChangeMadeInList != null)
            return false;
        if (explanation != null ? !explanation.equals(that.explanation) : that.explanation != null) return false;
        if (explanationOfAllTransactions != null ? !explanationOfAllTransactions.equals(that.explanationOfAllTransactions) : that.explanationOfAllTransactions != null)
            return false;
        if (preDecisionFdkIncreases != null ? !preDecisionFdkIncreases.equals(that.preDecisionFdkIncreases) : that.preDecisionFdkIncreases != null)
            return false;
        if (afterDecisionFdkIncreases != null ? !afterDecisionFdkIncreases.equals(that.afterDecisionFdkIncreases) : that.afterDecisionFdkIncreases != null)
            return false;
        if (itsInformation != null ? !itsInformation.equals(that.itsInformation) : that.itsInformation != null)
            return false;
        if (companyGln != null ? !companyGln.equals(that.companyGln) : that.companyGln != null) return false;
        if (companyTaxNo != null ? !companyTaxNo.equals(that.companyTaxNo) : that.companyTaxNo != null) return false;
        if (drugVat != null ? !drugVat.equals(that.drugVat) : that.drugVat != null) return false;
        if (drugBoxProperties != null ? !drugBoxProperties.equals(that.drugBoxProperties) : that.drugBoxProperties != null)
            return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (discount != null ? !discount.equals(that.discount) : that.discount != null) return false;
        if (sourceCountryForCalculation != null ? !sourceCountryForCalculation.equals(that.sourceCountryForCalculation) : that.sourceCountryForCalculation != null)
            return false;
        return isActive != null ? isActive.equals(that.isActive) : that.isActive == null;
    }

    @Override
    public int hashCode() {
        int result = drugCardId != null ? drugCardId.hashCode() : 0;
        result = 31 * result + (drugName != null ? drugName.hashCode() : 0);
        result = 31 * result + (drugCode != null ? drugCode.hashCode() : 0);
        result = 31 * result + (atcCode != null ? atcCode.hashCode() : 0);
        result = 31 * result + (atcName != null ? atcName.hashCode() : 0);
        result = 31 * result + (drugCompany != null ? drugCompany.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + (priceProtectedUnprotected != null ? priceProtectedUnprotected.hashCode() : 0);
        result = 31 * result + (priceDecReqCode != null ? priceDecReqCode.hashCode() : 0);
        result = 31 * result + (equivalentCode != null ? equivalentCode.hashCode() : 0);
        result = 31 * result + (referenceStatusCode != null ? referenceStatusCode.hashCode() : 0);
        result = 31 * result + (activeMatter != null ? activeMatter.hashCode() : 0);
        result = 31 * result + (unitQuantity != null ? unitQuantity.hashCode() : 0);
        result = 31 * result + (unitType != null ? unitType.hashCode() : 0);
        result = 31 * result + (packageQuantitySize != null ? packageQuantitySize.hashCode() : 0);
        result = 31 * result + (recipeType != null ? recipeType.hashCode() : 0);
        result = 31 * result + (importedManufactured != null ? importedManufactured.hashCode() : 0);
        result = 31 * result + (sourceCountry != null ? sourceCountry.hashCode() : 0);
        result = 31 * result + (dateOfChange != null ? dateOfChange.hashCode() : 0);
        result = 31 * result + (validityDate != null ? validityDate.hashCode() : 0);
        result = 31 * result + (typeOfChangeMadeInList != null ? typeOfChangeMadeInList.hashCode() : 0);
        result = 31 * result + (explanation != null ? explanation.hashCode() : 0);
        result = 31 * result + (explanationOfAllTransactions != null ? explanationOfAllTransactions.hashCode() : 0);
        result = 31 * result + (preDecisionFdkIncreases != null ? preDecisionFdkIncreases.hashCode() : 0);
        result = 31 * result + (afterDecisionFdkIncreases != null ? afterDecisionFdkIncreases.hashCode() : 0);
        result = 31 * result + (itsInformation != null ? itsInformation.hashCode() : 0);
        result = 31 * result + (companyGln != null ? companyGln.hashCode() : 0);
        result = 31 * result + (companyTaxNo != null ? companyTaxNo.hashCode() : 0);
        result = 31 * result + (drugVat != null ? drugVat.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (drugBoxProperties != null ? drugBoxProperties.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (discount != null ? discount.hashCode() : 0);
        result = 31 * result + (sourceCountryForCalculation != null ? sourceCountryForCalculation.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        return result;
    }
}
