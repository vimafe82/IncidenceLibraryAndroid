package es.incidence.core.manager;

import android.location.Location;

import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.ReverseGeocodingSearchEngine;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.SearchSelectionCallback;

public class MapBoxManager
{
    public static SearchRequestTask searchAddress(Location location, SearchCallback searchCallback)
    {
        ReverseGeocodingSearchEngine reverseGeocoding = MapboxSearchSdk.createReverseGeocodingSearchEngine();

        final ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .limit(1)
                .build();

        SearchRequestTask searchRequestTask = reverseGeocoding.search(options, searchCallback);
        return searchRequestTask;
    }

    public static SearchRequestTask searchAddress(String address, SearchSelectionCallback searchCallback)
    {
        SearchEngine searchEngine = MapboxSearchSdk.createSearchEngine();

        final SearchOptions options = new SearchOptions.Builder()
                .limit(10)
                .build();

        SearchRequestTask searchRequestTask = searchEngine.search(address, options, searchCallback);
        return searchRequestTask;
    }
}
