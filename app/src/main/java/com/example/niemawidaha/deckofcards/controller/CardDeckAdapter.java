package com.example.niemawidaha.deckofcards.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.niemawidaha.deckofcards.R;
import com.example.niemawidaha.deckofcards.model.Card;
import com.example.niemawidaha.deckofcards.model.CardDeckResponse;

import java.util.List;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CardDeckAdapter extends RecyclerView.Adapter<CardDeckAdapter.CardViewHolder> {

    // member vars:
    private List<Card> cardDeck ;
    private LayoutInflater mInflater;
    public ItemClickListener mClickListener;
    private Context context;

    // datas passed into the constructor:
    public CardDeckAdapter(Context context,List<Card> cardDeck){

        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.cardDeck = cardDeck;
    }

    // inflates the cell layout from the item_view_card_xml
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_view_card,parent,false);

        return new CardViewHolder(view);

    }

    // binds the data to the image view in each
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        // use picasso to load the images:
        Picasso.with(context).load(cardDeck.get(position).getImage()).centerCrop().into(holder.cardImage);
    }

    @Override
    public int getItemCount() {
        return cardDeck.size();
    }

    // stores and recycles views:
    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // members:
        ImageView cardImage;

        public CardViewHolder(View itemView){
            super(itemView);
            cardImage = itemView.findViewById(R.id.iv_user_cards);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }

        // constructor:
    }

    // conveniency method for getting the data at a click position:
    public String getItem(int id){

        return cardDeck.get(id).getCode();

    }
    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {

        this.mClickListener = itemClickListener;

    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {

        void onItemClick(View view, int position);
    }
}
