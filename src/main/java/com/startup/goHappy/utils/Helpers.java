package com.startup.goHappy.utils;

import org.springframework.stereotype.Service;

@Service
public class Helpers {
    public boolean matches(String fullText, String searchText) {
        String fullTextLower = fullText.toLowerCase();
        String[] searchWords = searchText.toLowerCase().split("\\s+");

        for (String searchWord : searchWords) {
            String[] fullTextWords = fullTextLower.split("\\s+");
            boolean wordFound = false;

            for (String fullWord : fullTextWords) {
                fullWord = fullWord.replaceAll("[^a-zA-Z0-9]", "");
                String cleanSearchWord = searchWord.replaceAll("[^a-zA-Z0-9]", "");
                if (fullWord.startsWith(cleanSearchWord)) {
                    wordFound = true;
                    break;
                }
            }

            if (!wordFound) {
                return false;
            }
        }
        return true;
    }
}
