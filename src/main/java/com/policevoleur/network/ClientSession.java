package com.policevoleur.network;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * PC 2 : se connecte au serveur (Socket) et joue le voleur. Il pre-verifie ses coups avec les
 * memes regles, mais seul l'etat renvoye par le serveur fait foi.
 */
public final class ClientSession implements GameSession {

    private final GameEngine engine;
    private volatile SessionListener listener = new SessionListener() {
    };
    private volatile GameState state;
    private volatile boolean closed;
    private Socket socket;
    private PrintWriter out;

    public ClientSession(GameEngine engine) {
        this.engine = engine;
        this.state = engine.initialState();
    }

    public void connect(String host, int port, int timeoutMillis) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), timeoutMillis);
        synchronized (this) {
            socket = s;
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
        }
        Thread t = new Thread(() -> readLoop(s), "client-reader");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public GameState state() {
        return state;
    }

    @Override
    public boolean controls(Role role) {
        return role == Role.THIEF;
    }

    @Override
    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String submitThiefMove(int to) {
        String error = engine.thiefMoveError(state, to);
        if (error != null) {
            return error;
        }
        return send(Protocol.MOVE_THIEF + " " + to);
    }

    @Override
    public String submitPoliceMove(int[] destinations) {
        return "Vous jouez le voleur.";
    }

    @Override
    public String requestRestart() {
        return send(Protocol.RESTART);
    }

    @Override
    public void close() {
        closed = true;
        synchronized (this) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // rien a faire
                }
            }
        }
    }

    private synchronized String send(String line) {
        if (out == null) {
            return "Non connecte.";
        }
        out.println(line);
        return out.checkError() ? "Connexion perdue." : null;
    }

    private void readLoop(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(Protocol.STATE)) {
                    try {
                        state = Protocol.decodeState(line);
                        listener.onState(state);
                    } catch (IllegalArgumentException e) {
                        listener.onInfo("Message du serveur illisible.");
                    }
                } else if (line.startsWith(Protocol.ERROR)) {
                    listener.onInfo(line.substring(Protocol.ERROR.length()).trim());
                }
                // HELLO : rien a faire, l'etat suit
            }
        } catch (IOException ignored) {
            // connexion coupee : traitee ci-dessous
        }
        if (!closed) {
            listener.onOpponentLeft();
        }
    }
}
