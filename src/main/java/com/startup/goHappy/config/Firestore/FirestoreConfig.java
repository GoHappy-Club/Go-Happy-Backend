package com.startup.goHappy.config.Firestore;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import com.google.api.services.storage.Storage.Channels;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;


@Configuration
public class FirestoreConfig {

	@Bean
	public Firestore getFireStore() throws IOException {
		// Load environment variables from .env file
		Dotenv dotenv = Dotenv.load();
		String serviceAccountPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");

		FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		FirestoreOptions options1 = FirestoreOptions.newBuilder()
				.setCredentials(credentials).build();
		return options1.getService();
	}
}