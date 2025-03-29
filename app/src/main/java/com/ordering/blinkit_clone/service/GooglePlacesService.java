package com.ordering.blinkit_clone.service;

import android.content.Context;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GooglePlacesService {

    private static GooglePlacesService service;

    private AutocompleteSessionToken token;
    private PlacesClient placesClient;
    private boolean isTokenDestroyed = true;
    private IGooglePlaceCallback callback;
    private Filter searchFilter;
    private static Context applicationContext;

    public static GooglePlacesService service(Context context) {
        if (service == null) {
            applicationContext = context;
            service = new GooglePlacesService();
        }
        return service;
    }

    private GooglePlacesService() {
        this.init();
    }

    private String placesAPIKey() {
        return "AIzaSyBUdfd676Xq_zXN7PrpDgiMPmX-6cm8YRc";
    }

    private void init() {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, placesAPIKey());
                // Create a new Places client instance.
                this.placesClient = Places.createClient(applicationContext);
            }
            if (this.isTokenDestroyed) {
                this.token = AutocompleteSessionToken.newInstance();
                this.isTokenDestroyed = false;
            }
        } catch (Exception e) {
            this.isTokenDestroyed = true;
        }
    }

    /**
     * Performs search on Google Places api for given keyword.
     *
     * @param placeQuery - query to be searched
     * @return - List of matching places found
     */
    private ArrayList<GooglePlacesResult> performSearch(String placeQuery) {
        this.init();
        ArrayList<GooglePlacesResult> resultList = new ArrayList<>();
        try {
            // Use the builder to create a FindAutocompletePredictionsRequest.
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(null)
                    .setSessionToken(this.token)
                    .setQuery(placeQuery)
                    .setCountries("IN")
                    .build();
            Task<FindAutocompletePredictionsResponse> autocompletePredictions = placesClient.findAutocompletePredictions(request);
            // This method should have been called off the main UI thread. Block and wait for at most
            // 60s for a result from the API.
            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS);

            FindAutocompletePredictionsResponse response = autocompletePredictions.isSuccessful() ? autocompletePredictions.getResult() : null;
            if (response != null) {
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    resultList.add(new GooglePlacesResult(prediction));
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {

            this.isTokenDestroyed = true;
        }
        return resultList;
    }

    /**
     * Returns place details for given Place ID. Response includes Place ID,
     * Name, Lat-Long, Address and Address Components.
     * <p>
     * CAUTION: If you need to add fields in the response, first discuss and get
     * the approval. Each call of this function costs money.
     *
     * @param placeID  - Place ID to be searched
     * @param listener - OnCompleteListener object to get the result.
     */
    public void findPlace(@NonNull String placeID, OnCompleteListener<FetchPlaceResponse> listener) {
        if (placeID.isEmpty() || listener == null) {
            return; // If place ID or listener not supplied then no need to send request
        }
        try {
            // Construct a request object, passing the place ID and fields array.
            // IMPORTANT: We are requesting Basic Data which will cost 0.17 dollar per request. Do not add any other field without discussion.
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.DISPLAY_NAME,
                    Place.Field.LOCATION,
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS);
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields).setSessionToken(this.token)
                    .build();
            placesClient.fetchPlace(request).addOnCompleteListener(listener);
        } catch (Exception e) {

        }
        this.isTokenDestroyed = true;
    }

    public void filter(CharSequence s, IGooglePlaceCallback callback) {
        try {
            if (this.searchFilter == null) {
                this.initFilter();
            }
            this.callback = callback;
            this.searchFilter.filter(s);
        } catch (Exception e) {

        }
    }

    private void initFilter() {
        this.searchFilter = new Filter() {
            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                Filter.FilterResults results = new Filter.FilterResults();
                if (constraint.toString().isEmpty()) {
                    return results;
                }
                // Query the autocomplete API for the (constraint) search string.
                List<GooglePlacesResult> searchResult = performSearch(constraint.toString());
                if (searchResult.isEmpty()) {
                    return results;
                }
                // The API successfully returned results.
                results.values = searchResult;
                results.count = searchResult.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                if (callback == null) {
                    return;
                }
                ArrayList<GooglePlacesResult> places = (ArrayList<GooglePlacesResult>) results.values;
                if (places != null && !places.isEmpty()) {
                    places.get(places.size() - 1).isLastItem = false;
                }
                callback.onPlaceSearchResult(places);
            }
        };
    }

    public interface IGooglePlaceCallback {
        void onPlaceSearchResult(ArrayList<GooglePlacesResult> places);
    }

    public static class GooglePlacesResult {
        public String placeId, primaryAddress, secondaryAddress, fullAddress;
        public boolean isLastItem = true;

        GooglePlacesResult() {
        }

        public GooglePlacesResult(AutocompletePrediction prediction) {
            this.placeId = prediction.getPlaceId();
            this.primaryAddress = prediction.getPrimaryText(null).toString();
            this.secondaryAddress = prediction.getSecondaryText(null).toString();
            this.fullAddress = prediction.getFullText(null).toString();
        }
    }
}

