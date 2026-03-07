package com.gymflow.gymflow.gym.service;


import com.gymflow.gymflow.gym.dto.GymDTO;

import java.util.List;

public interface GymService {
    GymDTO getGymById(Long id);
    GymDTO getGymByCode(String code);
    GymDTO updateGym(Long id, GymDTO gymDTO);
    List<GymDTO> getAllGyms(); // For System Admin
}