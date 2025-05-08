import { useNavigate } from 'react-router-dom';

// Obtiene el token JWT almacenado en el navegador (si existe)
export const getToken = () => {
    return localStorage.getItem("jwtToken");
};

// Función para realizar peticiones fetch autenticadas con el token JWT
export const authFetch = async (url, options = {}) => {
    const token = getToken();
    if (!token) throw new Error("No token found"); // Si no hay token, lanza un error

    // Retorna la llamada fetch con el token JWT incluido en el encabezado Authorization
    return fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            "Authorization": "Bearer " + token, // Formato estándar para JWT
            "Content-Type": "application/json"
        }
    });
};

// Decodifica el token JWT (payload) y lo convierte a objeto JSON
export const parseJwt = (token) => {
    try {
        const base64Payload = token.split('.')[1];  // Segunda parte del JWT (payload)
        const payload = atob(base64Payload);        // Decodifica Base64
        return JSON.parse(payload);                 // Convierte a objeto JS
    } catch (e) {
        return null; // Si falla, retorna null
    }
};

// Retorna el usuario actualmente logueado (extraído del payload del token)
export const getLoggedUser = () => {
    const token = getToken();
    if (!token) return null;
    const decoded = parseJwt(token);
    if (!decoded) return null;
    return {
        username: decoded.sub,
        fullName: decoded.fullName
    };
};

// Cierra sesión eliminando el token y el nombre de usuario del almacenamiento local
export const logout = () => {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("username");
    return true;
};

// Verifica si hay un token válido guardado
export const isAuthenticated = () => {
    const token = getToken();
    return !!token; // Retorna true si existe token, false si no
};

// Intenta iniciar sesión enviando usuario y contraseña al backend
export const login = async (usernameOrEmail, password) => {
    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ usernameOrEmail, password }) // Cuerpo de la solicitud
        });

        if (response.ok) {
            const responseData = await response.json();
            // Guarda el token y el nombre de usuario en localStorage
            localStorage.setItem("jwtToken", responseData.token);
            return { success: true }; // Indica éxito
        } else {
            const errorText = await response.text();
            return { success: false, error: errorText || "Login failed. Please try again." };
        }
    } catch (error) {
        console.error("Login error:", error);
        return { success: false, error: "An error occurred during login. Please try again later." };
    }
};

// Intenta registrar un nuevo usuario con los datos proporcionados
export const register = async (userData) => {
    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userData) // userData debe tener username, userEmail, etc.
        });

        if (response.ok) {
            return { success: true }; // Registro exitoso
        } else {
            const errorText = await response.text();
            return { success: false, error: errorText || "Registration failed. Please try again." };
        }
    } catch (err) {
        console.error("Registration error:", err);
        return { success: false, error: "An error occurred during registration. Please try again later." };
    }
};
