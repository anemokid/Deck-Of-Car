package com.example.niemawidaha.deckofcards;

import android.content.Context;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.niemawidaha.deckofcards.backend.CardDeckService;
import com.example.niemawidaha.deckofcards.backend.DeckApiUtils;
import com.example.niemawidaha.deckofcards.controller.CardDeckAdapter;
import com.example.niemawidaha.deckofcards.model.Card;
import com.example.niemawidaha.deckofcards.model.CardDeckResponse;
import com.example.niemawidaha.deckofcards.model.DeckID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    // private members:
    private final static String LOG_TAG = "LOG_TAG: Main Activity";
    private CardDeckService cardDeckService;
    private RecyclerView mRecyclerView;
    private CardDeckAdapter mCardDeckAdapter;

    private TextView m_tv_NumberOfCardsRemaining;
    private Button m_btn_ShuffleNewDeck;
    private EditText m_et_SpecifiedCards;
    private Button m_btn_DrawCards;


    // values:
    private static List<Card> cardDeck;
    private String deck_id; // stores the DECK ID
    private int currentCount; // stores the current count of the remaining cards
    private int fullDeck = 52; // stores the value of the users remaining
    private int numOfUserSelectedValue; // the value user selects after validation
    private String displayCount = String.format("Cards Remaining: %s", currentCount); // String placeholder that updates the Cards Remaining text
    private String displayUserValueValidationError = String.format("There are only %s, cards remaining", currentCount); // String placeholder that updates the Cards Remaining text
    private String displaySelectedCard; // String placeholder that updates the Cards Remaining text

    public static final String ShuffleNewDeckEndPoint = "https://deckofcardsapi.com/";

    /**
     * networking:
     */
    final String CARD_DECK_BASE_URL = "https://deckofcardsapi.com/api/deck/new/shuffle/";
    String deckOfCards_BASE_URL = "https://deckofcardsapi.com/api/deck/" + deck_id + "/draw/?";
    final String QUERY_PARAM = "count"; // Parameter for the search string


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the service:
        cardDeckService = DeckApiUtils.getCardDeckService();

        //buildUp52CardsURI(); // builds a URI for the 52 cards
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // helper methods:
        findViews();
        setUpRV();
        loadCards();
    }

    /**
     * findsview:
     */
    public void findViews() {
        m_tv_NumberOfCardsRemaining = findViewById(R.id.tv_cards_remaining);
        m_btn_ShuffleNewDeck = findViewById(R.id.btn_shuffle_new_deck);
        m_et_SpecifiedCards = findViewById(R.id.et_draw_cards);
        m_btn_DrawCards = findViewById(R.id.btn_display_drawn_cards);

        setUpRV();
    }

    /**
     * setup recycler view:
     */
    public void setUpRV(){
        // set up RV:
        int numOfColumns = 2;

        RecyclerView recyclerView = findViewById(R.id.rv_user_cards);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numOfColumns));

        mCardDeckAdapter = new CardDeckAdapter(this, cardDeck);
        mCardDeckAdapter.setClickListener(this);

        // set adapter on the RV:
        recyclerView.setAdapter(mCardDeckAdapter);

        Log.d(LOG_TAG, "setupRV called!");

    }

    /**
     * onItemClick():
     * when an image view is clicked, the app should display a Toast message
     * saying the value and suit of the card that was clicked
     * contracted method from the ItemClickListner interface in the adapter:
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        displaySelectedCard = String.format("You have selected: " + cardDeck.get(position).getCode());

        Log.i("TAG", "You clicked card " + mCardDeckAdapter.getItem(position) );
        Toast.makeText(this, displaySelectedCard, Toast.LENGTH_LONG).show();
    }

    /**
     * isCardDeckSelectedEmpty():
     * helper method for checking if the card deck selected was a null value
     *
     * @param mUserInput
     * @return
     */
    public boolean isCardDeckSelectedEmpty(EditText mUserInput) {

        return mUserInput.getText().length() == 0;

    }

    /**
     * drawDeck():
     *
     * @param view
     */
    public void drawDeck(View view) throws IOException {

        // if user passes validation:
        // the value needed to draw is the numOfUserSelected from the et_draw_cards
        // grab the user selected value for the deck of cards endpoint

        boolean isUserValueValid;

        int numOfUserSelection = Integer.valueOf(m_et_SpecifiedCards.getText().toString()); // the value specified by the user on correct check:

        // if blank:
        // set an error: You must draw atleast one card
        if( m_et_SpecifiedCards == null ) {

            isUserValueValid = false; // values null and not true
            Toast.makeText(MainActivity.this, R.string.txt_error_user_value, Toast.LENGTH_LONG).show();
        }else if ( numOfUserSelection == 0) {

            // if 0:
            // set an error: You must draw atleast one card
            isUserValueValid = false; // values null and not true
            Toast.makeText(MainActivity.this, R.string.txt_error_user_value, Toast.LENGTH_LONG).show();

        } else if (numOfUserSelection> currentCount){

            // if the user input is greater than the number of cards remaining in the current deck:
            // set an error: There are only X, cards remaining
            // use currentCount to display:
            // String display User Validation error: use the users count:

            isUserValueValid = false;
            Toast.makeText(MainActivity.this, displayUserValueValidationError, Toast.LENGTH_LONG).show();

        } else {

            // if the value is valid then store the value selected by user in a global var:
            numOfUserSelectedValue = numOfUserSelection;

            Toast.makeText(MainActivity.this, R.string.txt_valid_user_value, Toast.LENGTH_LONG).show();

            isUserValueValid = true;

        }

        // if the value is valid then start a request:
        if( isUserValueValid) {

            // Make an HTTP GET request: to num_cards:
            // grab the value of the deck_id for the endpoint:
            // make an HTTP GET request ot the "draw cards" API endpoint
            // https://deckofcardsapi.com/api/deck/{deck_id}/draw/?count={num_cards}
            // take the value and make an HTTP get request: query parameter count:
            // endpoint: https://deckofcardsapi.com/api/deck/{deck_id}/draw/?count={num_of_cards}

            Log.d(LOG_TAG, "drawDeck called!");
            cardDeck = makeDrawRequest();
        }


        // delete the user input from the Edit text, clear input so hint is visibl
        m_et_SpecifiedCards.getText().clear();

        // close/ hide the keyboard:
        hideKeyboard();
    } // ends draw deck:

    /**
     * makeDrawRequest():
     * takes a users validated value and makes a request to the Deck Of Cards API:
     */
    public List<Card> makeDrawRequest() throws IOException {

        Log.d(LOG_TAG, "makeDrawRequest called!");

        /**
         * query for the 52 cards:
         */
        cardDeck = new ArrayList<>();

        // get the string:
        //https://deckofcardsapi.com/api/deck/5966wkl4tcmd/draw/?count=52
        // query: count
        // make a new string out of the id:
        //String deckOfCards_BASE_URL = "https://deckofcardsapi.com/api/deck/" + deck_id + "/draw/?";

        // build up the query URI, limiting results to 52 cards:
        Uri secondBuiltURI = Uri.parse(deckOfCards_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, "52")
                .build();

        // convert the second URI to a string:
        String cardDeck52URL = secondBuiltURI.toString();

        // CONNECT & DOWNLOAD DATA:
        // pass this string to downloadURL
        String cardDeck52Response = downloadUrl(cardDeck52URL);


        try {

            JSONObject fullData = new JSONObject(cardDeck52Response); // gets the full json object

            // to get the cards json array:
            JSONArray fiftyTwoCards = new JSONArray(fullData.getJSONArray("cards"));

            // get the JSON OBJECT: card deck of 52 cards:
            // for a single card:
            // since u have a list of cards:
            for( int i = 0; i < fiftyTwoCards.length(); i++){
                for (Card card : cardDeck) {


                    // create a json object and get the values from the json array:
                    JSONObject cardJSON = fiftyTwoCards.getJSONObject(i);

                    // get these specific values from the json object
                    String suit = cardJSON.getString("suit"); // suit
                    String image = cardJSON.getString("image"); // image
                    String code = cardJSON.getString("code");// code
                    String value = cardJSON.getString("value");// value

                    // add the values to the card:
                    card.setSuit(suit);
                    card.setImage(image);
                    card.setCode(code);
                    card.setValue(value);

                    // add the card to the list
                    cardDeck.add(card);

                    // data supposedely full

                } //ends for loop
            }  // ends for each loop
        } catch (JSONException e) {
            e.printStackTrace();

        } // ends try catch
        return cardDeck;
    } // ends makeDrawRequest

    /**
     * hideKeyboard():
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    /**
     * shuffleNewDeck():
     *
     * @param view
     */
    public void shuffleNewDeck(View view) throws IOException {

        Log.d(LOG_TAG, "shuffleNewDeck called!");

        // build query URI:
        Uri builtURI = Uri.parse(CARD_DECK_BASE_URL)
                .buildUpon()
                .build();

        // convert the Card Deck URI to a string:
        String cardDeckURL = builtURI.toString();

        // CONNECT AND DOWNLOAD DATA:
        // pass this string into the downloadURL()
        String cardDeckResponse = downloadUrl(cardDeckURL);

        // get the JSON OBJECT: deck_id
        try {
            JSONObject fullData = new JSONObject(cardDeckResponse);
            deck_id = fullData.getString("deck_id");
            //String success = fullData.getString("success");
            //String remaining = fullData.getString("remaining");

            m_tv_NumberOfCardsRemaining.setText(deck_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * updateCardsRemainingText():
     */
    public void updateCardsRemainingText() {


    }

    /**
     * downloadURL():
     */
    public String downloadUrl(String cardDeckUrl) throws IOException {

        InputStream inputStream = null;

        // Only dislay the first 100 characters of the
        // web page content:
        int len = 10000;


        HttpURLConnection connection = null;
        try {

            URL url = new URL(cardDeckUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);

            // Start the Query:
            connection.connect();

            int response = connection.getResponseCode();

            Log.d(LOG_TAG, "The response is" + response);

            inputStream = connection.getInputStream();

            // conver the input stream into a string
            String contentAsString = convertInputToString(inputStream, len);
            return contentAsString;

            // Close the InputStream and connectoin
        } finally {

            connection.disconnect();

            if (inputStream != null) {
                inputStream.close();
            }
        } // ends try-finally for url connection
    }

    /**
     * convertInputToString(): converts the InputStream to a string so tht
     * the activity can display it, the method uses an InputStreamReader instance
     * to read bytes and decode them into characters.
     */
    public String convertInputToString(InputStream stream, int length) throws IOException {

        Reader reader = null;

        reader = new InputStreamReader(stream, "UTF-8");

        char[] buffer = new char[length];

        reader.read(buffer);

        return new String(buffer);


    }



    /**
     * networking for 52 cards:
     */
    private void loadCards() {

        /**
         * EXTRACT ID:
         */
        cardDeckService.getDeckID().enqueue(new Callback<DeckID>() {

            @Override
            public void onResponse(Call<DeckID> call, Response<DeckID> response) {

                if (response.isSuccessful()) {

                    // extract the deck id:
                    //deck_id = response.body().getDeckId();

                    Log.d("DECK ID:",  response.body().toString());
                } else {

                    int statusCode = response.code();

                    // handle request erros depending on status code:
                    Log.d("DECK ID: ", "deck id: posts didnt load from api" + statusCode);

                }
            }

            @Override
            public void onFailure(Call<DeckID> call, Throwable t) {
                Log.d("MainActivity", "ugh");
                t.printStackTrace();
            }
        });

        cardDeckService.getCardDeck(deck_id, 52).enqueue(new Callback<CardDeckResponse>() {
            @Override
            public void onResponse(Call<CardDeckResponse> call, Response<CardDeckResponse> response) {
                if( response.isSuccessful()){

                    //mCardDeckAdapter.updateAnswers(response.body().getCards());
                    Log.d("CARD DECK:", "posts loaded form the API");
                } else {

                    int statusCode = response.code();

                    Log.d("CARD DECK:", "posts didnt load from api" + statusCode);
                }
            }

            @Override
            public void onFailure(Call<CardDeckResponse> call, Throwable t) {
                Log.d("CARD DECK", "error loading from API");
                t.printStackTrace();
            }
        });
    } // ends load cards:
}
