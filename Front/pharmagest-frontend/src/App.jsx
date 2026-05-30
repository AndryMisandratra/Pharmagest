import React from 'react';
import {
  BrowserRouter as Router,
  Routes, Route, Navigate
} from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './context/AuthContext';

// Layout
import Layout from './components/layout/Layout';

// Auth
import LoginPage from './pages/auth/LoginPage';

// Dashboard
import DashboardPage from './pages/dashboard/DashboardPage';

// Médicaments
import MedicamentsPage
  from './pages/medicaments/MedicamentsPage';
import MedicamentFormPage
  from './pages/medicaments/MedicamentFormPage';

// Stock
import StockPage from './pages/stock/StockPage';
import HistoriquePage from './pages/stock/HistoriquePage';

// Ventes
import VentePage from './pages/ventes/VentePage';
import VenteDetailPage
  from './pages/ventes/VenteDetailsPage';

// Patients
import PatientsPage from './pages/patients/PatientsPage';
import PatientFormPage
  from './pages/patients/PatientFormPage';

// Fournisseurs
import FournisseursPage
  from './pages/fournisseurs/FournisseursPage';
import FournisseurFormPage
  from './pages/fournisseurs/FournisseurFormPage';

// Commandes
import CommandesPage
  from './pages/commandes/CommandesPage';
import CommandeFormPage
  from './pages/commandes/CommandeFormPage';
import CommandeReceptionPage
  from './pages/commandes/CommandeReceptionPage';

// Rapports
import RapportsPage from './pages/rapports/RapportsPage';

// Utilisateurs
import UtilisateursPage
  from './pages/utilisateurs/UtilisateursPage';
import UtilisateurFormPage
  from './pages/utilisateurs/UtilisateurFormPage';

// ================================================
// Route protégée
// ================================================
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) {
    return (
      <div className="flex items-center justify-center
                      min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full
                        h-12 w-12 border-b-2
                        border-primary-600"/>
      </div>
    );
  }
  return isAuthenticated
    ? children
    : <Navigate to="/login" replace />;
};

// ================================================
// Routes de l'application
// ================================================
const AppRoutes = () => {
  const { isAuthenticated } = useAuth();
  return (
    <Routes>

      {/* Route publique */}
      <Route
        path="/login"
        element={
          isAuthenticated
            ? <Navigate to="/dashboard" replace />
            : <LoginPage />
        }
      />

      {/* Routes protégées */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index
               element={
                 <Navigate to="/dashboard" replace />
               }/>

        {/* Dashboard */}
        <Route path="dashboard"
               element={<DashboardPage />} />

        {/* Médicaments */}
        <Route path="medicaments"
               element={<MedicamentsPage />} />
        <Route path="medicaments/nouveau"
               element={<MedicamentFormPage />} />
        <Route path="medicaments/:id/modifier"
               element={<MedicamentFormPage />} />

        {/* Stock */}
        <Route path="stock"
               element={<StockPage />} />
        <Route path="stock/historique/:medicamentId"
               element={<HistoriquePage />} />

        {/* Ventes */}
        <Route path="ventes"
               element={<VentePage />} />
        <Route path="ventes/:id"
               element={<VenteDetailPage />} />

        {/* Patients */}
        <Route path="patients"
               element={<PatientsPage />} />
        <Route path="patients/nouveau"
               element={<PatientFormPage />} />
        <Route path="patients/:id/modifier"
               element={<PatientFormPage />} />

        {/* Fournisseurs */}
        <Route path="fournisseurs"
               element={<FournisseursPage />} />
        <Route path="fournisseurs/nouveau"
               element={<FournisseurFormPage />} />
        <Route path="fournisseurs/:id/modifier"
               element={<FournisseurFormPage />} />

        {/* Commandes */}
        <Route path="commandes"
               element={<CommandesPage />} />
        <Route path="commandes/nouvelle"
               element={<CommandeFormPage />} />
        <Route path="commandes/:id"
               element={<CommandeFormPage />} />
        <Route path="commandes/:id/modifier"
               element={<CommandeFormPage />} />
        <Route path="commandes/:id/reception"
               element={<CommandeReceptionPage />} />

        {/* Rapports */}
        <Route path="rapports"
               element={<RapportsPage />} />

        {/* Utilisateurs */}
        <Route path="utilisateurs"
               element={<UtilisateursPage />} />
        <Route path="utilisateurs/nouveau"
               element={<UtilisateurFormPage />} />
        <Route path="utilisateurs/:id/modifier"
               element={<UtilisateurFormPage />} />

      </Route>

      {/* Redirection par défaut */}
      <Route path="*"
             element={
               <Navigate to="/dashboard" replace />
             }/>
    </Routes>
  );
};

// ================================================
// App principale
// ================================================
function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          pauseOnHover
          theme="light"
        />
      </Router>
    </AuthProvider>
  );
}

export default App;
