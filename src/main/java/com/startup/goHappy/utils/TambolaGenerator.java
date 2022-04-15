package com.startup.goHappy.utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;


@Service
public class TambolaGenerator {


    public  List<List<Integer>> generate() throws IOException
    {
    	List<List<Integer>> ticket = new ArrayList<>();
    	ticket.add(new ArrayList<>());
    	ticket.add(new ArrayList<>());
    	ticket.add(new ArrayList<>());
    	for(int i=0;i<3;i++) {
    		for(int j=0;j<9;j++) {
    			ticket.get(i).add(0);
    		}
    	}
    	Random r=new Random();
    	for(int i=0;i<3;i++) {
    		for(int j=0;j<5;j++) {
    			int randomIndex = ThreadLocalRandom.current().nextInt(0, 9);
    			while(ticket.get(i).get(randomIndex)!=0) {
    				randomIndex = ThreadLocalRandom.current().nextInt(0, 9);
    			}
    			int min = (randomIndex)*10;
    			int max = (randomIndex+1)*10;
    			
    			int randomNum = ThreadLocalRandom.current().nextInt(min, max);
    			while(ticket.get(0).contains(randomNum)||
    					ticket.get(1).contains(randomNum)||
    					ticket.get(2).contains(randomNum)) {
    				randomNum = ThreadLocalRandom.current().nextInt(min, max);
    			}
    			ticket.get(i).set(randomIndex, randomNum);
    		}
    		
    	}
//    	for(int i=0;i<3;i++) {
//    		for(int j=0;j<9;j++) {
//    			System.out.print(ticket.get(i).get(j)+"    |");
//    		}
//    		System.out.println();
//    	}
    	System.out.println("Ticket Generated");
    	return ticket;
    }
}
