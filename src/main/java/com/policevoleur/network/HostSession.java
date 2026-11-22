package com.policevoleur.network;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * PC 1 : heberge le serveur (ServerSocket), joue les policiers en local et fait autorite :
 * il verifie les coups du client (voleur), gere les tours et envoie l'etat aux deux cotes.
 */
public final class HostSession implements GameSession {

    private final GameEngine engine;
    private volatile SessionListener listener = new SessionListener() {
    };
    private GameState state;
    private ServerSocket serverSocket;
    private Socket client;
    private PrintWriter out;
    private volatile boolean closed;

    public HostSession(GameEngine engine) {
        this.engine = engine;
        this.state = engine.initialState();
    }

    /** Ouvre le port et attend le client en arriere-plan. Port 0 = port libre au choix du systeme. */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Thread t = new Thread(this::acceptLoop, "host-accept");
        t.setDaemon(true);
        t.start();
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    public synchronized boolean isOpponentConnected() {
        return client != null;
    }

    @Override
    public synchronized GameState state() {
        return state;
    }

    @Override
    public boolean controls(Role role) {
        return role == Role.POLICE;
    }

    @Override
    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    @Override
    public String submitThiefMove(int to) {
        return "Vous jouez les policiers.";
    }

    @Override
    public synchronized String submitPoliceMove(int[] destinations) {
        if (client == null) {
            return "En attente de la connexion du joueur 2 (voleur).";
        }
        String error = engine.policeMoveError(state, destinations);
        if (error != null) {
            return error;
        }
        state = engine.applyPoliceMove(state, destinations);
        publish();
        return null;
    }

    @Override
    public synchronized String requestRestart() {
        state = engine.initialState();
        publish();
        return null;
    }

    @Override
    public void close() {
        closed = true;
        closeQuietly(serverSocket);
        synchronized (this) {
            closeQuietly(client);
        }
    }

    // ------------------------------------------------------------------ reseau

    private void acceptLoop() {
        while (!closed && !serverSocket.isClosed()) {
            try {
                serve(serverSocket.accept());
            } catch (IOException e) {
                if (!closed && !serverSocket.isClosed()) {
                    listener.onInfo("Erreur reseau : " + e.getMessage());
                }
            }
        }
    }

    private void serve(Socket socket) {
        try (socket) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            synchronized (this) {
                client = socket;
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                out.println(Protocol.HELLO);
                out.println(Protocol.encodeState(state));
            }
            listener.onOpponentConnected();
            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line);
            }
        } catch (IOException ignored) {
            // connexion coupee : traitee dans finally
        } finally {
            synchronized (this) {
                client = null;
                out = null;
            }
            if (!closed) {
                listener.onOpponentLeft();
            }
        }
    }

    private synchronized void handleLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 2 && parts[0].equals(Protocol.MOVE_THIEF)) {
            int to;
            try {
                to = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                send(Protocol.ERROR + " Sommet invalide.");
                return;
            }
            String error = engine.thiefMoveError(state, to);
            if (error != null) {
                send(Protocol.ERROR + " " + error);
                return;
            }
            state = engine.applyThiefMove(state, to);
            publish();
        } else if (parts.length == 1 && parts[0].equals(Protocol.RESTART)) {
            state = engine.initialState();
            publish();
        } else {
            send(Protocol.ERROR + " Message inconnu.");
        }
    }

    /** Envoie l'etat au client et previent l'interface locale. A appeler verrou pris. */
    private void publish() {
        send(Protocol.encodeState(state));
        listener.onState(state);
    }

    private void send(String line) {
        if (out != null) {
            out.println(line);
        }
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
                // rien a faire
            }
        }
    }
}
