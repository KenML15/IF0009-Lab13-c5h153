package com.medpharm.config;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.medpharm.model.Usuario;
import com.medpharm.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordInitializer implements CommandLineRunner {

    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private static final String PASSWORD_PRUEBA = "password123";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<Usuario> invalidos = usuarioRepository.findAll().stream()
                .filter(u -> !BCRYPT.matcher(u.getPassword()).matches())
                .toList();

        invalidos.forEach(u -> {
            u.setPassword(passwordEncoder.encode(PASSWORD_PRUEBA));
            log.info("Hash BCrypt inválido corregido para el usuario '{}'", u.getUsername());
        });

        usuarioRepository.saveAll(invalidos);
    }
}