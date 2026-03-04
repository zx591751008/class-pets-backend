package com.classpets.backend.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.entity.AbandonedPet;
import com.classpets.backend.mapper.AbandonedPetMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AbandonedPetService {

    private final AbandonedPetMapper abandonedPetMapper;

    public AbandonedPetService(AbandonedPetMapper abandonedPetMapper) {
        this.abandonedPetMapper = abandonedPetMapper;
    }

    public boolean isPetAbandoned(Long studentId, String petId) {
        if (studentId == null || petId == null || petId.isEmpty()) {
            return false;
        }
        Long count = abandonedPetMapper.selectCount(new LambdaQueryWrapper<AbandonedPet>()
                .eq(AbandonedPet::getStudentId, studentId)
                .eq(AbandonedPet::getPetId, petId));
        return count != null && count > 0;
    }

    public List<String> getAbandonedPetIds(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        List<AbandonedPet> records = abandonedPetMapper.selectList(new LambdaQueryWrapper<AbandonedPet>()
                .eq(AbandonedPet::getStudentId, studentId));
        return records.stream()
                .map(AbandonedPet::getPetId)
                .filter(petId -> petId != null && !petId.isEmpty())
                .collect(Collectors.toList());
    }

    public void abandonPet(Long classId, Long studentId, String petId) {
        if (classId == null || studentId == null || petId == null || petId.isEmpty()) {
            return;
        }
        if (!isPetAbandoned(studentId, petId)) {
            AbandonedPet record = new AbandonedPet();
            record.setClassId(classId);
            record.setStudentId(studentId);
            record.setPetId(petId);
            record.setAbandonedAt(System.currentTimeMillis());
            abandonedPetMapper.insert(record);
        }
    }

    public void deleteByStudentId(Long studentId) {
        if (studentId == null) {
            return;
        }
        abandonedPetMapper.delete(new LambdaQueryWrapper<AbandonedPet>()
                .eq(AbandonedPet::getStudentId, studentId));
    }
}
