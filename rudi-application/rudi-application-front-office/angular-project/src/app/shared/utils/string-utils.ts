export class StringUtils {

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasLength(CharSequence)
     * @see #hasText(String)
     */
    static hasLength(str: string): boolean {
        return (str != null && str !== '');
    }

    /**
     * Transforme une saisie de recherche en motif avec jokers pour la recherche "contient".
     * Chaque mot est préfixé d'une étoile et le motif se termine par une étoile.
     * Exemple : "café du bar" => "*café *du *bar*"
     *
     * Recherche exacte : si la saisie est entourée de guillemets droits, ceux-ci sont retirés
     * et aucun joker n'est ajouté, ce qui donne côté back une égalité stricte.
     * Exemple : "\"réu\"" => "réu" (ne remonte que les valeurs strictement égales à "réu")
     * @param search la saisie utilisateur (peut être {@code null})
     * @return le motif de recherche, ou une chaîne vide si la saisie est vide/blanche
     */
    static toWildcardSearch(search: string): string {
        const trimmed = search?.trim();
        if (!StringUtils.hasLength(trimmed)) {
            return '';
        }
        // Recherche exacte : saisie entourée de guillemets droits -> pas de joker
        if (trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"')) {
            return trimmed.substring(1, trimmed.length - 1).trim();
        }
        return trimmed.split(/\s+/).map((word: string) => '*' + word).join(' ') + '*';
    }

}
