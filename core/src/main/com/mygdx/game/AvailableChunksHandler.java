package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import static com.mygdx.game.Asserts.notOnUI;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import com.mygdx.game.model.Chunk;
import java.util.ArrayList;
import java.util.List;

public class AvailableChunksHandler implements Net.HttpResponseListener, Runnable {

    private final String url;

    private final HttpRequestBuilder requestBuilder;
    private final JsonReader reader;
    private final AvailableChunksListener listener;

    public AvailableChunksHandler(AvailableChunksListener listener, String host, int port) {
        this.url = String.format("http://%s:%d/", host, port);
        this.listener = listener;
        this.reader = new JsonReader();
        this.requestBuilder = new HttpRequestBuilder();
    }

    @Override
    public void run() {
        assert notOnUI();
        System.out.println("Run request on " + Thread.currentThread());

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
            final List<Chunk> availableChunks = new ArrayList<Chunk>();
            String rawResponse = httpResponse.getResultAsString();
            log("Available chunks fetch success: " + rawResponse);
            JsonValue chunksArrJson = reader.parse(rawResponse);

            for (JsonValue chunkJson : chunksArrJson.get("chunks")) {
                int lat = chunkJson.getInt("lat");
                int lon = chunkJson.getInt("lon");
                availableChunks.add(new Chunk(lat, lon));
            }
            log("Available chunks fetch parsed to " + availableChunks);
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    listener.availableFetchDone(availableChunks);
                }
            });
        } else {
            log("Available chunks fetch failed: status code " + httpResponse.getStatus().getStatusCode());
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    listener.availableFetchFailed();
                }
            });
        }
    }

    @Override
    public void failed(Throwable t) {
        log("Available chunks fetch failed: " + formatException(t));
        System.out.println("Failed request on " + Thread.currentThread());
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                listener.availableFetchFailed();
            }
        });
    }

    @Override
    public void cancelled() {
        log("Available chunks fetch canelled");
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                listener.availableFetchFailed();
            }
        });
    }

    private static void log(String message) {
        Gdx.app.log("pano.chunk.meta", message);
    }

    public interface AvailableChunksListener {

        void availableFetchDone(List<Chunk> availableChunks);

        void availableFetchFailed();
    }

}
