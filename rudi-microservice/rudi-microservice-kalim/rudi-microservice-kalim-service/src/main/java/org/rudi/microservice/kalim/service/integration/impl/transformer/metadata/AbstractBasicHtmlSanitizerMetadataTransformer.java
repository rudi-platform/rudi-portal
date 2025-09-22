package org.rudi.microservice.kalim.service.integration.impl.transformer.metadata;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBasicHtmlSanitizerMetadataTransformer<T> extends AbstractMetadataTransformer<T> {

	private static final Safelist RUDI_SAFELIST_BASIC_EXTENDED = Safelist.basic()
			.addEnforcedAttribute("a", "target", "_blank")
			// autoriser les styles (définis comme safe par
			// https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html#safe-sinks)
			.addAttributes(":all", "align", "alink", "alt", "bgcolor", "border", "cellpadding", "cellspacing", "class",
					"color", "cols", "colspan", "coords", "dir", "face", "height", "hspace", "ismap", "lang",
					"marginheight", "marginwidth", "multiple", "nohref", "noresize", "noshade", "nowrap", "ref", "rel",
					"rev", "rows", "rowspan", "scrolling", "shape", "span", "summary", "tabindex", "title", "usemap",
					"valign", "value", "vlink", "vspace", "width");

	protected String cleanupText(String text) {
		log.debug("Cleaning up text with strict sanitizer : {}", text);
		if (StringUtils.isEmpty(text)) {
			return text;
		}
		String cleanedText = Jsoup.clean(text, Safelist.none());
		log.debug("Clean text: {}", cleanedText);
		return cleanedText;
	}

	protected String cleanupHtml(String html) {
		log.debug("Cleaning up html with basic sanitizer : {}", html);
		if (StringUtils.isEmpty(html)) {
			return html;
		}
		String cleanedHtml = Jsoup.clean(html, RUDI_SAFELIST_BASIC_EXTENDED);
		log.debug("Clean html: {}", cleanedHtml);
		return cleanedHtml;
	}

}
