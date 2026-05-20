package com.synergymarket.service;

import com.synergymarket.dto.LoginRequestDTO;
import com.synergymarket.dto.LoginResponseDTO;
import com.synergymarket.security.CustomUserDetailsService;
import com.synergymarket.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getSenha())
        );
        UserDetails user = userDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtService.gerarToken(user);
        String perfil = user.getAuthorities().iterator().next().getAuthority();

        return LoginResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .perfil(perfil)
                .build();
    }
}
