package com.startup.goHappy.entities.service;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.firebase.database.annotations.Nullable;
import com.startup.goHappy.entities.model.UserProfile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFirestoreRepository<T> {
    private final CollectionReference collectionReference;
    private final String collectionName;
    private final Class<T> parameterizedType;

    protected AbstractFirestoreRepository(Firestore firestore, String collection) {
        this.collectionReference = firestore.collection(collection);
        this.collectionName = collection;
        this.parameterizedType = getParameterizedType();
    }
    private Class<T> getParameterizedType(){
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>)type.getActualTypeArguments()[0];
    }

    public boolean save(T model){
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = collectionReference.document(documentId).set(model);

        try {
            log.info("{}-{} saved at{}", collectionName, documentId, resultApiFuture.get().getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving {}={} {}", collectionName, documentId, e.getMessage());
        }

        return false;

    }

    public void delete(T model){
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = collectionReference.document(documentId).delete();

    }

    public List<T> retrieveAll(){
        ApiFuture<QuerySnapshot> querySnapshotApiFuture = collectionReference.get();

        try {
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshotApiFuture.get().getDocuments();

            return queryDocumentSnapshots.stream()
                    .map(queryDocumentSnapshot -> queryDocumentSnapshot.toObject(parameterizedType))
                    .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred while retrieving all document for {}", collectionName);
        }
        return Collections.<T>emptyList();

    }


    public Optional<T> get(String documentId){
        DocumentReference documentReference = collectionReference.document(documentId);
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = documentReference.get();

        try {
            DocumentSnapshot documentSnapshot = documentSnapshotApiFuture.get();

            if(documentSnapshot.exists()){
                return Optional.ofNullable(documentSnapshot.toObject(parameterizedType));
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred retrieving: {} {}, {}", collectionName, documentId, e.getMessage());
        }

        return Optional.empty();

    }

    
    public Optional<T> findById(String id){
        Query query = collectionReference.whereEqualTo("id", id);
        
    	
        try {
          
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			UserProfile user = null;
			for (DocumentSnapshot documentSnapshot : querySnapshot.get().getDocuments()) {
				 if(documentSnapshot.exists()){
		                return Optional.ofNullable(documentSnapshot.toObject(parameterizedType));
		            }
			} 

        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred retrieving: {} {}, {}", collectionName, id, e.getMessage());
        }

        return Optional.empty();

    }
    

    protected String getDocumentId(T t) {
        Object key;
        Class clzz = t.getClass();
        do{
            key = getKeyFromFields(clzz, t);
            clzz = clzz.getSuperclass();
        } while(key == null && clzz != null);

        if(key==null){
            return UUID.randomUUID().toString();
        }
        return String.valueOf(key);
    }

    private Object getKeyFromFields(Class<?> clazz, Object t) {

		return Arrays.stream(clazz.getDeclaredFields())
						.filter(field -> field.isAnnotationPresent(com.startup.goHappy.entities.model.DocumentId.class))
						.findFirst()
						.map(field -> getValue(t, field))
						.orElse(null);
	  }
    
    @Nullable
	  private Object getValue(Object t, java.lang.reflect.Field field) {
		  field.setAccessible(true);
      try {
        return field.get(t);
      } catch (IllegalAccessException e) {
        log.error("Error in getting documentId key", e);
	      }
			  return null;
		  }
	  
	    public CollectionReference getCollectionReference(){
	        return this.collectionReference;
	    }
	    protected Class<T> getType(){ return this.parameterizedType; }
}
	
