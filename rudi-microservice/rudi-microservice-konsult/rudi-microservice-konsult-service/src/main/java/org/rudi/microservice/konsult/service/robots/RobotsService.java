package org.rudi.microservice.konsult.service.robots;

import java.io.IOException;

import org.rudi.common.core.DocumentContent;

public interface RobotsService {

	/**
	 * @param resourceName nom de la ressource demandée sous forme d'un string
	 * @return la ressource demandée sous forme d'un DocumentContent
	 */
	DocumentContent getRobotsRessourceByName(String resourceName) throws IOException;

}
