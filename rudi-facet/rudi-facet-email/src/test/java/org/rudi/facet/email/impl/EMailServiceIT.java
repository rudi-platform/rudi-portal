/**
 * 
 */
package org.rudi.facet.email.impl;

import java.io.File;
import java.net.URL;

import jakarta.mail.internet.MimeMessage;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.DocumentContent;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.email.SpringBootTestApplication;
import org.rudi.facet.email.exception.EMailException;
import org.rudi.facet.email.model.EMailDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * @author fni18300
 *
 */

//@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { SpringBootTestApplication.class })
class EMailServiceIT {

	@Autowired
	private EMailService emailService;

	@MockitoBean
	private JavaMailSenderImpl javaMailSender;

	@Test
	void testEmail() throws EMailException {

		URL url = Thread.currentThread().getContextClassLoader().getResource("body-test.html");
		File f = new File(url.getFile());
		DocumentContent body = new DocumentContent("test.html", "text/html", f);
		DocumentContent attachement = new DocumentContent("test.html", "text/html", f);
		EMailDescription eMailDescription = new EMailDescription(emailService.getDefaultFrom(), "test@rudi.com",
				"sujet", body);
		eMailDescription.addCc("test2@rudi.com");
		eMailDescription.addBcc("test3@rudi.com");
		eMailDescription.addTo("test4@rudi.com");
		eMailDescription.addAttachment(attachement);
		Assert.assertEquals(eMailDescription.getFrom(), emailService.getDefaultFrom());
		Assert.assertEquals(2, eMailDescription.getTos().size());
		Assert.assertEquals(1, eMailDescription.getBccs().size());
		Assert.assertEquals(1, eMailDescription.getCcs().size());
		Assert.assertEquals(1, eMailDescription.getAttachments().size());
		Assert.assertNotNull(eMailDescription.getSubject());
		Assert.assertNotNull(eMailDescription.getBody());

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		emailService.sendMail(eMailDescription);

	}
}
