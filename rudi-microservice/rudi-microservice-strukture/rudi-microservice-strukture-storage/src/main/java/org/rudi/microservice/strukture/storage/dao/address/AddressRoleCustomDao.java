package org.rudi.microservice.strukture.storage.dao.address;

import java.util.List;

import org.rudi.microservice.strukture.core.bean.criteria.AddressRoleSearchCriteria;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;

/**
 * Permet d'obtenir une liste de rôles pour les adresses
 */
public interface AddressRoleCustomDao {

	List<AddressRoleEntity> searchAddressRoles(AddressRoleSearchCriteria searchCriteria);
}
