/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.service.helper;

import java.util.HashSet;
import java.util.Locale;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * @author FNI18300
 *
 */
@AclSpringBootTest
class EmailHelperUT {

	@Autowired
	private EmailHelper emailHelper;

	@MockitoBean
	private JavaMailSenderImpl javaMailSender;

	@Test
	void accountCreationConfirmation() {

		assertNotNull(emailHelper);

		UserEntity user = new UserEntity();
		user.setLogin("user1");
		user.setAddresses(new HashSet<>());
		EmailAddressEntity emailAddress = new EmailAddressEntity();
		emailAddress.setEmail("user1@gmail.com");
		user.getAddresses().add(emailAddress);

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		emailHelper.sendAccountCreationConfirmation(user, Locale.FRENCH);

		UserEntity user2 = new UserEntity();
		user2.setLogin("user2@laposte.net");

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		emailHelper.sendAccountCreationConfirmation(user2, Locale.FRENCH);
	}

}
