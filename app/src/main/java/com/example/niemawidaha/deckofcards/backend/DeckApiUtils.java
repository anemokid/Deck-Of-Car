package com.example.niemawidaha.deckofcards.backend;

public class DeckApiUtils {

    public static final String BASE_URL = "https://deckofcardsapi.com";

    public static CardDeckService getCardDeckService(){
        return RetrofitClient.getClient(BASE_URL).create(CardDeckService.class);
    }
}
