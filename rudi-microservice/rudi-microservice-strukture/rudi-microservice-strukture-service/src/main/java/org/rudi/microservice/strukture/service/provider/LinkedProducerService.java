package org.rudi.microservice.strukture.service.provider;

import java.util.UUID;

import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.springframework.stereotype.Service;

@Service
public interface LinkedProducerService {

	LinkedProducer createLinkedProducer(UUID organizationUuid) throws AppServiceNotFoundException, AppServiceUnauthorizedException, AppServiceBadRequestException;

	LinkedProducer getLinkedProducer(UUID linkedProducerUuid);

	OwnerInfo getLinkedProducerOwnerInfo(UUID uuid) throws AppServiceBadRequestException, IllegalArgumentException;
}
