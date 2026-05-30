import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import {
  TrendingUp, ShoppingCart, Package,
  AlertTriangle, Pill, Activity,
  ArrowUp, ArrowDown, RefreshCw,
  Clock, DollarSign, Users, FileText
} from 'lucide-react';
import dashboardApi from '../../api/dashboardApi';
import { useAuth } from '../../context/AuthContext';

// ================================================
// Composant carte KPI
// ================================================
const KpiCard = ({ title, value, subtitle,
                   icon, color, trend }) => (
  <div className="bg-white rounded-2xl shadow-md
                  border border-gray-100 p-5
                  hover:shadow-lg hover:-translate-y-0.5
                  transition-all duration-300">
    <div className="flex items-start justify-between">
      <div className="flex-1">
        <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
          {title}
        </p>
        <p className={`text-2xl font-extrabold mt-2 ${color}`}>
          {value}
        </p>
        {subtitle && (
          <p className="text-xs text-gray-400 mt-1.5 font-medium">
            {subtitle}
          </p>
        )}
        {trend !== undefined && (
          <div className={`flex items-center gap-1 mt-2
                          text-xs font-bold
                          ${trend >= 0
                            ? 'text-green-600'
                            : 'text-red-500'}`}>
            {trend >= 0
              ? <ArrowUp size={12} />
              : <ArrowDown size={12} />}
            {Math.abs(trend)}% vs hier
          </div>
        )}
      </div>
      <div className={`p-3 rounded-xl ${color
        .replace('text-', 'bg-')
        .replace('600', '100')
        .replace('900', '100')
        .replace('500', '100')}`}>
        {icon}
      </div>
    </div>
  </div>
);

// ================================================
// Composant carte alerte
// ================================================
const AlerteCard = ({ titre, items,
                      couleur, icon }) => {
  if (!items || items.length === 0) return null;

  const styles = {
    rouge: {
      card:  'bg-gradient-to-br from-red-50 to-red-100/50 border-red-200',
      titre: 'text-red-700',
      badge: 'bg-red-500 text-white',
      item:  'text-red-600',
      icon:  'text-red-500',
    },
    orange: {
      card:  'bg-gradient-to-br from-orange-50 to-orange-100/50 border-orange-200',
      titre: 'text-orange-700',
      badge: 'bg-orange-500 text-white',
      item:  'text-orange-600',
      icon:  'text-orange-500',
    },
  };

  const s = styles[couleur];

  return (
    <div className={`rounded-xl border p-4 shadow-sm ${s.card}`}>
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className={`p-1.5 rounded-lg bg-white/60 ${s.icon}`}>
            {icon}
          </div>
          <h3 className={`font-bold text-sm ${s.titre}`}>
            {titre}
          </h3>
        </div>
        <span className={`text-xs font-bold px-2.5 py-1
                         rounded-full shadow-sm ${s.badge}`}>
          {items.length}
        </span>
      </div>
      <ul className="space-y-1.5">
        {items.slice(0, 5).map((item, i) => (
          <li key={i}
              className={`text-xs flex items-center
                         gap-2 ${s.item} font-medium`}>
            <span className={`w-1.5 h-1.5 rounded-full ${s.icon.replace('text-', 'bg-')}`} />
            <span className="truncate">{item}</span>
          </li>
        ))}
        {items.length > 5 && (
          <li className={`text-xs italic ${s.item} font-medium`}>
            +{items.length - 5} autres...
          </li>
        )}
      </ul>
    </div>
  );
};

