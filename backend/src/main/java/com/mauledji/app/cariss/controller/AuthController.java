package com.mauledji.app.cariss.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauledji.app.cariss.dto.LoginRequest;
import com.mauledji.app.cariss.dto.RegisterRequest;
import com.mauledji.app.cariss.model.User;
import com.mauledji.app.cariss.repository.UserRepo;
import com.mauledji.app.cariss.security.JwtUtil;
import com.mauledji.app.cariss.util.PasswordValidator;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier dominio (ideal para pruebas o apps separadas en frontend)
@RestController // Indica que esta clase es un controlador REST que devuelve objetos JSON
@RequestMapping("/api/auth") // Prefijo para todas las rutas de esta clase (ej: /api/auth/login)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // Logger para imprimir eventos en consola

    private static final int MAX_LOGIN_ATTEMPTS = 5; // Límite de intentos de inicio de sesión fallidos
    private final Map<String, Integer> loginAttempts = new HashMap<>(); // Mapa para llevar el conteo de intentos por usuario

    @Autowired
    private UserRepo userRepo; // Acceso a base de datos de usuarios

    @Autowired
    private JwtUtil jwtUtil; // Herramienta para generar y validar tokens JWT

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Encriptador de contraseñas

    @PostMapping("/login") // Ruta POST: /api/auth/login
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest, // Datos enviados por el cliente (username/email + password)
            @RequestHeader(value = "X-Forwarded-For", required = false) String clientIp, // IP del cliente (opcional)
            HttpServletRequest request // Alternativa si la IP no viene en header
    ) {

        if (clientIp == null) {
            clientIp = request.getRemoteAddr(); // Obtiene IP directamente del request
        }

        String input = loginRequest.getUsernameOrEmail();
        String password = loginRequest.getPassword();

        // Si el usuario excedió los intentos permitidos, se bloquea temporalmente
        if (isAccountLocked(input)) {
            logger.warn("Account temporarily locked for user: {}", input);
            return ResponseEntity.status(429).body("Account temporarily locked. Please try again later.");
        }

        // Intenta buscar el usuario por username, luego por email si no lo encuentra
        Optional<User> userOptional = userRepo.findByUsername(input);
        if (userOptional.isEmpty()) {
            userOptional = userRepo.findByUserEmail(input);
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Compara la contraseña ingresada con la contraseña encriptada almacenada
            if (passwordEncoder.matches(password, user.getUserPassword())) {
                loginAttempts.remove(input); // Reinicia el contador de intentos

                // Genera token JWT para el usuario autenticado con su nombre completo
                String token = jwtUtil.generateToken(user.getUsername(), user.getUserFullName());

                // Respuesta que contiene el token y el username
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("username", user.getUsername());
                response.put("fullName", user.getUserFullName());

                logger.info("Successful login for user: {}", user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                incrementLoginAttempts(input); // Suma un intento fallido
                logger.warn("Failed login attempt for user: {}", input);
            }
        }

        // Si no pasa la validación, devuelve error 401
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register") // Ruta POST: /api/auth/register
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Verifica que la contraseña cumpla con los requisitos definidos en PasswordValidator
        if (!PasswordValidator.isValid(registerRequest.getUserPassword())) {
            return ResponseEntity.badRequest().body(PasswordValidator.getValidationMessage());
        }

        // Valida que no exista un usuario con el mismo username o email
        if (userRepo.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (userRepo.existsByUserEmail(registerRequest.getUserEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Crea y guarda el nuevo usuario
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setUserFullName(registerRequest.getUserFullName());
        user.setUserEmail(registerRequest.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(registerRequest.getUserPassword())); // Encripta la contraseña

        userRepo.save(user);
        logger.info("New user registered: {}", user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }

    // Verifica si un usuario superó el límite de intentos de login fallidos
    private boolean isAccountLocked(String username) {
        return loginAttempts.getOrDefault(username, 0) >= MAX_LOGIN_ATTEMPTS;
    }

    // Suma un intento fallido al contador del usuario
    private void incrementLoginAttempts(String username) {
        loginAttempts.merge(username, 1, Integer::sum);
    }
}
