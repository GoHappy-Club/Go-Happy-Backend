package com.startup.goHappy.entities.service;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.startup.goHappy.entities.model.Event;


public class BaseService<T> {
	
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
	ElasticsearchOperations operations;
	
	ElasticsearchRepository<T, String> repository;
	
//	@Autowired
//	BaseServiceAspectExt
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	public void setRepository(ElasticsearchRepository<T, String> repository) {
		this.repository = repository;
	}
	
	public T findById(String id) {
		Optional<T> t = this.repository.findById(id);
		if(t.isPresent())
			return t.get();
		return null;
	}
	
	public T save(T obj) {
		List<T> objs = new ArrayList<>();
		objs.add(obj);
		this.save(objs);
		return null;
	}
	
	public void save(List<T> objs) {
		Class<T> clazz = getGenericTypeClass();
		if(objs.size()>0) {
			this.repository.saveAll(objs);
			this.repository.refresh();
		}
		
	}

	
	public void delete(String id) {
		T obj = this.findById(id);
		this.repository.delete(obj);	
	}
	
	public void deleteAll() {
		this.repository.deleteAll();	
	}

	
	public Iterable<T> findAll() {
		return this.repository.findAll();
	}
	
	public long count(NativeSearchQuery qb) {
		return operations.count(qb, getGenericTypeClass());
	}
	
	@SuppressWarnings("deprecation")
	public Iterable<T> search(NativeSearchQuery qb) {
		return this.repository.search(qb);
	}
	
	public List<T> search(QueryBuilder qb) throws IOException{
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Class<T> clazz = getGenericTypeClass();
		List<T> rows = new ArrayList<>();
		List<SearchHit> sources = scanHits(qb);
		for(SearchHit source: sources) {
			T t = mapper.readValue(source.getSourceAsString(), clazz);
			rows.add(t);
		}
		return rows;
	}
	

	private List<SearchHit> scanHits(QueryBuilder qb) throws IOException {
		Class<T> clazz = getGenericTypeClass();
		Document doc = clazz.getAnnotation(Document.class);
		SearchRequest searchRequest = new SearchRequest(doc.indexName());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(qb);
		searchRequest.source(searchSourceBuilder);
//		
//		SearchResponse scrollResp = this.client.prepareSearch(doc.indexName()).setScroll(new TimeValue(60000))
//				.setQuery(qb).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setIndices(doc.indexName())
//				.setTypes(doc.indexStoreType()).setSize(1000).get();
		SearchResponse scrollResp = client.search(searchRequest,null);
		List<SearchHit> sources = new ArrayList<>();
		for(SearchHit hit: scrollResp.getHits().getHits()) {
			sources.add(hit);
		}
		return sources;
	}

	@SuppressWarnings("unchecked")
	public Class<T> getGenericTypeClass() {
		String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]
				.getTypeName();
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
			return (Class<T>) clazz;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	

}
