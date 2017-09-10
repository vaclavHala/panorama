package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import static com.mygdx.game.common.ChunkNamingScheme.chunkFileName;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import java.io.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.*;

public class ChunkDataHandler implements Runnable {

    private static final String TAG = "CHUNKS_DATA";

    private static final int BUFFER_SIZE = 2 << 14;

    private final ProgressListener progress;
    private final FileHandle fileTmp;
    private final FileHandle fileFinal;
    private final String host;
    private final int port;
    private final int lat;
    private final int lon;

    /**
     * @param progress gets notified about download progress, may be null.
     *      Will be called in thread of handler, if it needs to run in render thread
     *      then it must post the runnable itself
     */
    public ChunkDataHandler(
            FileHandle fileTmp, FileHandle fileFinal,
            ProgressListener progress,
            String host, int port, int lat, int lon) {
        this.progress = progress;
        this.fileTmp = fileTmp;
        this.fileFinal = fileFinal;
        this.host = host;
        this.port = port;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public void run() {

        String request = request();
        log("Sending request for chunk data: lon=%s lat=%s", lon, lat);

        SocketHints socketHints = new SocketHints();
        socketHints.connectTimeout = 4000;
        Socket socket = null;
        OutputStream file = null;
        try {
            socket = Gdx.net.newClientSocket(Protocol.TCP, host, port, socketHints);
            file = fileTmp.write(false, BUFFER_SIZE);
            socket.getOutputStream().write(request.getBytes(UTF_8));
            socket.getOutputStream().flush();
            log("Wrote request: " + request);
            InputStream in = socket.getInputStream();
            Map<String, String> header = readHeader(in);
            log("Response header: " + header);
            int dataLength = parseInt(header.get("Content-Length"));

            InputStream bin = new BufferedInputStream(in, BUFFER_SIZE);
            byte[] buff = new byte[BUFFER_SIZE];
            int dataRead = 0;
            while (dataRead < dataLength) {
                int readNow = bin.read(buff);
                if (readNow == -1) {
                    // we expected to get more but there is nada
                    throw new IllegalArgumentException("Unexpected end of data");
                }
                file.write(buff);
                dataRead += readNow;
                log("Transferred %s bytes (have %s of %s bytes)", readNow, dataRead, dataLength);
                if (this.progress != null) {
                    int percentDone = Math.min(99, 100 * dataRead / dataLength);
                    this.progress.update(percentDone);
                }
            }
            fileTmp.moveTo(fileFinal);
            if (this.progress != null) {
                log("Whole chunk obtained successfully: lat=%s, lon=%s", lat, lon);
                this.progress.update(100);
                this.progress.success();
            }

        } catch (Exception e) {
            log("Error reading chunk: lat=%s, lon=%s: %s", lat, lon, formatException(e));
            if (file != null) {
                fileTmp.delete();
            }
            if (this.progress != null) {
                this.progress.fail();
            }
        } finally {
            if (socket != null) {
                socket.dispose();
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private List<String> readHeaderLines(InputStream in) throws IOException {
        List<String> header = new ArrayList<String>();
        byte[] buff = new byte[BUFFER_SIZE];
        int at = 0;
        while (true) {
            byte b = (byte) in.read();
            if (b == -1) {
                throw new IOException("End of data reached before header ended");
            }
            buff[at++] = b;

            if (b == 10) {
                String line = new String(buff, 0, at, UTF_8).trim();
                log("LINE: " + line);
                if (line.isEmpty()) {
                    return header;
                }
                header.add(line);
                at = 0;
            }
        }
    }

    private Map<String, String> readHeader(InputStream in) throws IOException {
        List<String> lines = readHeaderLines(in);
        String status = lines.get(0);
        String[] statusSplit = status.split(" ");
        if (parseInt(statusSplit[1]) != 200) {
            throw new IOException("HTTP response is not OK: " + status);
        }

        Map<String, String> header = new HashMap<String, String>();
        for (String line : lines.subList(1, lines.size())) {
            String[] headerSplit = line.split(": ", 2);
            header.put(headerSplit[0], headerSplit[1]);
        }

        return header;
    }

    private String request() {
        return format("GET /%s HTTP/1.1\r\n\r\n", chunkFileName(lon, lat));

    }

    private static void log(String message, Object... args) {
        Gdx.app.log(TAG, format(message, args));
    }

}
