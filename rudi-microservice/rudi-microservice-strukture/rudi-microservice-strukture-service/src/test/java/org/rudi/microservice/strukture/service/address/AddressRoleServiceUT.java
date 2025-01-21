package org.rudi.microservice.strukture.service.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.microservice.strukture.core.bean.AddressRole;
import org.rudi.microservice.strukture.core.bean.AddressType;
import org.rudi.microservice.strukture.core.bean.criteria.AddressRoleSearchCriteria;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Class de test du service AddressRoleService
 */
@StruktureSpringBootTest
class AddressRoleServiceUT {

	@Autowired
	private AddressRoleService addressRoleService;

	@Autowired
	private AddressRoleDao addressRoleDao;

	@MockitoBean
	DatasetService datasetService;

	private AddressRole roleSiteWebProjet;
	private AddressRole roleMailHotline;

	private List<AddressRole> addedAddressRoles;

	@BeforeEach
	public void initData() {
		addedAddressRoles = new ArrayList<AddressRole>();
		roleSiteWebProjet = addressRoleService.createAddressRole(initRoleAdresseSiteWeb());
		roleMailHotline = addressRoleService.createAddressRole(initRoleAdresseMail());

		addedAddressRoles.add(roleSiteWebProjet);
		addedAddressRoles.add(roleMailHotline);
	}

	@AfterEach
	public void cleanData() {
		for (AddressRole role : addedAddressRoles) {
			AddressRoleEntity entity = addressRoleDao.findByUUID(role.getUuid());
			addressRoleDao.delete(entity);
		}
	}

	private AddressRole initRoleTelephonePro() {
		AddressRole addressRole = new AddressRole();
		addressRole.setCode("PRO");
		addressRole.setLabel("téléphone professionnel");
		addressRole.setType(AddressType.PHONE);
		addressRole.setOrder(Integer.valueOf(1));
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime openingDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,
				0);
		addressRole.setOpeningDate(openingDate);

		LocalDateTime closingDate = LocalDateTime.of(openingDate.getYear() + 10, openingDate.getMonthValue(),
				openingDate.getDayOfMonth(), 0, 0);
		addressRole.setClosingDate(closingDate);

		return addressRole;
	}

	private AddressRole initRoleAdresseSiteWeb() {
		AddressRole addressRole = new AddressRole();
		addressRole.setCode("PROJET");
		addressRole.setLabel("site web projet");
		addressRole.setType(AddressType.WEBSITE);
		addressRole.setOrder(Integer.valueOf(1));
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime openingDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,
				0);
		addressRole.setOpeningDate(openingDate);

		LocalDateTime closingDate = LocalDateTime.of(openingDate.getYear() + 10, openingDate.getMonthValue(),
				openingDate.getDayOfMonth(), 0, 0);
		addressRole.setClosingDate(closingDate);

		return addressRole;
	}

	private AddressRole initRoleAdresseMail() {
		AddressRole addressRole = new AddressRole();
		addressRole.setCode("HOTLINE");
		addressRole.setLabel("mail hotline");
		addressRole.setType(AddressType.EMAIL);
		addressRole.setOrder(Integer.valueOf(1));
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime openingDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,
				0);
		addressRole.setOpeningDate(openingDate);

		LocalDateTime closingDate = LocalDateTime.of(openingDate.getYear() + 10, openingDate.getMonthValue(),
				openingDate.getDayOfMonth(), 0, 0);
		addressRole.setClosingDate(closingDate);

		return addressRole;
	}

	@Test
	void testCRUDAddressRole() {
		AddressRoleSearchCriteria criteria = new AddressRoleSearchCriteria();
		criteria.setType(AddressType.PHONE);
		criteria.setActive(true);
		int initialCountPhone = addressRoleService.searchAddressRoles(criteria).size();

		criteria.setType(AddressType.EMAIL);
		criteria.setActive(true);
		int initialCountEmail = addressRoleService.searchAddressRoles(criteria).size();

		assertNotNull(addressRoleService);
		assertNotNull(roleMailHotline);
		assertNotNull(roleSiteWebProjet);

		long nbAddressRole = addressRoleDao.count();

		// création d'un role d'adresse
		// ----------------------------
		AddressRole telephonePro = initRoleTelephonePro();
		LocalDateTime openingDate = telephonePro.getOpeningDate();
		LocalDateTime closingDate = telephonePro.getClosingDate();

		AddressRole addressRoleCree = addressRoleService.createAddressRole(telephonePro);

		assertEquals("PRO", addressRoleCree.getCode());
		assertEquals("téléphone professionnel", addressRoleCree.getLabel());
		assertEquals(AddressType.PHONE, addressRoleCree.getType());
		assertEquals(Integer.valueOf(1), addressRoleCree.getOrder());
		assertEquals(openingDate, addressRoleCree.getOpeningDate());
		assertEquals(closingDate, addressRoleCree.getClosingDate());

		assertEquals(nbAddressRole + 1, addressRoleDao.count());

		// chargement d'un role d'adresse
		// -------------------------------
		AddressRole addresseRoleChargee = addressRoleService.getAddressRole(addressRoleCree.getUuid());
		assertEquals("PRO", addresseRoleChargee.getCode());
		assertEquals("téléphone professionnel", addresseRoleChargee.getLabel());

		// modification d'un role d'adresse
		// --------------------------------
		addresseRoleChargee.setCode("PRO1");
		addresseRoleChargee.setClosingDate(null);

		AddressRole updatedAddressRole = addressRoleService.updateAddressRole(addresseRoleChargee);
		assertEquals("PRO1", updatedAddressRole.getCode());
		assertNull(updatedAddressRole.getClosingDate());

		// recherche
		// ---------

		// recherche sans critères
		List<AddressRole> addressRoles = addressRoleService.searchAddressRoles(new AddressRoleSearchCriteria());
		assertEquals(nbAddressRole + 1, addressRoles.size());

		// recherche tous critères
		criteria = new AddressRoleSearchCriteria();
		criteria.setType(AddressType.PHONE);
		criteria.setActive(true);
		addressRoles = addressRoleService.searchAddressRoles(criteria);
		assertEquals(1 + initialCountPhone, addressRoles.size());
		assertEquals(updatedAddressRole.getUuid(), addressRoles.get(0).getUuid());

		// recherche par type
		criteria = new AddressRoleSearchCriteria();
		criteria.setType(AddressType.EMAIL);
		addressRoles = addressRoleService.searchAddressRoles(criteria);
		assertThat(addressRoles).as("La taille doit être la même qu'à létat initial")
				.matches(a -> initialCountEmail == a.size()).as("L'uuid du role mail hotline doit être présent")
				.anyMatch(a -> a.getUuid().equals(roleMailHotline.getUuid()));
		assertEquals(initialCountEmail, addressRoles.size());

		// suppression d'un role d'adresse
		// --------------------------------
		addressRoleService.deleteAddressRole(updatedAddressRole.getUuid());
		assertEquals(nbAddressRole, addressRoleDao.count());

	}
}
