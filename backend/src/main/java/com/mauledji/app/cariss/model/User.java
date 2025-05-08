package com.mauledji.app.cariss.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor; 
import lombok.Builder;            
import lombok.Getter;             
import lombok.NoArgsConstructor;  
import lombok.Setter;             

@Getter
@Setter
@Table(name = "user")             // Indica que esta entidad se mapea a una tabla llamada "user" en la base de datos.
@Entity                           // Marca esta clase como una entidad JPA (tabla en la base de datos).
@NoArgsConstructor                // Constructor vacío requerido por JPA.
@AllArgsConstructor               // Constructor con todos los argumentos.
@Builder                          // Permite construir objetos con el patrón builder.
public class User implements Serializable {

    @Id // Define el campo como clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El valor se autogenera con auto_increment (MySQL u otras BD).
    private Integer userId;

    @Column(nullable = false, unique = true) // No puede ser nulo y debe ser único en la tabla.
    private String username;

    @Column(nullable = false) // Obligatorio.
    private String userFullName;

    @Column(nullable = false, unique = true) // No puede ser nulo y no se puede repetir (clave única).
    private String userEmail;

    @Column(nullable = false) // No puede estar vacío. Se almacena encriptada.
    private String userPassword;
}