// ================================================
// Page Dashboard principale
// ================================================
const DashboardPage = () => {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(null);

  const fetchDashboard = async () => {
    setLoading(true);
    try {
      const response = await dashboardApi.getResume();
      setData(response.data);
      setLastUpdate(new Date());
    } catch (error) {
      toast.error('Erreur chargement du dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
    // Rafraîchissement auto toutes les 5 minutes
    const interval = setInterval(fetchDashboard, 300000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-80 gap-4">
        <div className="relative">
          <div className="w-16 h-16 border-4 border-gray-200 border-t-primary-600 rounded-full animate-spin"></div>
          <div className="absolute inset-0 flex items-center justify-center">
            <Activity size={24} className="text-primary-600 animate-pulse" />
          </div>
        </div>
        <p className="text-gray-500 text-sm font-medium animate-pulse">
          Chargement du tableau de bord...
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">

      {/* En-tête */}
      <div className="flex items-center
                      justify-between">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center shadow-lg">
            <span className="text-2xl text-white font-bold">
              {user?.prenom?.charAt(0).toUpperCase()}
            </span>
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-800">
              Bonjour, {user?.prenom} 👋
            </h2>
            <p className="text-gray-500 text-sm mt-0.5 flex items-center gap-1">
              <Clock size={14} />
              Voici le résumé de votre pharmacie aujourd'hui
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {/* Dernière mise à jour */}
          {lastUpdate && (
            <p className="text-xs text-gray-400 bg-gray-50 px-3 py-1.5 rounded-lg">
              Mis à jour à{' '}
              {lastUpdate.toLocaleTimeString('fr-FR', {
                hour: '2-digit',
                minute: '2-digit'
              })}
            </p>
          )}
          {/* Bouton rafraîchir */}
          <button
            onClick={fetchDashboard}
            className="flex items-center gap-2
                       bg-white border border-gray-200
                       text-gray-600 hover:bg-gray-50
                       px-4 py-2 rounded-xl text-sm
                       transition shadow-sm hover:shadow-md">
            <RefreshCw size={16} />
            Actualiser
          </button>
        </div>
      </div>

      {/* KPIs du jour */}
      <div className="grid grid-cols-1 sm:grid-cols-2
                      lg:grid-cols-4 gap-4">
        <KpiCard
          title="Chiffre d'affaires"
          value={data?.chiffreAffairesJour
            ? `${Number(data.chiffreAffairesJour)
                .toLocaleString('fr-FR')} Ar`
            : '0 Ar'}
          subtitle="Ventes validées aujourd'hui"
          icon={<TrendingUp size={22}
                className="text-green-600" />}
          color="text-green-600"
        />
        <KpiCard
          title="Ventes du jour"
          value={data?.nombreVentesJour || 0}
          subtitle="Transactions effectuées"
          icon={<ShoppingCart size={22}
                className="text-blue-600" />}
          color="text-blue-600"
        />
        <KpiCard
          title="Médicaments"
          value={data?.nombreMedicamentsActifs || 0}
          subtitle="Dans le catalogue"
          icon={<Pill size={22}
                className="text-purple-600" />}
          color="text-purple-600"
        />
        <KpiCard
          title="Alertes actives"
          value={
            (data?.nombreAlerteRougeStock || 0) +
            (data?.nombreAlerteRougePeremption || 0)
          }
          subtitle="Nécessitent votre attention"
          icon={<AlertTriangle size={22}
                className="text-red-600" />}
          color="text-red-600"
        />
      </div>

      {/* Section alertes */}
      {((data?.nombreAlerteRougeStock > 0) ||
        (data?.nombreAlerteOrangeStock > 0) ||
        (data?.nombreAlerteRougePeremption > 0) ||
        (data?.nombreAlerteOrangePeremption > 0)) && (

        <div className="bg-white rounded-2xl shadow-md
                        border border-gray-100 p-6">
          <h3 className="text-lg font-bold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <div className="p-2 rounded-lg bg-orange-100">
              <AlertTriangle size={18}
                             className="text-orange-600" />
            </div>
            Alertes actives
            <span className="ml-auto text-xs font-medium bg-red-100 text-red-600 px-2 py-1 rounded-full">
              {(data?.nombreAlerteRougeStock || 0) +
               (data?.nombreAlerteOrangeStock || 0) +
               (data?.nombreAlerteRougePeremption || 0) +
               (data?.nombreAlerteOrangePeremption || 0)} total
            </span>
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2
                          gap-4">
            <AlerteCard
              titre="Rupture de stock imminente"
              items={data?.medicamentsStockBas}
              couleur="rouge"
              icon={<Package size={16}
                    className="text-red-600" />}
            />
            <AlerteCard
              titre="Stock faible"
              items={data?.medicamentsStockFaible}
              couleur="orange"
              icon={<Package size={16}
                    className="text-orange-600" />}
            />
            {data?.nombreAlerteRougePeremption > 0 && (
              <AlerteCard
                titre="Péremption < 30 jours"
                items={[`${data.nombreAlerteRougePeremption} lot(s) concerné(s)`]}
                couleur="rouge"
                icon={<AlertTriangle size={16}
                      className="text-red-600" />}
              />
            )}
            {data?.nombreAlerteOrangePeremption > 0 && (
              <AlerteCard
                titre="Péremption < 90 jours"
                items={[`${data.nombreAlerteOrangePeremption} lot(s) concerné(s)`]}
                couleur="orange"
                icon={<AlertTriangle size={16}
                      className="text-orange-600" />}
              />
            )}
          </div>
        </div>
      )}

      {/* Aucune alerte */}
      {data &&
        data.nombreAlerteRougeStock === 0 &&
        data.nombreAlerteOrangeStock === 0 &&
        data.nombreAlerteRougePeremption === 0 &&
        data.nombreAlerteOrangePeremption === 0 && (
        <div className="bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200
                        rounded-2xl p-6 flex items-center
                        gap-4 shadow-sm">
          <div className="w-12 h-12 rounded-xl bg-green-100 flex items-center justify-center">
            <span className="text-2xl">✅</span>
          </div>
          <div>
            <p className="font-bold text-green-800 text-lg">
              Tout est en ordre !
            </p>
            <p className="text-green-600 text-sm">
              Aucune alerte de stock ou de péremption
              détectée aujourd'hui.
            </p>
          </div>
        </div>
      )}

      {/* Top médicaments */}
      {data?.topMedicaments &&
       data.topMedicaments.length > 0 && (
        <div className="bg-white rounded-2xl shadow-md
                        border border-gray-100 p-6">
          <h3 className="text-lg font-bold
                         text-gray-800 mb-4
                         flex items-center gap-2">
            <div className="p-2 rounded-lg bg-primary-100">
              <TrendingUp size={18}
                          className="text-primary-600" />
            </div>
            Top 5 médicaments — 30 derniers jours
          </h3>
          <div className="space-y-3">
            {data.topMedicaments.map((med, index) => {
              const maxQty = data.topMedicaments[0]
                              .quantiteVendue;
              const pct = (med.quantiteVendue / maxQty)
                          * 100;
              return (
                <div key={index}
                     className="flex items-center gap-4 p-3 rounded-xl hover:bg-gray-50 transition-colors">
                  {/* Rang */}
                  <span className={`w-8 h-8 rounded-xl
                                   flex items-center
                                   justify-center text-sm
                                   font-bold flex-shrink-0
                                   ${index === 0
                                     ? 'bg-gradient-to-br from-yellow-400 to-yellow-500 text-white shadow-md'
                                     : index === 1
                                     ? 'bg-gradient-to-br from-gray-300 to-gray-400 text-gray-700 shadow-md'
                                     : index === 2
                                     ? 'bg-gradient-to-br from-orange-400 to-orange-500 text-white shadow-md'
                                     : 'bg-gray-100 text-gray-500'
                                   }`}>
                    {index + 1}
                  </span>
                  {/* Nom */}
                  <span className="text-sm font-semibold text-gray-700
                                   flex-1 truncate">
                    {med.denomination}
                  </span>
                  {/* Barre de progression */}
                  <div className="flex-1 max-w-48">
                    <div className="bg-gray-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className="bg-gradient-to-r from-primary-500 to-primary-600 rounded-full h-2.5 transition-all duration-500"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                  {/* Quantité */}
                  <span className="text-sm font-bold text-gray-600 w-20 text-right">
                    {med.quantiteVendue} u
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Résumé rapide */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl shadow-md
                        border border-gray-100 p-5
                        hover:shadow-lg transition-all">
          <h4 className="text-xs font-bold uppercase tracking-wide text-gray-400 mb-3">
            Stock en alerte rouge
          </h4>
          <p className="text-3xl font-extrabold text-red-600">
            {data?.nombreAlerteRougeStock || 0}
          </p>
          <p className="text-xs text-gray-400 mt-2 font-medium">
            médicaments sous le seuil minimum
          </p>
        </div>
        <div className="bg-white rounded-2xl shadow-md
                        border border-gray-100 p-5
                        hover:shadow-lg transition-all">
          <h4 className="text-xs font-bold uppercase tracking-wide text-gray-400 mb-3">
            Stock en alerte orange
          </h4>
          <p className="text-3xl font-extrabold text-orange-500">
            {data?.nombreAlerteOrangeStock || 0}
          </p>
          <p className="text-xs text-gray-400 mt-2 font-medium">
            médicaments sous 2x le seuil
          </p>
        </div>
        <div className="bg-white rounded-2xl shadow-md
                        border border-gray-100 p-5
                        hover:shadow-lg transition-all">
          <h4 className="text-xs font-bold uppercase tracking-wide text-gray-400 mb-3">
            Lots périmant bientôt
          </h4>
          <p className="text-3xl font-extrabold text-orange-600">
            {(data?.nombreAlerteRougePeremption || 0) +
             (data?.nombreAlerteOrangePeremption || 0)}
          </p>
          <p className="text-xs text-gray-400 mt-2 font-medium">
            lots dans les 90 prochains jours
          </p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;