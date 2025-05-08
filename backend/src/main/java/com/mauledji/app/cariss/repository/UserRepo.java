package com.mauledji.app.cariss.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base de Spring Data JPA para acceso a datos.
import org.springframework.stereotype.Repository; // Marca esta interfaz como un componente de repositorio para que Spring lo registre.

import com.mauledji.app.cariss.model.User;

@Repository // Anotación que indica que esta interfaz es un repositorio de acceso a base de datos.
public interface UserRepo extends JpaRepository<User, Integer> {
    // Extiende JpaRepository, lo que nos da acceso a métodos CRUD sin tener que implementarlos.
    // El primer parámetro es la entidad (User), y el segundo es el tipo de su clave primaria (Integer).

    // Busca un usuario por su nombre de usuario (username).
    Optional<User> findByUsername(String username);

    // Busca un usuario por su correo electrónico.
    Optional<User> findByUserEmail(String userEmail);

    // Verifica si ya existe un usuario con ese nombre de usuario.
    boolean existsByUsername(String username);

    // Verifica si ya existe un usuario con ese correo electrónico.
    boolean existsByUserEmail(String userEmail);
}
