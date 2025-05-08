import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLoggedUser, logout, isAuthenticated } from '../services/authService';
import Layout from '../components/Layout';

export default function HomePage() {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        // Check if user is authenticated
        if (!isAuthenticated()) {
            navigate('/login');
            return;
        }

        // Get user information
        const userInfo = getLoggedUser();
        setUser(userInfo);
    }, [navigate]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (!user) {
        return <div>Loading...</div>;
    }
    
    return (
        <Layout>
            <div className="home-container">
                <h1>Home Page</h1>
                <div className="welcome-message">
                    <p>Welcome, {user.fullName}!</p>
                </div>
            <div className="actions">
                <button onClick={handleLogout} className="logout-button">
                    Close Session
                </button>
            </div>
            </div>
        </Layout>
    );
}


