import React from 'react';
import Header from './Header';
import Footer from './Footer';

export default function Layout({ children }) {
    return (
        <div>
            <Header />
            <main>{children}</main> {/* Aquí va el contenido dinámico (Home, Perfil, etc) */}
            <Footer />
        </div>
    );
}
