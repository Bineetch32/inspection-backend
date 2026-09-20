package com.inspection.backend.service;

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

        List<InspectionRecordEntity> entities = records.stream()
                .map(this::toEntity)
                .toList();

        repository.saveAll(entities);
    }

    public List<InspectionRecordEntity> getAll() {
        return repository.findAll();
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
