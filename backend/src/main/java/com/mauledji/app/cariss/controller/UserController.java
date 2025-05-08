package com.mauledji.app.cariss.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauledji.app.cariss.model.User;
import com.mauledji.app.cariss.service.UserService;

@RestController // Marca la clase como un controlador REST. Devuelve objetos directamente (no vistas HTML).
@RequestMapping("/api/v1/users") // Prefijo para todas las rutas de esta clase: /api/v1/users
public class UserController {
    
    @Autowired // Inyecta automáticamente una instancia del servicio de usuarios
    private UserService userService;

    @GetMapping // Maneja peticiones GET a /api/v1/users
    public List<User> getAllUsers() {
        return userService.getAllUsers(); // Devuelve todos los usuarios como lista JSON
    }

    @GetMapping("/{userId}") // Maneja GET a /api/v1/users/{id}
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        // Busca el usuario por ID. Si existe, lo devuelve con estado 200 OK. Si no, 404 Not Found.
        return userService.getUserById(userId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping // Maneja POST a /api/v1/users
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Verifica si el email ya existe. Si existe, responde 400 Bad Request
        if (userService.existsByEmail(user.getUserEmail())) {
            return ResponseEntity.badRequest().body(null);
        }
        // Si es válido, crea el usuario y responde con 200 OK y el usuario creado
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{userId}") // Maneja PUT a /api/v1/users/{id}
    public ResponseEntity<User> updateUser(@PathVariable Integer userId, @RequestBody User user) {
        // Busca si el usuario existe por ID
        return userService.getUserById(userId).<ResponseEntity<User>>map(existingUser -> {
            // Actualiza los campos del usuario existente
            existingUser.setUsername(user.getUsername());
            existingUser.setUserFullName(user.getUserFullName());
            existingUser.setUserEmail(user.getUserEmail());
            existingUser.setUserPassword(user.getUserPassword());

            // Guarda y devuelve el usuario actualizado
            return ResponseEntity.ok(userService.updateUser(existingUser, user));
        }).orElseGet(() -> ResponseEntity.notFound().build()); // Si no existe, responde 404
    }

    @DeleteMapping("/{userId}") // Maneja DELETE a /api/v1/users/{id}
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId); // Elimina el usuario
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content (sin cuerpo)
    }

}
