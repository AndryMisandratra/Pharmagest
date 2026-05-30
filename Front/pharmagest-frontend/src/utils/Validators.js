// ================================================
// VALIDATIONS COMMUNES
// ================================================

// Téléphone Madagascar :
// 10 chiffres commençant par 032, 033, 034, 037, 038
export const validateTelephone = (tel) => {
  if (!tel) return true; // optionnel
  const regex = /^(032|033|034|037|038)\d{7}$/;
  if (!regex.test(tel)) {
    return 'Numéro invalide — doit commencer par 032, 033, 034, 037 ou 038 (10 chiffres)';
  }
  return true;
};

// Email valide
export const validateEmail = (email) => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!regex.test(email)) {
    return 'Format email invalide';
  }
  return true;
};

// Email Google valide (Gmail / Googlemail)
export const validateGoogleEmail = (email) => {
  const validDomain = /@(?:gmail\.com|googlemail\.com)$/i;
  if (!email || !validDomain.test(email)) {
    return 'L\'email doit être une adresse Gmail valide';
  }
  return true;
};

// Code CIP 13 chiffres
export const validateCodeCip = (cip) => {
  const regex = /^\d{13}$/;
  if (!regex.test(cip)) {
    return 'Le code CIP doit contenir exactement 13 chiffres';
  }
  return true;
};

// Numéro sécurité sociale 15 chiffres
export const validateNumSecuSociale = (num) => {
  if (!num) return true; // optionnel
  const regex = /^\d{15}$/;
  if (!regex.test(num)) {
    return 'Le numéro de sécurité sociale doit contenir 15 chiffres';
  }
  return true;
};

// Nom/prénom/nom de médecin sans chiffres ni caractères spéciaux
export const validateName = (value) => {
  if (!value) return true; // optionnel
  const regex = /^[A-Za-zÀ-ÖØ-öø-ÿ' .-]+$/;
  if (!regex.test(value)) {
    return 'Ce champ ne doit contenir que des lettres, espaces, apostrophes, traits d’union ou points';
  }
  return true;
};

// Montant positif
export const validateMontant = (montant) => {
  if (montant <= 0) {
    return 'Le montant doit être supérieur à 0';
  }
  return true;
};

// Nettoie une saisie de chiffres uniquement
export const sanitizeDigits = (value) =>
  value.replace(/\D+/g, '');

// Nettoie une saisie de nom/prénom autorisant accents, espaces, apostrophes, traits d'union et points
export const sanitizeNameInput = (value) =>
  value.replace(/[^A-Za-zÀ-ÖØ-öø-ÿ' .-]+/g, '');

// Date future (péremption)
export const validateDateFuture = (date) => {
  if (!date) return 'La date est obligatoire';
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  if (new Date(date) <= today) {
    return 'La date doit être dans le futur';
  }
  return true;
};

// Mot de passe minimum 8 caractères
export const validatePassword = (password) => {
  if (password.length < 8) {
    return 'Le mot de passe doit contenir au moins 8 caractères';
  }
  return true;
};