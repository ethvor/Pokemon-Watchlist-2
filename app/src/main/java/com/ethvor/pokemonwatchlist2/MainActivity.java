package com.ethvor.pokemonwatchlist2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchButton;
    private TextView pokemonName;
    private TextView pokemonNumber;
    private TextView pokemonWeight;
    private TextView pokemonHeight;
    private TextView pokemonBaseXp;
    private TextView pokemonMove;
    private TextView pokemonAbility;
    private ImageView pokemonImage;
    private ListView watchlistView;
    private ArrayList<String> watchlist;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        pokemonName = findViewById(R.id.pokemonName);
        pokemonNumber = findViewById(R.id.pokemonNumber);
        pokemonWeight = findViewById(R.id.pokemonWeight);
        pokemonHeight = findViewById(R.id.pokemonHeight);
        pokemonBaseXp = findViewById(R.id.pokemonBaseXp);
        pokemonMove = findViewById(R.id.pokemonMove);
        pokemonAbility = findViewById(R.id.pokemonAbility);
        pokemonImage = findViewById(R.id.pokemonImage);
        watchlistView = findViewById(R.id.watchlistView);

        watchlist = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, watchlist);
        watchlistView.setAdapter(adapter);

        watchlistView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String item = watchlist.get(position);
                String pokemonId = item.split(" ")[0];
                fetchPokemon(pokemonId);
            }
        });

        searchButton.setOnClickListener(v -> {
            String input = searchInput.getText().toString().toLowerCase();

            if (!isValidInput(input)) {
                Toast.makeText(this, "Invalid pokemon name or id", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchPokemon(input);
        });
    }

    //get pokemon from api
    private void fetchPokemon(String pokemon) {
        AndroidNetworking.get("https://pokeapi.co/api/v2/pokemon/{pokemon}")
                .addPathParameter("pokemon", pokemon)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        displayPokemon(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, "Pokemon not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayPokemon(JSONObject response) {
        try {
            String name = response.getString("name");
            int id = response.getInt("id");
            int weight = response.getInt("weight");
            int height = response.getInt("height");
            int baseXp = response.getInt("base_experience");

            JSONArray moves = response.getJSONArray("moves");
            String move = moves.getJSONObject(0).getJSONObject("move").getString("name");

            JSONArray abilities = response.getJSONArray("abilities");
            String ability = abilities.getJSONObject(0).getJSONObject("ability").getString("name");

            String imageUrl = response.getJSONObject("sprites").getString("front_default");

            pokemonName.setText(name);
            pokemonNumber.setText("Number: " + id);
            pokemonWeight.setText("Weight: " + weight);
            pokemonHeight.setText("Height: " + height);
            pokemonBaseXp.setText("Base XP: " + baseXp);
            pokemonMove.setText("Move: " + move);
            pokemonAbility.setText("Ability: " + ability);

            loadImage(imageUrl);

            //add to watchlist
            String entry = id + " " + name;
            if (!watchlist.contains(entry)) {
                watchlist.add(entry);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
        }
    }

    private void loadImage(String imageUrl) {
        AndroidNetworking.get(imageUrl)
                .build()
                .getAsBitmap(new BitmapRequestListener() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        pokemonImage.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(ANError error) {
                    }
                });
    }

    private boolean isValidInput(String input) {
        if (input.isEmpty()) {
            return false;
        }

        String invalidChars = "%&*(@)!;:<>";
        for (int i = 0; i < input.length(); i++) {
            if (invalidChars.indexOf(input.charAt(i)) >= 0) {
                return false;
            }
        }

        // check if number
        boolean isNumeric = true;
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                isNumeric = false;
                break;
            }
        }

        if (isNumeric) {
            int id = Integer.parseInt(input);
            if (id < 1 || id > 1010) {
                return false;
            }
        }

        return true;
    }
}
