import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { Eye, EyeOff, LogIn, Shield } from 'lucide-react';

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm();

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      await login(data.email, data.password);
      toast.success('Connexion réussie ! Bienvenue 👋');
      navigate('/dashboard');
    } catch (error) {
      toast.error(
        error.response?.data ||
        'Email ou mot de passe incorrect'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">

      {/* Partie gauche — Décoration */}
      <div className="hidden lg:flex lg:w-1/2
                      bg-gradient-to-br from-primary-900
                      via-primary-700 to-primary-500
                      flex-col items-center justify-center
                      p-12 text-white">
        <div className="max-w-md text-center">
          <div className="text-8xl mb-6">💊</div>
          <h1 className="text-4xl font-bold mb-4">
            PharmaGest
          </h1>
          <p className="text-primary-200 text-lg mb-8">
            Système de gestion de pharmacie complet,
            sécurisé et ergonomique
          </p>

          {/* Fonctionnalités */}
          <div className="space-y-3 text-left">
            {[
              '✅ Gestion du stock en temps réel',
              '✅ Alertes automatiques de péremption',
              '✅ Point de vente avec règle FEFO',
              '✅ Rapports PDF et CSV',
              '✅ Gestion des ordonnances',
            ].map((feature, i) => (
              <div key={i}
                   className="flex items-center gap-2
                              bg-white/10 rounded-lg px-4 py-2
                              text-sm">
                {feature}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Partie droite — Formulaire */}
      <div className="w-full lg:w-1/2 flex items-center
                      justify-center p-8 bg-gray-50">
        <div className="w-full max-w-md">

          {/* En-tête mobile */}
          <div className="lg:hidden text-center mb-8">
            <span className="text-5xl">💊</span>
            <h1 className="text-2xl font-bold
                           text-primary-900 mt-2">
              PharmaGest
            </h1>
          </div>

          {/* Carte formulaire */}
          <div className="bg-white rounded-2xl shadow-xl p-8">

            {/* Titre */}
            <div className="mb-6">
              <h2 className="text-2xl font-bold
                             text-gray-800">
                Connexion
              </h2>
              <p className="text-gray-500 text-sm mt-1">
                Entrez vos identifiants pour accéder
                au système
              </p>
            </div>

            {/* Formulaire */}
            <form onSubmit={handleSubmit(onSubmit)}
                  className="space-y-5">

              {/* Email */}
              <div>
                <label className="block text-sm
                                   font-medium
                                   text-gray-700 mb-1">
                  Adresse email
                  <span className="text-red-500 ml-1">*</span>
                </label>
                <input
                  type="email"
                  autoComplete="email"
                  placeholder="votre@email.com"
                  className={`w-full px-4 py-3 rounded-xl
                             border-2 text-sm
                             transition duration-200
                             focus:outline-none
                             ${errors.email
                               ? 'border-red-400 bg-red-50 focus:border-red-500'
                               : 'border-gray-200 focus:border-primary-500 bg-white'
                             }`}
                  {...register('email', {
                    required: "L'email est obligatoire",
                    pattern: {
                      value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                      message: 'Format email invalide'
                    }
                  })}
                />
                {errors.email && (
                  <p className="text-red-500 text-xs
                                mt-1 flex items-center gap-1">
                    ⚠️ {errors.email.message}
                  </p>
                )}
              </div>

              {/* Mot de passe */}
              <div>
                <label className="block text-sm
                                   font-medium
                                   text-gray-700 mb-1">
                  Mot de passe
                  <span className="text-red-500 ml-1">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    placeholder="••••••••"
                    className={`w-full px-4 py-3 rounded-xl
                               border-2 text-sm pr-12
                               transition duration-200
                               focus:outline-none
                               ${errors.password
                                 ? 'border-red-400 bg-red-50 focus:border-red-500'
                                 : 'border-gray-200 focus:border-primary-500 bg-white'
                               }`}
                    {...register('password', {
                      required: 'Le mot de passe est obligatoire',
                      minLength: {
                        value: 6,
                        message: 'Minimum 6 caractères'
                      }
                    })}
                  />
                  <button
                    type="button"
                    onClick={() =>
                      setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2
                               -translate-y-1/2 text-gray-400
                               hover:text-gray-600
                               transition">
                    {showPassword
                      ? <EyeOff size={20} />
                      : <Eye size={20} />}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-red-500 text-xs
                                mt-1 flex items-center gap-1">
                    ⚠️ {errors.password.message}
                  </p>
                )}
              </div>

              {/* Bouton connexion */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-primary-600
                           hover:bg-primary-700
                           disabled:bg-primary-300
                           text-white font-semibold
                           py-3 px-4 rounded-xl
                           flex items-center
                           justify-center gap-2
                           transition duration-200
                           shadow-md hover:shadow-lg
                           text-sm mt-2">
                {isLoading ? (
                  <>
                    <div className="animate-spin
                                    rounded-full h-5 w-5
                                    border-b-2 border-white"/>
                    Connexion en cours...
                  </>
                ) : (
                  <>
                    <LogIn size={18} />
                    Se connecter
                  </>
                )}
              </button>
            </form>

            {/* Sécurité */}
            <div className="mt-6 flex items-center
                            justify-center gap-2
                            text-xs text-gray-400">
              <Shield size={14} />
              <span>Connexion sécurisée avec JWT</span>
            </div>
          </div>

          <p className="text-center text-xs
                        text-gray-400 mt-4">
            PharmaGest v1.0 — © 2025 Tous droits réservés
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;