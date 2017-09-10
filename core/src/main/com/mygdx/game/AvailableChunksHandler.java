package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.Chunks.AvailableChunk;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import java.util.ArrayList;
import java.util.List;

public class AvailableChunksHandler implements Net.HttpResponseListener, Runnable {

    private static final String TAG = "CHUNKS_META";

    private final String url;

    private final HttpRequestBuilder requestBuilder;
    private final JsonReader reader;
    private final Chunks chunks;

    public AvailableChunksHandler(Chunks chunks, String host, int port) {
        this.url = String.format("http://%s:%d/", host, port);
        this.chunks = chunks;
        this.reader = new JsonReader();
        this.requestBuilder = new HttpRequestBuilder();
    }

    @Override
    public void run() {
        final Net.HttpRequest httpRequest = requestBuilder.newRequest()
                                                          .method(Net.HttpMethods.GET)
                                                          .url(this.url)
                                                          .followRedirects(true)
                                                          .build();
        log("Requesting available chunks from: " + httpRequest.getUrl());
        Gdx.net.sendHttpRequest(httpRequest, this);
    }

    @Override
    public void handleHttpResponse(Net.HttpResponse httpResponse) {
        if (httpResponse.getStatus().getStatusCode() == 200) {
            final List<AvailableChunk> availableChunks = new ArrayList<AvailableChunk>();
            String rawResponse = httpResponse.getResultAsString();
            log("Available chunks fetch success: " + rawResponse);
            JsonValue chunksArrJson = reader.parse(rawResponse);

            for (JsonValue chunkJson : chunksArrJson.get("chunks")) {
                int lat = chunkJson.getInt("lat");
                int lon = chunkJson.getInt("lon");
                availableChunks.add(new AvailableChunk(lat, lon));
            }
            log("Available chunks fetch parsed to " + availableChunks);
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    chunks.availableChunksResult(availableChunks);
                }
            });
        } else {
            log("Available chunks fetch failed: status code " + httpResponse.getStatus().getStatusCode());
        }
    }

    @Override
    public void failed(Throwable t) {
        log("Available chunks fetch failed: " + formatException(t));
    }

    @Override
    public void cancelled() {
        log("Available chunks fetch canelled");
    }

    private static void log(String message) {
        Gdx.app.log(TAG, message);
    }

}
