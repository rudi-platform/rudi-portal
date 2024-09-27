package org.rudi.microservice.konsult.core.customization;

import java.util.List;

import lombok.Data;

@Data
public class FooterDescriptionData {
	private FooterLogoData footerLogo;
	private List<SocialNetworkData> socialNetworks;
}
