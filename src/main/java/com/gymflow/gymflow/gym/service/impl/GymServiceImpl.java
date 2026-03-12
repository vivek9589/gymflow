package com.gymflow.gymflow.gym.service.impl;

import com.gymflow.gymflow.common.exception.BusinessException;

import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.gym.dto.request.GymRequest;
import com.gymflow.gymflow.gym.dto.response.GymResponse;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.gym.service.GymService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;

    @Override
    public GymResponse getGymById(Long id) {
        log.info("Fetching gym by ID: {}", id);
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with ID: " + id));
        return mapToResponse(gym);
    }

    @Override
    public GymResponse getGymByCode(String code) {
        log.info("Fetching gym by code: {}", code);
        Gym gym = gymRepository.findByGymCode(code)
                .orElseThrow(() -> new GymNotFoundException("Invalid Gym Code: " + code));
        return mapToResponse(gym);
    }

    @Override
    @Transactional
    public GymResponse updateGym(Long id, GymRequest request) {
        log.info("Updating gym with ID: {}", id);
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with ID: " + id));

        gym.setName(request.getName());
        gym.setAddress(request.getAddress());
        gym.setContactNumber(request.getContactNumber());

        return mapToResponse(gymRepository.save(gym));
    }

    @Override
    public List<GymResponse> getAllGyms() {
        log.info("Fetching all gyms");
        return gymRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private GymResponse mapToResponse(Gym gym) {
        GymResponse response = new GymResponse();
        response.setId(gym.getId());
        response.setName(gym.getName());
        response.setGymCode(gym.getGymCode());
        response.setAddress(gym.getAddress());
        response.setContactNumber(gym.getContactNumber());
        return response;
    }
}
