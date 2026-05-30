import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Mail } from 'lucide-react';
import { toast } from 'react-toastify';
import dashboardApi from '../../api/dashboardApi';

const Navbar = ({ title }) => {
  const { user } = useAuth();
  const [sendingAlert, setSendingAlert] = useState(false);

  const envoyerAlerte = async () => {
    try {
      setSendingAlert(true);
      await dashboardApi.testerAlertes();
      toast.success('Alerte envoyée au pharmacien');
    } catch (error) {
      toast.error('Impossible d\'envoyer l\'alerte');
    } finally {
      setSendingAlert(false);
    }
  };

  return (
    <header className="sticky top-0 z-20 bg-white shadow-sm border-b
                       border-gray-200 px-6 py-4
                       flex items-center
                       justify-between">

      {/* Titre de la page */}
      <h1 className="text-xl font-semibold
                     text-gray-800">
        {title}
      </h1>

      {/* Droite : utilisateur */}
      <div className="flex items-center gap-4">

        {/* Infos utilisateur */}
        <div className="flex items-center gap-2
                        bg-gray-50 px-3 py-2
                        rounded-lg border border-gray-200">
          <div className="bg-primary-100 rounded-full p-1">
            <User size={16}
                  className="text-primary-600" />
          </div>
          <div className="text-sm">
            <p className="font-medium text-gray-700">
              {user?.prenom} {user?.nom}
            </p>
            <p className="text-xs text-gray-400">
              {user?.role?.replace('ROLE_', '')}
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={envoyerAlerte}
          disabled={sendingAlert}
          className="inline-flex items-center gap-2
                     rounded-lg border border-primary-500
                     bg-primary-50 px-3 py-2 text-sm font-medium
                     text-primary-700 hover:bg-primary-100
                     disabled:cursor-not-allowed
                     disabled:bg-gray-100
                     disabled:text-gray-400"
        >
          <Mail size={16} />
          {sendingAlert ? 'Envoi...' : 'Alerte instant.'}
        </button>
      </div>
    </header>
  );
};

export default Navbar;