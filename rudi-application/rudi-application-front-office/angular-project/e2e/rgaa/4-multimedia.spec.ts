import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  checkImageAlternative,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 4: Multimédia", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("4.1 - Médias temporels avec transcription textuelle", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const videoElements = page.locator("video, audio");
    const count = await videoElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "4.1",
        test: "Médias avec transcription",
        status: "non-applicable",
        details: "Aucun média temporel trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const media = videoElements.nth(i);
      const src = (await media.getAttribute("src")) || `media-${i}`;

      // Chercher une transcription à proximité
      const container = media.locator("..");
      const transcription = container.locator(
        '[class*="transcript"], [class*="transcription"], details:has-text("Transcription")'
      );
      const hasTranscription = (await transcription.count()) > 0;

      // Vérifier aussi aria-describedby
      const ariaDescribedby = await media.getAttribute("aria-describedby");

      if (hasTranscription || ariaDescribedby) {
        conformeCount++;
      } else {
        nonConformeElements.push(src);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "4.1",
      test: "Médias avec transcription",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} médias avec transcription détectée`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });

  test("4.2 - Médias temporels avec sous-titres synchronisés", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const videoElements = page.locator("video");
    const count = await videoElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "4.2",
        test: "Médias avec sous-titres",
        status: "non-applicable",
        details: "Aucune vidéo trouvée",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const video = videoElements.nth(i);
      const src = (await video.getAttribute("src")) || `video-${i}`;

      // Vérifier la présence de balises <track> pour les sous-titres
      const tracks = video.locator(
        "track[kind='subtitles'], track[kind='captions']"
      );
      const hasSubtitles = (await tracks.count()) > 0;

      if (hasSubtitles) {
        conformeCount++;
      } else {
        nonConformeElements.push(src);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "4.2",
      test: "Médias avec sous-titres",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} vidéos avec sous-titres`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });

  test("4.3 - Médias temporels avec audiodescription", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const videoElements = page.locator("video");
    const count = await videoElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "4.3",
        test: "Médias avec audiodescription",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    for (let i = 0; i < count; i++) {
      const video = videoElements.nth(i);
      const src = (await video.getAttribute("src")) || `video-${i}`;

      // Vérifier la présence de balises <track> pour l'audiodescription
      const tracks = video.locator("track[kind='descriptions']");
      const hasAudioDescription = (await tracks.count()) > 0;

      // Vérifier aussi la présence de versions alternatives
      const container = video.locator("..");
      const alternativeVersion = container.locator(
        '[class*="audio-description"], [href*="audiodescription"]'
      );
      const hasAlternative = (await alternativeVersion.count()) > 0;

      if (hasAudioDescription || hasAlternative) {
        conformeCount++;
      } else {
        issues.push(src);
      }
    }

    testResults.push({
      criterion: "4.3",
      test: "Médias avec audiodescription",
      status: issues.length === 0 ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} vidéos avec audiodescription`,
      elements: issues,
    });

    if (issues.length > 0) {
      console.warn("Vidéos sans audiodescription détectée:", issues);
    }
  });

  test("4.13 - Contrôle de lecture des médias", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const mediaElements = page.locator("video, audio");
    const count = await mediaElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "4.13",
        test: "Contrôle de lecture des médias",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    for (let i = 0; i < count; i++) {
      const media = mediaElements.nth(i);

      // Vérifier la présence de l'attribut controls
      const hasControls = await media.getAttribute("controls");

      // Vérifier qu'il n'y a pas d'autoplay sans contrôles
      const hasAutoplay = await media.getAttribute("autoplay");

      if (hasControls || !hasAutoplay) {
        conformeCount++;
      } else {
        const identifier = (await media.getAttribute("src")) || `media-${i}`;
        issues.push(`${identifier}: autoplay sans contrôles`);
      }
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "4.13",
      test: "Contrôle de lecture des médias",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} médias avec contrôles appropriés`,
      elements: issues,
    });

    expect(issues.length).toBe(0);
  });
});
