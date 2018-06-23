package com.example.niemawidaha.deckofcards.backend;

import com.example.niemawidaha.deckofcards.model.Card;
import com.example.niemawidaha.deckofcards.model.CardDeckResponse;
import com.example.niemawidaha.deckofcards.model.DeckID;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CardDeckService {

    @GET("api/deck/new/shuffle")
    Call<DeckID> getDeckID();

    @GET("api/deck/{deck_id}/draw/")
    Call<CardDeckResponse> getCardDeck(
            @Path("deck_id") String deckID,
            @Query("count") int numOfCards);

}
