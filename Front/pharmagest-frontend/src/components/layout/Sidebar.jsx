import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard, Pill, Package, ShoppingCart,
  Users, Truck, ClipboardList,
  BarChart3, UserCog, LogOut, ChevronLeft,
  ChevronRight
} from 'lucide-react';

const Sidebar = () => {
  const { user, logout, isPharmacien,
          isCaissier, canManageStock, canSell } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Définition des menus selon le rôle
  const menuItems = [
    {
      path: '/dashboard',
      icon: <LayoutDashboard size={20} />,
      label: 'Dashboard',
      visible: true
    },
    {
      path: '/medicaments',
      icon: <Pill size={20} />,
      label: 'Médicaments',
      visible: true
    },
    {
      path: '/stock',
      icon: <Package size={20} />,
      label: 'Stock & Lots',
      visible: canManageStock()
    },
    {
      path: '/ventes',
      icon: <ShoppingCart size={20} />,
      label: 'Ventes',
      visible: canSell()
    },
    {
      path: '/patients',
      icon: <Users size={20} />,
      label: 'Patients',
      visible: canManageStock()
    },
    {
      path: '/fournisseurs',
      icon: <Truck size={20} />,
      label: 'Fournisseurs',
      visible: !isCaissier()
    },
    {
      path: '/commandes',
      icon: <ClipboardList size={20} />,
      label: 'Commandes',
      visible: canManageStock()
    },
    {
      path: '/rapports',
      icon: <BarChart3 size={20} />,
      label: 'Rapports',
      visible: isPharmacien()
    },
    {
      path: '/utilisateurs',
      icon: <UserCog size={20} />,
      label: 'Utilisateurs',
      visible: isPharmacien()
    },
  ];

  return (
    <div className={`bg-primary-900 text-white
                     min-h-screen flex flex-col
                     transition-all duration-300
                     ${collapsed ? 'w-16' : 'w-64'}`}>

      {/* Logo */}
      <div className="flex items-center justify-between
                      p-4 border-b border-primary-700">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <span className="text-2xl">💊</span>
            <span className="font-bold text-lg">
              PharmaGest
            </span>
          </div>
        )}
        {collapsed && (
          <span className="text-2xl mx-auto">💊</span>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="text-primary-300
                     hover:text-white transition">
          {collapsed
            ? <ChevronRight size={18} />
            : <ChevronLeft size={18} />}
        </button>
      </div>

      {/* Infos utilisateur */}
      {!collapsed && (
        <div className="p-4 border-b border-primary-700">
          <p className="font-semibold text-sm">
            {user?.prenom} {user?.nom}
          </p>
          <span className="inline-block mt-1 px-2 py-0.5
                           bg-primary-600 rounded-full
                           text-xs text-primary-100">
            {user?.role?.replace('ROLE_', '')}
          </span>
        </div>
      )}

      {/* Menu navigation */}
      <nav className="flex-1 py-4 overflow-y-auto">
        {menuItems
          .filter(item => item.visible)
          .map(item => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3
                 transition duration-200 text-sm
                 ${isActive
                   ? 'bg-primary-600 text-white border-r-4 border-blue-300'
                   : 'text-primary-200 hover:bg-primary-700 hover:text-white'
                 }
                 ${collapsed ? 'justify-center' : ''}`
              }>
              {item.icon}
              {!collapsed && (
                <span>{item.label}</span>
              )}
            </NavLink>
          ))
        }
      </nav>

      {/* Bouton déconnexion */}
      <div className="p-4 border-t border-primary-700">
        <button
          onClick={handleLogout}
          className={`flex items-center gap-3
                     text-primary-200
                     hover:text-red-400
                     transition duration-200
                     text-sm w-full
                     ${collapsed ? 'justify-center' : ''}`}>
          <LogOut size={20} />
          {!collapsed && <span>Déconnexion</span>}
        </button>
      </div>
    </div>
  );
};

export default Sidebar;