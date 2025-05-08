package com.mauledji.app.cariss.util;

import java.util.regex.Pattern;

// Clase utilitaria para validar contraseñas según criterios de seguridad
public class PasswordValidator {

    // Longitud mínima de la contraseña
    private static final int MIN_LENGTH = 8;

    // Expresiones regulares para verificar distintos tipos de caracteres
    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");             // Al menos una mayúscula
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");             // Al menos una minúscula
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");              // Al menos un número
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]"); // Al menos un carácter especial

    /**
     * Valida si una contraseña cumple con los criterios de seguridad:
     * - Longitud mínima
     * - Contiene mayúscula, minúscula, número y símbolo
     * 
     * @param password La contraseña a validar
     * @return true si es válida, false si no cumple los criterios
     */
    public static boolean isValid(String password) {
        // Si es nula o más corta que el mínimo, la rechazamos
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }

        // Retorna true solo si cumple con todos los requisitos
        return HAS_UPPER.matcher(password).find() &&
               HAS_LOWER.matcher(password).find() &&
               HAS_NUMBER.matcher(password).find() &&
               HAS_SPECIAL.matcher(password).find();
    }

    /**
     * Devuelve el mensaje de validación para mostrar al usuario en caso de contraseña inválida.
     *
     * @return Mensaje con los requisitos mínimos
     */
    public static String getValidationMessage() {
        return "Password must be at least 8 characters long and contain at least one uppercase letter, " +
               "one lowercase letter, one number, and one special character.";
    }
}
