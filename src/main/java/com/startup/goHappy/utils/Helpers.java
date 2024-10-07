package com.startup.goHappy.utils;

import org.springframework.stereotype.Service;

@Service
public class Helpers {
    public boolean matches(String fullText, String searchText) {
        String[] searchWords = searchText.toLowerCase().split("\\s+");

        int searchWordIndex = 0;
        for (String searchWord : searchWords) {
            if(fullText.contains(searchWord)){ return true; }
            if (isPartialMatch(fullText, searchWord)) {
                searchWordIndex++;
            }
        }

        return searchWordIndex == searchWords.length;
    }

    private boolean isPartialMatch(String fullWord, String partialWord) {
        int fullIndex = 0;
        int partialIndex = 0;

        while (fullIndex < fullWord.length() && partialIndex < partialWord.length()) {
            if (fullWord.charAt(fullIndex) == partialWord.charAt(partialIndex)) {
                partialIndex++;
            }
            fullIndex++;
        }

        return partialIndex == partialWord.length();
    }
}
