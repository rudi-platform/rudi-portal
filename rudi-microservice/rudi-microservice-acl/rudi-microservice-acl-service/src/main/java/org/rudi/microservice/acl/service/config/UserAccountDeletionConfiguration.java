package org.rudi.microservice.acl.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration de la suppression automatique des comptes utilisateur après inactivité.
 *
 * Propriétés:
 * - rudi.account.inactivity-months-before-deletion: Nombre de mois d'inactivité avant suppression du compte (défaut: 24)
 * - rudi.account.deletion-warning-months-before: Nombre de mois avant la suppression effective auquel l'avertissement est envoyé (défaut: 1)
 *   Exemple: Si inactivité de 24 mois et avertissement de 1 mois, l'utilisateur sera averti après 23 mois d'inactivité.
 */
@Configuration
@ConfigurationProperties(prefix = "rudi.account")
@Getter
@Setter
public class UserAccountDeletionConfiguration {

	/**
	 * Nombre de mois d'inactivité après lesquels un compte utilisateur est automatiquement supprimé.
	 * Défaut: 24 mois
	 */
	private int inactivityMonthsBeforeDeletion = 24;

	/**
	 * Nombre de mois avant la suppression effective auquel un avertissement de suppression est envoyé à l'utilisateur.
	 * Défaut: 1 mois
	 *
	 * Exemple: Si inactivityMonthsBeforeDeletion=24 et deletionWarningMonthsBefore=1,
	 * l'avertissement sera envoyé après 23 mois d'inactivité.
	 */
	private int deletionWarningMonthsBefore = 1;
}

