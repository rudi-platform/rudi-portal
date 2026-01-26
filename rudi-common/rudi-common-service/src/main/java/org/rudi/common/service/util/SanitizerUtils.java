package org.rudi.common.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SanitizerUtils {

	private static final String STYLES_SEPARATOR = ";";
	private static final String STYLE_KEY_VALUE_SEPARATOR = ":";
	private static final String P_TAG = "p";
	private static final String STYLE_ATTR = "style";
	private static final List<String> RUDI_SAFE_P_STYLE = List.of("text-align");

	private static final Safelist RUDI_SAFELIST_BASIC_EXTENDED = Safelist.basic()
			.addEnforcedAttribute("a", "target", "_blank")
			.removeTags("hr", "blockquote", "code")
			.removeProtocols("a", "href", "ftp")
			// autoriser les styles (définis comme safe par
			// https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html#safe-sinks)
			.addAttributes(":all", "align", "alink", "alt", "bgcolor", "border", "cellpadding", "cellspacing", "class",
					"color", "cols", "colspan", "coords", "dir", "face", "height", "hspace", "ismap", "lang",
					"marginheight", "marginwidth", "multiple", "nohref", "noresize", "noshade", "nowrap", "ref", "rel",
					"rev", "rows", "rowspan", "scrolling", "shape", "span", "summary", "tabindex", "title", "usemap",
					"valign", "value", "vlink", "vspace", "width")
			.addAttributes(P_TAG, STYLE_ATTR);

	public String cleanupText(String text) {
		log.debug("Cleaning up text with strict sanitizer : {}", text);
		if (StringUtils.isEmpty(text)) {
			return text;
		}

		String cleanedText = Jsoup.clean(text, Safelist.none());
		log.debug("Clean text: {}", cleanedText);
		return cleanedText;
	}

	public String cleanupHtml(String html) {
		log.debug("Cleaning up html with basic sanitizer : {}", html);
		if (StringUtils.isEmpty(html)) {
			return html;
		}
		String cleanedHtml = Jsoup.clean(html, RUDI_SAFELIST_BASIC_EXTENDED);

		// post-traitement pour n'autoriser que certains styles sur les <p>
		Document doc = Jsoup.parse(cleanedHtml);
		removeUnappropriatePStyles(doc);
		cleanedHtml = doc.body().html();
		log.debug("Clean html: {}", cleanedHtml);
		return cleanedHtml;
	}

	private void removeUnappropriatePStyles(Document doc) {
		doc.select(P_TAG + "[" + STYLE_ATTR + "]").forEach(element -> {
			String style = element.attr(STYLE_ATTR);

			// retrouver tous les styles définis dans l'attribut style
			Map<String, String> styles = new HashMap<>();
			List.of(style.split(STYLES_SEPARATOR)).forEach(s -> {
				String[] keyValue = s.split(STYLE_KEY_VALUE_SEPARATOR);
				if (keyValue.length == 2) {
					styles.put(keyValue[0].trim(), keyValue[1].trim());
				}
			});
			// enlever tous les styles
			element.removeAttr(STYLE_ATTR);
			// reconstruire l'attribut style uniquement avec les styles autorisés
			StringBuilder allowedStyles = new StringBuilder();
			RUDI_SAFE_P_STYLE.forEach(key -> {
				if (styles.containsKey(key)) {
					allowedStyles.append(key).append(STYLE_KEY_VALUE_SEPARATOR).append(styles.get(key))
							.append(STYLES_SEPARATOR);
				}
			});
			if (StringUtils.isNotEmpty(allowedStyles.toString())) {
				element.attr(STYLE_ATTR, allowedStyles.toString());
			}

		});
	}

}
