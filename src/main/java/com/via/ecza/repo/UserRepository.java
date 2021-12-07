package com.via.ecza.repo;

import java.util.List;
import java.util.Optional;

import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface    UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);

    List<User> findByUsernameNot(String username, Sort sort);
    Page<User> findByUsernameNot(String username,  Pageable page);


//    String SQL1 = "select * from users u" +
//            "u.user_id =:userId";
//    @Query(value = SQL1, nativeQuery = true)
//    Optional<User> getUsers(Long id, @Param("userId") Long userId);

//    Optional<Role> getAllUsersWithExporter(String role);


}

