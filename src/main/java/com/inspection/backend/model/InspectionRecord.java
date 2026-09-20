package com.inspection.backend.model;

import java.time.LocalDate;

public class InspectionRecord {

    private Integer srNo;
    private LocalDate inspectionDate;
    private String partNo;
    private String partName;
    private String vendorCode;
    private String vendorName;
    private String model;
    private Integer quantityChecked;
    private Integer okQuantity;
    private Integer ngQuantity;
    private String inspectionStatus;
    private String defectDescription;
    private String defectPhoto;
    private String packagingStatus;
    private String packagingPhoto;
    private String checkedBy;
    private String verifiedBy;
    private String remarks;

    // Source information
    private String sheetName;
    private Integer excelRowNumber;


    public Integer getSrNo() {
        return srNo;
    }

    public void setSrNo(Integer srNo) {
        this.srNo = srNo;
    }


    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
    }


    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }


    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }


    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }


    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public Integer getQuantityChecked() {
        return quantityChecked;
    }

    public void setQuantityChecked(Integer quantityChecked) {
        this.quantityChecked = quantityChecked;
    }


    public Integer getOkQuantity() {
        return okQuantity;
    }

    public void setOkQuantity(Integer okQuantity) {
        this.okQuantity = okQuantity;
    }


    public Integer getNgQuantity() {
        return ngQuantity;
    }

    public void setNgQuantity(Integer ngQuantity) {
        this.ngQuantity = ngQuantity;
    }


    public String getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }


    public String getDefectDescription() {
        return defectDescription;
    }

    public void setDefectDescription(String defectDescription) {
        this.defectDescription = defectDescription;
    }


    public String getDefectPhoto() {
        return defectPhoto;
    }

    public void setDefectPhoto(String defectPhoto) {
        this.defectPhoto = defectPhoto;
    }


    public String getPackagingStatus() {
        return packagingStatus;
    }

    public void setPackagingStatus(String packagingStatus) {
        this.packagingStatus = packagingStatus;
    }


    public String getPackagingPhoto() {
        return packagingPhoto;
    }

    public void setPackagingPhoto(String packagingPhoto) {
        this.packagingPhoto = packagingPhoto;
    }


    public String getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(String checkedBy) {
        this.checkedBy = checkedBy;
    }


    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }


    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }


    public Integer getExcelRowNumber() {
        return excelRowNumber;
    }

    public void setExcelRowNumber(Integer excelRowNumber) {
        this.excelRowNumber = excelRowNumber;
    }
}