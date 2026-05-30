import React, { createContext, useState, useContext, useEffect } from 'react';
import axiosInstance from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {

  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Au démarrage → vérifie si un token existe déjà
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser  = localStorage.getItem('user');

    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  // ================================================
  // CONNEXION
  // ================================================
  const login = async (email, password) => {
    const response = await axiosInstance.post('/api/auth/login', {
      email,
      password,
    });

    const data = response.data;

    // Stocker le token et les infos utilisateur
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      email:  data.email,
      nom:    data.nom,
      prenom: data.prenom,
      role:   data.role,
    }));

    setToken(data.token);
    setUser({
      email:  data.email,
      nom:    data.nom,
      prenom: data.prenom,
      role:   data.role,
    });

    return data;
  };

  // ================================================
  // DÉCONNEXION
  // ================================================
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  // ================================================
  // VÉRIFICATION DES RÔLES
  // ================================================
  const isPharmacien = () =>
    user?.role === 'ROLE_PHARMACIEN';

  const isPreparateur = () =>
    user?.role === 'ROLE_PREPARATEUR';

  const isCaissier = () =>
    user?.role === 'ROLE_CAISSIER';

  const isAdmin = () =>
    user?.role === 'ROLE_ADMIN';

  // Peut accéder aux fonctions de vente
  const canSell = () =>
    ['ROLE_PHARMACIEN', 'ROLE_PREPARATEUR',
     'ROLE_CAISSIER'].includes(user?.role);

  // Peut gérer le stock
  const canManageStock = () =>
    ['ROLE_PHARMACIEN',
     'ROLE_PREPARATEUR'].includes(user?.role);

  return (
    <AuthContext.Provider value={{
      user,
      token,
      loading,
      login,
      logout,
      isPharmacien,
      isPreparateur,
      isCaissier,
      isAdmin,
      canSell,
      canManageStock,
      isAuthenticated: !!token,
    }}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook personnalisé
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error(
      'useAuth doit être utilisé dans AuthProvider'
    );
  }
  return context;
};

export default AuthContext;