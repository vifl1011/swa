package de.shop.mail;

import static javax.ejb.TransactionAttributeType.SUPPORTS;

import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jboss.logging.Logger;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.service.NeuerKunde;
import de.shop.util.Config;
import de.shop.util.Log;


@ApplicationScoped
@Stateful
@Log
public class KundeObserver {
	private static final String NEWLINE = System.getProperty("line.separator");
	
	@Inject
	private Logger logger;
	
	@Resource(lookup = "java:jboss/mail/Default")
	private transient Session mailSession;
	
	@Inject
	private Config config;
	
	private String absenderMail;
	private String absenderName;
	private String empfaengerMail;
	private String empfaengerName;
	
	@PostConstruct
	// Attribute mit @Inject sind initialisiert
	private void postConstruct() {
		absenderMail = config.getAbsenderMail();
		absenderName = config.getAbsenderName();
		empfaengerMail = config.getEmpfaengerMail();
		empfaengerName = config.getEmpfaengerName();
		
		if (absenderMail == null || empfaengerMail == null) {
			logger.warn("Absender oder Empfaenger fuer Markteting-Emails sind nicht gesetzt.");
			return;
		}
		logger.infof("Absender fuer Markteting-Emails: %s", absenderMail);
		logger.infof("Empfaenger fuer Markteting-Emails: %s", empfaengerMail);
	}
	
	// Loose Kopplung durch @Observes, d.h. ohne JMS
	@Asynchronous
	@TransactionAttribute(SUPPORTS)
	public void onCreateKunde(@Observes @NeuerKunde Kunde kunde) {
		if (absenderMail == null || empfaengerMail == null) {
			return;
		}
		
		final MimeMessage message = new MimeMessage(mailSession);

		try {
			// Absender setzen
			final InternetAddress absenderObj = new InternetAddress(absenderMail, absenderName);
			message.setFrom(absenderObj);
			
			// Empfaenger setzen
			final InternetAddress empfaenger = new InternetAddress(empfaengerMail, empfaengerName);
			message.setRecipient(RecipientType.TO, empfaenger);   // RecipientType: TO, CC, BCC

			// Subject setzen
			final String subject = "Neuer Kunde";
			message.setSubject(subject);
			
			// HTML-Text setzen mit MIME Type "text/html"
			final Adresse adr = kunde.getAdresse();
			final String text = "<p><b>" + kunde.getVorname() + " " + kunde.getName()
					            + "</b></p>" + NEWLINE
			                    + "<p>" + adr.getPlz() + " " + adr.getOrt() + "</p>" + NEWLINE
			                    + "<p>" + adr.getStrasse() + " " + adr.getHausnummer() + "</p>" + NEWLINE;
			message.setContent(text, "text/html");
			
			// Hohe Prioritaet einstellen
			//message.setHeader("Importance", "high");
			//message.setHeader("Priority", "urgent");
			//message.setHeader("X-Priority", "1");
			
			// HTML-Text mit einem Bild als Attachment
			Transport.send(message);
		}
		catch (MessagingException | UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			return;
		}
	}
}
