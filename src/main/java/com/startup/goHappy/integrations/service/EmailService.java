package com.startup.goHappy.integrations.service;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailRequest;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Component
public class EmailService {
	
	@Autowired
    private JavaMailSender emailSender;
	
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String user = "me";
	private static Gmail service = null;
	private static String filePath = "";
	
	public static Gmail getGmailService() throws IOException, GeneralSecurityException {

		final String sacc = "{\"web\":{\"client_id\":\"908368396731-209h63dhdr8tsjp7in4k88h1dc2jhc4c.apps.googleusercontent.com\",\"project_id\":\"go-happy-322816\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
	    InputStream in = new ByteArrayInputStream(sacc.getBytes
	                (Charset.forName("UTF-8")));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		
		// Credential builder

		@SuppressWarnings("deprecation")
		Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
				.setJsonFactory(JSON_FACTORY)
				.setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
						clientSecrets.getDetails().getClientSecret().toString())
				.build().setAccessToken(getAccessToken()).setRefreshToken(
						"1//0gg7-7T8HHhi5CgYIARAAGBASNwF-L9IrZzFUGSTlNNmFo8IlGOiSOR3eG5wJzGfn83TS8KZtm3NmXXhEaNNdhi5eC1A8RSRc5_s");//Replace this

		// Create Gmail service
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
				.setApplicationName(APPLICATION_NAME).build();

		return service;
	}
	
	private static String getAccessToken() {

		try {
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("grant_type", "refresh_token");
			params.put("client_id", "908368396731-209h63dhdr8tsjp7in4k88h1dc2jhc4c.apps.googleusercontent.com"); //Replace this
			params.put("client_secret", "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"); //Replace this
			params.put("refresh_token",
					"1//0gg7-7T8HHhi5CgYIARAAGBASNwF-L9IrZzFUGSTlNNmFo8IlGOiSOR3eG5wJzGfn83TS8KZtm3NmXXhEaNNdhi5eC1A8RSRc5_s"); //Replace this

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			URL url = new URL("https://accounts.google.com/o/oauth2/token");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.getOutputStream().write(postDataBytes);

			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				buffer.append(line);
			}

			JSONObject json = new JSONObject(buffer.toString());
			String accessToken = json.getString("access_token");
			return accessToken;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
    public void sendSimpleMessage(String to, String subject, String text) throws MessagingException, IOException, GeneralSecurityException {
        
    	sendEmail(to,subject,text);
//    	MimeMessage msg = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
//		
//        helper.setTo(to);
//        helper.setFrom("GoHappy-Club");
//        helper.setSubject(subject);
//        helper.setText(text, true);
//
//        emailSender.send(msg);      
    }
    
    
    public static void sendMessage(Gmail service, String userId, MimeMessage email)
			throws MessagingException, IOException {
		Message message = createMessageWithEmail(email);
		message = service.users().messages().send(userId, message).execute();

		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}

	public static Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		email.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from)); //me
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to)); //
		email.setSubject(subject); 

//        email.setText(bodyText,"utf-8");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyText, "text/html; charset=utf-8");
        Multipart multiPart = new MimeMultipart("alternative");
        multiPart.addBodyPart(htmlPart);
        email.setContent(multiPart);

		return email;
	}
	
	public static void sendEmail(String to, String subject, String text) throws IOException, GeneralSecurityException, MessagingException {
		
		Gmail service = getGmailService();
		MimeMessage Mimemessage = createEmail(to,"me",subject,text);
	
		Message message = createMessageWithEmail(Mimemessage);
		
		message = service.users().messages().send("me", message).execute();
		
		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}
    
    
	void sendEmailWithAttachment() throws MessagingException{

        MimeMessage msg = emailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		
        helper.setTo("to_@email");

        helper.setSubject("Testing from Spring Boot");

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText("<h1>Check attachment for image!</h1>", true);

		// hard coded a file path
        //FileSystemResource file = new FileSystemResource(new File("path/android.png"));

        helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

        emailSender.send(msg);

    }
}


	