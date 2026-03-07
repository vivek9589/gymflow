package com.gymflow.gymflow.gym.service.impl;

import com.gymflow.gymflow.common.exception.BusinessException;
import com.gymflow.gymflow.gym.dto.GymDTO;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.gym.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;

    @Override
    public GymDTO getGymById(Long id) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gym not found with ID: " + id));
        return mapToDTO(gym);
    }

    @Override
    public GymDTO getGymByCode(String code) {
        Gym gym = gymRepository.findByGymCode(code)
                .orElseThrow(() -> new BusinessException("Invalid Gym Code"));
        return mapToDTO(gym);
    }

    @Override
    @Transactional
    public GymDTO updateGym(Long id, GymDTO dto) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gym not found"));

        gym.setName(dto.getName());
        gym.setAddress(dto.getAddress());
        gym.setContactNumber(dto.getContactNumber());

        return mapToDTO(gymRepository.save(gym));
    }

    private GymDTO mapToDTO(Gym gym) {
        GymDTO dto = new GymDTO();
        dto.setId(gym.getId());
        dto.setName(gym.getName());
        dto.setGymCode(gym.getGymCode());
        dto.setAddress(gym.getAddress());
        dto.setContactNumber(gym.getContactNumber());
        return dto;
    }

    @Override
    public List<GymDTO> getAllGyms() {
        return gymRepository.findAll().stream().map(this::mapToDTO).toList();
    }
}