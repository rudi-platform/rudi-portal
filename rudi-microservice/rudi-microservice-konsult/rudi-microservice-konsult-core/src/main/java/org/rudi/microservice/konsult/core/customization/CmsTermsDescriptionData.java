/**
 * RUDI Portail
 */
package org.rudi.microservice.konsult.core.customization;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author FNI18300
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CmsTermsDescriptionData extends AbstractCmsDescriptionData {

	// Doit rester pour la page d'inscription
	private String cguCategory;

	private List<String> termsCategories;

}
