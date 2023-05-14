package com.blogsite.Service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean flag = false;
		//smtp properties
		Properties properties = new Properties();
		properties.put("mail.smtp.auth",true);
		properties.put("mail.smtp.starttls.enable",true);
		properties.put("mail.smtp.port","587");
		properties.put("mail.smtp.host","smtp.gmail.com");
		
		String username = "kuilasukriti21749";
		String password = "lmxporcumzetwdta";
		String from = "kuilasukriti21749@gmail.com";
		//session
		Session session = Session.getInstance(properties, new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username,password);
		}
		});
		try {
			Message msg = new MimeMessage(session);
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			msg.setFrom(new InternetAddress(from));
			msg.setSubject(subject);
//			msg.setText(message);
			msg.setContent(message,"text/html");
			Transport.send(msg);
			flag = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

}
