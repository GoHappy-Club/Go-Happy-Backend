package com.startup.goHappy.config.Firestore;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

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
	
	String PROJECT_ID = "go-happy-322816";
	String PATH_TO_JSON_KEY = "gohappy-main-bucket/config/go-happy-322816-99b559058469.json";
	String BUCKET_NAME = "gohappy-main-bucket";
	String OBJECT_NAME = "go-happy-322816-99b559058469.json";
//	@Value("gs://gohappy-main-bucket/config/go-happy-322816-99b559058469.json")
//	  private Resource gcsFile;
	@Bean
	public Firestore getFireStore() throws IOException {
//		URL url = new URL("https://storage.googleapis.com/gohappy-main-bucket/config/go-happy-322816-99b559058469.json");
//		 InputStream in = url.openStream();
//		System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk"+
//				StreamUtils.copyToString(
//				        gcsFile.getInputStream(),
//				        Charset.defaultCharset()));
//		InputStream serviceAccount = getClass().getResourceAsStream("./go-happy-322816-99b559058469.json");
//	
		final String sacc = "{\n"
				+ "  \"type\": \"service_account\",\n"
				+ "  \"project_id\": \"go-happy-322816\",\n"
				+ "  \"private_key_id\": \"99b5590584694224e4c4aebea48dc74edef8e341\",\n"
				+ "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC/Z+E687jRa3IT\\nt23zNAnqpWck0d+LKRIMKSiKAqF+udcVEp7rI1woDx5qfHYyaHX6Q7xGj8jcs2dA\\nR2I1zmFjrP/k7/cHURvLDpvGXkC584gQkLVuISLbqUX24giSbQ8c/i38JHXZ2wYk\\nO9O/k0ANGAOOWjT75EKR8ju24ppIiiyKoEOD9dQc/W9xAe9pets3LC5PB5h3S2/R\\nSoxaZJrgCPsJyLXGq0TAiOFOlfl1r6Tj83RgvyO0HBcvLJaKlIN/0IQpxXuf0HvA\\n8wgOGQ2kEChRgVyhcxiEOgbRcyuIEM1xcBD0P9Gf2lYP+gq88pjgfLQGcbFyITZX\\nUDC9yrELAgMBAAECggEAKSZCmEN0fOVeGZoKU8pgxWb45UQPjKHwRNgGZKFLKez3\\nLgsaSlAITH5vmhIW8SBfGMY+MGdSFgylyn5uQUYUzvKO8x+Z2qNX19BUshFqVe/i\\n1TjfIIWUMmhSZ9DRafVzVdbZeQc3pPX+/IQ2Mk4Ma6Q5g32mPW8l1q2eo+NUqvpW\\nifpskFY95wfyZrXwvxjDu1sx5Vx4Je+8EQ6in9ZyfJfSauztcOQhC0tMC9rLQFuO\\nzqfnyLEYjUjyPvzQ2o3DNaZUmJykoq/Utn/OgogLePEXM5vnd5+bAp2nVyHjOiI1\\n+HQ/1YGSKoUeWlioIOu82ldBAfWtWNG1J9cMkX9nbQKBgQDeN6zbHvMMsUwf4Kfh\\n45x9IjLemzC5hJvZlHsHvIfpdWDI00aj3bLS34re9et7r9TQ3SjWUNaYJfB09HaG\\n+KlyQ+3NoAGiliZt32kZAESpopq04P6QBMNTxAS4S1dxK3J0MIlm0rtv6S5s6b8q\\nEDPBa2wms0HtHoOCL61IsqMTlwKBgQDcgRDZzeFHcDzdAtKnb8PhIK973NK/CO10\\nQ6XVccW4h37eiaCm2WXc9Bdgc+xYTfLBKaMG7Q1hJlctLqeQfEIMntYW/gcWvqI5\\nl0neJnLK9GTLwhJ5QjpmXXHSPSdEqLRnk8S1ov7AI5xsanvX5cFA0jV+VxucfWxW\\nZidyKJusrQKBgQCbLD2kdaQ7RMNnrzUMZCiuqObk03He2l8KPMj8ygYOXEzNb9tw\\nKivpXPhYYVmi8VGm2AjgP2rarAORZ/QspA+PHyg74nPB2R/UsaFuN+W6nzVxAXxB\\njVHoeITjPJk/CZvOzuXjy1vf9fDZVRCMiuvZMO5AyARNVHV4v1o93aBK2QKBgF+Z\\n3bJw3qFYeirgVHCftm9e+nZbUUveFQV5ZubZwv20UT8usGZmjxFzCseYuvm2Ie+5\\nm6MfF9PtCZLfTWsJVKGgI/YoAO7NjAwoGbClPtPbjfABnnzQR/2lufmK5gGQm1bk\\n7D5MAuNPDTy8FiGOzXDLMsIqBiCdCGGVPLjz/jqpAoGBAMmkq91zWNoZAWQVRf9p\\n9/dYrK1niTP/G804VzncQJWb/DucGeYIStWWC6WTp+gyAUD5CZc2hqmxytpTXZGz\\njG5YpcB3hGFFFd1rlMCsjrJlxgg6E36QUbf3/a+YwGyKooXSIU0KU12n+OpsmLyk\\nB1LJgWmE4vOedhxQrAQSRuVq\\n-----END PRIVATE KEY-----\\n\",\n"
				+ "  \"client_email\": \"firebase-adminsdk-cbd7l@go-happy-322816.iam.gserviceaccount.com\",\n"
				+ "  \"client_id\": \"111819960328342292149\",\n"
				+ "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n"
				+ "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n"
				+ "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n"
				+ "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-cbd7l%40go-happy-322816.iam.gserviceaccount.com\"\n"
				+ "}\n"
				+ "";
		   InputStream serviceAccount = new ByteArrayInputStream(sacc.getBytes
	                (Charset.forName("UTF-8")));
//		FileInputStream serviceAccount = new FileInputStream("./go-happy-322816-99b559058469.json");
//	Storage storage = StorageOptions.newBuilder()
//	            .setProjectId(PROJECT_ID)
//	            .setCredentials(GoogleCredentials.fromStream(
//	                    new FileInputStream(PATH_TO_JSON_KEY))).build().getService();
//
//		Blob blob = storage.get(BUCKET_NAME, OBJECT_NAME);
//		ReadChannel r = blob.reader();x
		
		
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

		FirestoreOptions options1 = FirestoreOptions.newBuilder()
						.setCredentials(credentials).build();
//		FirebaseApp.initializeApp(options);
		return options1.getService();
	}
}