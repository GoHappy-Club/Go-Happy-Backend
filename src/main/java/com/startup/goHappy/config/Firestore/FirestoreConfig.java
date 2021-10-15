package com.startup.goHappy.config.Firestore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
import com.google.rpc.context.AttributeContext.Resource;


@Configuration
public class FirestoreConfig {
	
	String PROJECT_ID = "go-happy-322816";
	String PATH_TO_JSON_KEY = "gohappy-main-bucket/config/go-happy-322816-99b559058469.json";
	String BUCKET_NAME = "gohappy-main-bucket";
	String OBJECT_NAME = "go-happy-322816-99b559058469.json";
	  @Value("gs://gohappy-main-bucket/config/go-happy-322816-99b559058469.json")
	  private Resource gcsFile;
	@Bean
	public Firestore getFireStore() throws IOException {
//		FileInputStream serviceAccount = new FileInputStream(credentialPath);
		System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk"+gcsFile);
	Storage storage = StorageOptions.newBuilder()
	            .setProjectId(PROJECT_ID)
	            .setCredentials(GoogleCredentials.fromStream(
	                    new FileInputStream(PATH_TO_JSON_KEY))).build().getService();

		Blob blob = storage.get(BUCKET_NAME, OBJECT_NAME);
		ReadChannel r = blob.reader();
		
		
		GoogleCredentials credentials = GoogleCredentials.fromStream(java.nio.channels.Channels.newInputStream(r));

		FirestoreOptions options1 = FirestoreOptions.newBuilder()
						.setCredentials(credentials).build();
//		FirebaseApp.initializeApp(options);
		return options1.getService();
	}
}