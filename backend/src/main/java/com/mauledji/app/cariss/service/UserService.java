package com.mauledji.app.cariss.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Permite inyectar dependencias automáticamente.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Para encriptar contraseñas.
import org.springframework.stereotype.Service; // Marca esta clase como un servicio (componente de lógica de negocio).

import com.mauledji.app.cariss.model.User;
import com.mauledji.app.cariss.repository.UserRepo;

@Service // Anotación que indica que esta clase es un servicio de Spring, y puede ser inyectada en otras clases.
public class UserService {

    @Autowired // Inyecta automáticamente una instancia del repositorio para acceder a la base de datos.
    private UserRepo userRepo;

    @Autowired // Inyecta el codificador para encriptar contraseñas.
    private BCryptPasswordEncoder passwordEncoder;

    // Retorna una lista con todos los usuarios en la base de datos.
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Busca un usuario por su ID y retorna un Optional<User>.
    public Optional<User> getUserById(Integer userId) {
        return userRepo.findById(userId);
    }

    // Crea un nuevo usuario, encriptando su contraseña antes de guardarlo.
    public User createUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword())); // Encriptar contraseña
        return userRepo.save(user); // Guardar usuario
    }

    // Actualiza un usuario existente con los nuevos datos proporcionados.
    public User updateUser(User existingUser, User newUser) {
        existingUser.setUsername(newUser.getUsername());
        existingUser.setUserFullName(newUser.getUserFullName());
        existingUser.setUserEmail(newUser.getUserEmail());

        // Solo actualiza la contraseña si viene una nueva
        if (newUser.getUserPassword() != null && !newUser.getUserPassword().isBlank()) {
            existingUser.setUserPassword(passwordEncoder.encode(newUser.getUserPassword()));
        }

        return userRepo.save(existingUser); // Guarda los cambios
    }

    // Elimina un usuario por ID.
    public void deleteUser(Integer userId) {
        userRepo.deleteById(userId);
    }

    // Verifica si un usuario ya existe en la base de datos por su email.
    public boolean existsByEmail(String userEmail) {
        return userRepo.existsByUserEmail(userEmail);
    }
}
