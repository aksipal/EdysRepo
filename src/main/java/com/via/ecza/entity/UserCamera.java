package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="user_camera")
public class UserCamera {

    @Id
    @SequenceGenerator(name = "sq_user_camera", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_user_camera")
    @Column(name = "user_camera_id")
    private Long userCameraId;

    @Column(name="camera_type", nullable = false,unique = true)
    private int cameraType;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

    @Column(name = "created_at")
    private Date createdAt;

    private int status;

}
