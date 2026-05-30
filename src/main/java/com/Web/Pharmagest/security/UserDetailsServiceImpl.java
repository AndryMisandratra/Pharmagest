package com.Web.Pharmagest.security;

import com.Web.Pharmagest.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service  // ← CRUCIAL : sans ça Spring ne crée pas le bean
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        var utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé : " + email
                ));

        if (!utilisateur.getEstActif()) {
            throw new UsernameNotFoundException(
                    "Compte désactivé : " + email
            );
        }

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getPasswordHash())
                .authorities(List.of(
                        new SimpleGrantedAuthority(
                                utilisateur.getRole().name()
                        )
                ))
                .build();
    }
}