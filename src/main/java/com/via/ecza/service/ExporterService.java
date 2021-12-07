package com.via.ecza.service;


import com.via.ecza.dto.UserDto;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.entity.User;
import com.via.ecza.repo.UserRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ExporterService {
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper mapper;

    public List<UserDto> getAllUsersWithExporter(String authHeader) throws NotFoundException {
        String usernameFromControl = controlService.getUsernameFromToken(authHeader);
        Optional<User> opt = userRepository.findByUsername(usernameFromControl);
        if(!opt.isPresent()) {
            throw new NotFoundException("Böyle Bir Kullanıcı Yoktur");
        }
        List<User> list = userRepository.findByRole(Role.EXPORTER);
        UserDto[] dtoList = mapper.map(list, UserDto[].class);
        return Arrays.asList(dtoList);
    }
}
