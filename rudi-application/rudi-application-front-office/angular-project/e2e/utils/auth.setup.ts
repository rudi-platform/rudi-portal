/* global process, __dirname */
/**
 * Playwright authentication setup.
 *
 * Ce fichier effectue un login via l'API /authenticate du proxy Angular (port 4200),
 * puis sauvegarde les tokens JWT dans un fichier JSON.
 * Les tests authentifiés restaurent ces tokens via le helper `injectAuthTokens()`.
 *
 * Variables d'environnement :
 *   RUDI_LOGIN    — login utilisateur (défaut : 'rudi')
 *   RUDI_PASSWORD — mot de passe (défaut : 'rudi@123')
 */
import { test as setup } from '@playwright/test';
import fs from 'fs';
import path from 'path';

export const AUTH_TOKENS_PATH = path.join(__dirname, '../../test-results/.auth/tokens.json');

export interface AuthTokens {
  jwt: string;
  xtoken: string | null;
}

setup('authenticate', async ({ request }) => {
  const login = process.env.RUDI_LOGIN || 'rudi';
  const password = process.env.RUDI_PASSWORD || 'rudi@123';

  // Appeler l'API d'authentification via le proxy Angular
  const response = await request.post('/authenticate', {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    form: {
      login,
      password,
    },
  });

  if (!response.ok()) {
    throw new Error(`Authentication failed: ${response.status()} ${response.statusText()}`);
  }

  // Récupérer les tokens depuis les headers de réponse
  const jwtToken = response.headers()['authorization'];
  const xToken = response.headers()['x-token'] || null;

  if (!jwtToken) {
    throw new Error('No Authorization header in auth response');
  }

  // Sauvegarder les tokens dans un fichier JSON
  const tokens: AuthTokens = { jwt: jwtToken, xtoken: xToken };
  const dir = path.dirname(AUTH_TOKENS_PATH);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(AUTH_TOKENS_PATH, JSON.stringify(tokens, null, 2));
});
