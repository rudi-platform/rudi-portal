/**
 * RUDI Portail
 */
package org.rudi.microservice.konsult.core.customization;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ASO14058
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CmsDatasetsButtonDescriptionData extends AbstractCmsDescriptionData {

	private String assetType;

}
