import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

// Titre de chaque page selon l'URL
const pageTitles = {
  '/dashboard':    'Tableau de bord',
  '/medicaments':  'Catalogue Médicaments',
  '/stock':        'Gestion du Stock',
  '/ventes':       'Point de Vente',
  '/ordonnances':  'Ordonnances',
  '/patients':     'Dossiers Patients',
  '/fournisseurs': 'Fournisseurs',
  '/commandes':    'Commandes Fournisseurs',
  '/rapports':     'Rapports & Exports',
  '/utilisateurs': 'Gestion Utilisateurs',
};

const Layout = () => {
  const location = useLocation();
  const title = pageTitles[location.pathname]
             || 'PharmaGest';

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50">

      {/* Sidebar fixe à gauche */}
      <Sidebar />

      {/* Contenu principal */}
      <div className="flex-1 flex flex-col min-h-0">

        {/* Navbar en haut */}
        <Navbar title={title} />

        {/* Contenu de la page */}
        <main className="flex-1 min-h-0 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;