package com.startup.goHappy.config.Firestore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;


@Configuration
public class FirestoreConfig {
	@Bean
	public Firestore getFireStore(@Value("${firebase.credential.path}") String credentialPath) throws IOException {
		FileInputStream serviceAccount = new FileInputStream(credentialPath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

		FirestoreOptions options = FirestoreOptions.newBuilder()
						.setCredentials(credentials).build();
//		FirebaseApp.initializeApp(options);
		return options.getService();
	}
}