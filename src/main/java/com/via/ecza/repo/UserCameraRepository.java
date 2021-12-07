package com.via.ecza.repo;

import com.via.ecza.entity.User;
import com.via.ecza.entity.UserCamera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCameraRepository extends JpaRepository<UserCamera, Long> {

    Optional<UserCamera> findByCameraType(int cameraType);
    Optional<UserCamera> findByUser(User user);
}
