package com.inspection.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inspection.backend.model.InspectionRecord;
import com.inspection.backend.model.InspectionRecordEntity;
import com.inspection.backend.repository.InspectionRecordRepository;

@Service
public class InspectionRecordService {

    private final InspectionRecordRepository repository;

    public InspectionRecordService(InspectionRecordRepository repository) {
        this.repository = repository;
    }

    public void saveAll(List<InspectionRecord> records) {

        for (InspectionRecord record : records) {

            boolean alreadySaved =
                    repository.existsByInspectionDateAndPartNoAndSheetNameAndExcelRowNumber(
                            record.getInspectionDate(),
                            record.getPartNo(),
                            record.getSheetName(),
                            record.getExcelRowNumber());

            if (!alreadySaved) {
                repository.save(toEntity(record));
            }
        }
    }

    public List<InspectionRecordEntity> getAll() {
        return repository.findAll();
    }

    public List<InspectionRecord> getAllAsInspectionRecords() {

        List<InspectionRecord> records = new ArrayList<>();

        for (InspectionRecordEntity entity : repository.findAll()) {

            InspectionRecord record = new InspectionRecord();

            record.setSrNo(entity.getSrNo());
            record.setInspectionDate(entity.getInspectionDate());
            record.setPartNo(entity.getPartNo());
            record.setPartName(entity.getPartName());
            record.setVendorCode(entity.getVendorCode());
            record.setVendorName(entity.getVendorName());
            record.setModel(entity.getModel());
            record.setQuantityChecked(entity.getQuantityChecked());
            record.setOkQuantity(entity.getOkQuantity());
            record.setNgQuantity(entity.getNgQuantity());
            record.setInspectionStatus(entity.getInspectionStatus());
            record.setDefectDescription(entity.getDefectDescription());
            record.setDefectPhoto(entity.getDefectPhoto());
            record.setPackagingStatus(entity.getPackagingStatus());
            record.setPackagingPhoto(entity.getPackagingPhoto());
            record.setCheckedBy(entity.getCheckedBy());
            record.setVerifiedBy(entity.getVerifiedBy());
            record.setRemarks(entity.getRemarks());
            record.setSheetName(entity.getSheetName());
            record.setExcelRowNumber(entity.getExcelRowNumber());

            records.add(record);
        }

        return records;
    }

    private InspectionRecordEntity toEntity(InspectionRecord record) {

        InspectionRecordEntity entity = new InspectionRecordEntity();

        entity.setSrNo(record.getSrNo());
        entity.setInspectionDate(record.getInspectionDate());
        entity.setPartNo(record.getPartNo());
        entity.setPartName(record.getPartName());
        entity.setVendorCode(record.getVendorCode());
        entity.setVendorName(record.getVendorName());
        entity.setModel(record.getModel());
        entity.setQuantityChecked(record.getQuantityChecked());
        entity.setOkQuantity(record.getOkQuantity());
        entity.setNgQuantity(record.getNgQuantity());
        entity.setInspectionStatus(record.getInspectionStatus());
        entity.setDefectDescription(record.getDefectDescription());
        entity.setDefectPhoto(record.getDefectPhoto());
        entity.setPackagingStatus(record.getPackagingStatus());
        entity.setPackagingPhoto(record.getPackagingPhoto());
        entity.setCheckedBy(record.getCheckedBy());
        entity.setVerifiedBy(record.getVerifiedBy());
        entity.setRemarks(record.getRemarks());
        entity.setSheetName(record.getSheetName());
        entity.setExcelRowNumber(record.getExcelRowNumber());

        return entity;
    }
}
