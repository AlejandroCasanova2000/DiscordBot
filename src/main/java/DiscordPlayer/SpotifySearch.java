package DiscordPlayer;

import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.json.Json;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SpotifySearch {

    private static String accessToken;

    public static void getAccessToken() throws IOException, ParseException, SpotifyWebApiException {
        InputStream input = new FileInputStream("application.properties");
        Properties properties = new Properties();
        properties.load(input);
        SpotifyApi spotify = new SpotifyApi.Builder()
                .setClientId(properties.getProperty("spotifyID"))
                .setClientSecret(properties.getProperty("spotifySecret"))
                .build();
        ClientCredentialsRequest clientCredentials = spotify.clientCredentials().build();
        ClientCredentials credentials = clientCredentials.execute();
        accessToken = credentials.getAccessToken();
    }

    public static List<String> getPlaylistTrackNames(String playlistURL) throws IOException, ParseException, SpotifyWebApiException {
        try {
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(accessToken)
                    .build();
            GetPlaylistsItemsRequest playlist = spotifyApi.getPlaylistsItems(playlistURL)
                    .fields("items(track.name)").build();
            JSONObject json = new JSONObject(playlist.getJson());
            JSONArray tracks = json.getJSONArray("items");
            ArrayList<String> tracksNames = new ArrayList<String>();
            for (int i = 0; i < tracks.length(); i++) {
                tracksNames.add(tracks.getJSONObject(i).getJSONObject("track").getString("name"));
            }
            return tracksNames;
        }
        catch (UnauthorizedException e) {
            getAccessToken();
            return getPlaylistTrackNames(playlistURL);
        }
    }

}
