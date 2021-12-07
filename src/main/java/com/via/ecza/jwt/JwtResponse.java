package com.via.ecza.jwt;

import java.io.Serializable;

import com.via.ecza.entity.enumClass.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse  implements Serializable{
	private Long userId;
	private String username;
	private String jwttoken;
	private String email;
	private Role role;
}
