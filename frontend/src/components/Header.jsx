import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';
import { getLoggedUser, logout } from '../services/authService';
import { useNavigate } from 'react-router-dom';

export default function Header() {
    const username = getLoggedUser();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className={styles.header}>
            <div className={styles.logo}>
                <Link to="/home">🌀 Cariss</Link>
            </div>

            <nav className={styles.nav}>
                <Link to="">Example</Link>
                <Link to="">Example</Link>
                <Link to="">Example</Link>
            </nav>

            <div className={styles.userSection}>
                {username ? (
                <>
                    <span>{username.fullName}</span>
                    <button onClick={handleLogout}>Logout</button>
                </>
                ) : (
                <Link to="/login">Login</Link>
                )}
            </div>
        </header>
    );
}

