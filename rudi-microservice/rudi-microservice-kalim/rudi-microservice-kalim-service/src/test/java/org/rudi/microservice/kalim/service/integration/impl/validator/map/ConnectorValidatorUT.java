package org.rudi.microservice.kalim.service.integration.impl.validator.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.facet.kaccess.bean.Connector;
import org.rudi.microservice.kalim.service.KalimSpringBootTest;
import org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract.map.parameter.ConnectorValidator;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;
import lombok.val;

@KalimSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConnectorValidatorUT extends AbstractValidatorUT {
	private final ConnectorValidator connectorValidator;

	@Test
	@DisplayName("Media Connector : URL externe valide")
	void validate_external_url_ok() throws IOException {
		Connector connector = new Connector();
		connector.setInterfaceContract("dwnl");
		connector.setUrl("https://www.google.com");
		val errors = connectorValidator.validate(connector);

		assertThat(errors.size()).as("Il n'y a pas d'erreur d'url sur le champ connector").isEqualTo(0);
	}

	@Test
	@DisplayName("Media Connector : URL interne valide")
	void validate_internal_url_ok() throws IOException {
		Connector connector = new Connector();
		connector.setInterfaceContract("dwnl");
		connector.setUrl("https://localhost:28001/");
		val errors = connectorValidator.validate(connector);

		assertThat(errors.size()).as("Il n'y a pas d'erreur d'url sur le champ connector").isEqualTo(0);
	}

	@Test
	@DisplayName("Media Connector : URL vide")
	void validate_url_empty_nok() throws IOException {
		Connector connector = new Connector();
		connector.setInterfaceContract("dwnl");
		connector.setUrl("   ");
		val errors = connectorValidator.validate(connector);

		assertThat(errors.size()).as("L'URL du connecteur est obligatoire").isEqualTo(1);
	}

	@Test
	@DisplayName("Media Connector : URL incomplète")
	void validate_url_incomplète_nok() throws IOException {
		Connector connector = new Connector();
		connector.setInterfaceContract("dwnl");
		connector.setUrl("/medias/20124654");
		val errors = connectorValidator.validate(connector);

		assertThat(errors.size()).as("L'URL du connecteur doit être valide").isEqualTo(1);
	}
}
