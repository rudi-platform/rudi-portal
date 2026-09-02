import { defineConfig, devices } from "@playwright/test";

/**
 * Configuration Playwright pour les tests RGAA (accessibilité).
 *
 * Usage :
 *   npx playwright test                              # tous les tests (avec auth)
 *   npx playwright test --project=chromium-noauth     # sans authentification (pages publiques)
 *   npx playwright test --project=chromium            # avec authentification (pages protégées)
 *   npx playwright test 11-formulaires                # une thématique
 *   npx playwright test --reporter=html               # avec rapport HTML
 *
 * Variables d'environnement :
 *   BASE_URL        — URL du serveur Angular (défaut : http://localhost:4200)
 *   TARGET_URL      — path de la page à tester (défaut : /)
 *   RUDI_LOGIN      — login utilisateur (défaut : rudi)
 *   RUDI_PASSWORD   — mot de passe (défaut : rudi@123)
 *
 * Exemples :
 *   npx playwright test 11-formulaires --project=chromium-noauth   # page d'accueil, sans auth
 *   TARGET_URL=/login npx playwright test 11-formulaires --project=chromium-noauth
 *   TARGET_URL=/projets/soumettre-un-projet npx playwright test 11-formulaires --project=chromium
 *
 * L'application Angular doit tourner sur http://localhost:4200 avant de lancer les tests.
 * Lancer : npm start (ou la tâche VS Code "front: angular (dev)")
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [["list"], ["html", { open: "never", outputFolder: "test-results/rgaa-report" }]],
  use: {
    baseURL: process.env.BASE_URL || "http://localhost:4200",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [
    /* Projet setup : authentification */
    {
      name: "setup",
      testDir: "./e2e/utils",
      testMatch: /auth\.setup\.ts/,
    },
    /* Tests avec authentification (pages protégées) */
    {
      name: "chromium",
      testDir: "./e2e/rgaa",
      use: {
        ...devices["Desktop Chrome"],
      },
      dependencies: ["setup"],
    },
    /* Tests sans authentification (pages publiques : login, inscription) */
    {
      name: "chromium-noauth",
      testDir: "./e2e/rgaa",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  /* Ne pas lancer le serveur Angular automatiquement — il tourne déjà via les tâches VS Code */
});
