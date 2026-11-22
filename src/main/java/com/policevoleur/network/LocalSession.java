package com.policevoleur.network;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;

/** Partie sur un seul ordinateur (deux joueurs devant le meme ecran), pour tester sans reseau. */
public final class LocalSession implements GameSession {

    private final GameEngine engine;
    private volatile SessionListener listener = new SessionListener() {
    };
    private GameState state;

    public LocalSession(GameEngine engine) {
        this.engine = engine;
        this.state = engine.initialState();
    }

    @Override
    public synchronized GameState state() {
        return state;
    }

    @Override
    public boolean controls(Role role) {
        return true;
    }

    @Override
    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized String submitThiefMove(int to) {
        String error = engine.thiefMoveError(state, to);
        if (error != null) {
            return error;
        }
        state = engine.applyThiefMove(state, to);
        listener.onState(state);
        return null;
    }

    @Override
    public synchronized String submitPoliceMove(int[] destinations) {
        String error = engine.policeMoveError(state, destinations);
        if (error != null) {
            return error;
        }
        state = engine.applyPoliceMove(state, destinations);
        listener.onState(state);
        return null;
    }

    @Override
    public synchronized String requestRestart() {
        state = engine.initialState();
        listener.onState(state);
        return null;
    }

    @Override
    public void close() {
        // rien a fermer
    }
}
