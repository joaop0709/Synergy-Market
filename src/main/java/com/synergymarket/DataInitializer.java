package com.synergymarket.config;

import com.synergymarket.entity.Usuario;
import com.synergymarket.enums.PerfilUsuario;
import com.synergymarket.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .senha(passwordEncoder.encode("admin123"))
                    .perfil(PerfilUsuario.ADMIN)
                    .build();
            usuarioRepository.save(admin);
            System.out.println(">>> Usuário admin criado com sucesso. Senha: admin123");
        }

        if (!usuarioRepository.existsByUsername("funcionario")) {
            Usuario func = Usuario.builder()
                    .username("funcionario")
                    .senha(passwordEncoder.encode("func123"))
                    .perfil(PerfilUsuario.FUNCIONARIO)
                    .build();
            usuarioRepository.save(func);
            System.out.println(">>> Usuário funcionario criado com sucesso. Senha: func123");
        }
    }
}
