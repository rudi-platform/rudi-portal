/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.service.helper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.service.datafactory.UserDataFactory;
import org.rudi.microservice.acl.storage.entity.accountregistration.AccountRegistrationEntity;
import org.rudi.microservice.acl.storage.entity.accountupdate.ResetPasswordRequestEntity;
import org.rudi.microservice.acl.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author FNI18300
 *
 */
@AclSpringBootTest
class EmailHelperUT {

	@Autowired
	private EmailHelper emailHelper;

	@Autowired
	private UserDataFactory userDataFactory;

	@MockitoBean
	private JavaMailSenderImpl javaMailSender;

	@BeforeEach
	void setUp() {
		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send(any(MimeMessage.class));
	}

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

	@Test
	@DisplayName("sendAccountRegistration : envoie un courriel de confirmation d'adresse")
	void test_sendAccountRegistration() throws Exception {
		AccountRegistrationEntity account = new AccountRegistrationEntity();
		account.setLogin("registration@example.com");
		account.setToken("registration-token");

		emailHelper.sendAccountRegistration(account, Locale.FRENCH);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@Test
	@Transactional
	@DisplayName("lookupUserByEmail : retrouve l'utilisateur dont le login est l'adresse courriel")
	void test_lookupUserByEmail() {
		String login = "lookup-user@example.com";
		userDataFactory.getOrCreateActiveUserEntity(login);

		User user = emailHelper.lookupUserByEmail(login);

		assertThat(user).isNotNull();
		assertThat(user.getLogin()).isEqualTo(login);
	}

	@Test
	@DisplayName("sendTokenToChangePassword : envoie un courriel de réinitialisation de mot de passe")
	void test_sendTokenToChangePassword() {
		ResetPasswordRequestEntity resetPasswordRequest = new ResetPasswordRequestEntity();
		resetPasswordRequest.setUserUuid(UUID.randomUUID());
		resetPasswordRequest.setToken(UUID.randomUUID());
		resetPasswordRequest.setCreationDate(LocalDateTime.now());

		emailHelper.sendTokenToChangePassword(resetPasswordRequest, "reset-password@example.com", Locale.FRENCH);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@Test
	@DisplayName("sendPasswordChangeConfirmation : envoie un courriel de confirmation de changement de mot de passe")
	void test_sendPasswordChangeConfirmation() {
		UserEntity user = new UserEntity();
		user.setLogin("password-change@example.com");

		emailHelper.sendPasswordChangeConfirmation(user, Locale.FRENCH);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@Test
	@DisplayName("sendAccountDeletionWarning : envoie un courriel d'avertissement à l'utilisateur nommé")
	void test_sendAccountDeletionWarning() {
		UserEntity user = new UserEntity();
		user.setLogin("deletion-warning@example.com");
		user.setFirstname("Prénom");
		user.setLastname("Nom");

		emailHelper.sendAccountDeletionWarning(user, Locale.FRENCH, true, 24);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@Test
	@DisplayName("sendAccountDeletedToUser : envoie un courriel de suppression de compte")
	void test_sendAccountDeletedToUser() {
		emailHelper.sendAccountDeletedToUser("deleted-user@example.com", Locale.FRENCH);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@Test
	@Transactional
	@DisplayName("lookupModeratorEmails : retourne l'adresse des utilisateurs ayant le rôle MODERATOR")
	void test_lookupModeratorEmails() {
		String login = "moderator-lookup@example.com";
		userDataFactory.getOrCreateModeratorUserEntity(login, 0, false);

		List<String> emails = emailHelper.lookupModeratorEmails();

		assertThat(emails).contains(login);
	}

	@Test
	@DisplayName("sendAccountDeletedToAnimators : envoie un courriel à chaque destinataire renseigné")
	void test_sendAccountDeletedToAnimators() {
		emailHelper.sendAccountDeletedToAnimators(List.of("animator@example.com", ""), UUID.randomUUID(), Locale.FRENCH);

		verify(javaMailSender, times(1)).send(any(MimeMessage.class));
	}

}
