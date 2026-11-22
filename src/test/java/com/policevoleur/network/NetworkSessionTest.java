package com.policevoleur.network;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.Role;
import com.policevoleur.game.Winner;
import com.policevoleur.graph.BoardLoader;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/** Parties reelles sur sockets, en local (127.0.0.1) : aucun serveur externe necessaire. */
class NetworkSessionTest {

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long end = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                fail("Delai depasse");
            }
            Thread.sleep(10);
        }
    }

    @Test
    void hostAndClientPlayAFullGame() throws Exception {
        GameEngine engine = new GameEngine(BoardLoader.loadDefault());
        HostSession host = new HostSession(engine);
        host.start(0);
        ClientSession client = new ClientSession(engine);
        try {
            client.connect("127.0.0.1", host.port(), 2000);
            waitFor(host::isOpponentConnected);

            // c'est au voleur de commencer : les policiers ne peuvent pas jouer
            assertNotNull(host.submitPoliceMove(new int[] {5, 2, 3}));
            // coup illegal du voleur (case occupee) refuse cote client
            assertNotNull(client.submitThiefMove(1));
            // coup legal
            assertNull(client.submitThiefMove(4));
            waitFor(() -> host.state().turn() == Role.POLICE);
            waitFor(() -> client.state().turn() == Role.POLICE);
            assertEquals(4, client.state().thief());

            // le client ne peut pas rejouer, ni commander les policiers
            assertNotNull(client.submitThiefMove(0));
            assertNotNull(client.submitPoliceMove(new int[] {5, 2, 3}));

            assertNull(host.submitPoliceMove(new int[] {5, 2, 3}));
            waitFor(() -> client.state().turn() == Role.THIEF);
            assertEquals(5, client.state().policeAt(0));

            // le voleur revient au centre apres en etre sorti : il gagne
            assertNull(client.submitThiefMove(0));
            waitFor(() -> host.state().isOver());
            assertEquals(Winner.THIEF, host.state().winner());
            waitFor(() -> client.state().winner() == Winner.THIEF);

            // le client peut demander de recommencer
            assertNull(client.requestRestart());
            waitFor(() -> host.state().moves() == 0 && !host.state().isOver());
            waitFor(() -> client.state().moves() == 0 && !client.state().isOver());
        } finally {
            client.close();
            host.close();
        }
    }

    @Test
    void serverAnswersErrorToAnIllegalMessage() throws Exception {
        GameEngine engine = new GameEngine(BoardLoader.loadDefault());
        HostSession host = new HostSession(engine);
        host.start(0);
        try (Socket socket = new Socket("127.0.0.1", host.port())) {
            socket.setSoTimeout(5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            assertEquals("HELLO", in.readLine());
            assertTrue(in.readLine().startsWith("STATE "));
            out.println("MOVE_THIEF 1"); // occupe par un policier
            assertTrue(in.readLine().startsWith("ERROR "));
            out.println("MOVE_THIEF abc");
            assertTrue(in.readLine().startsWith("ERROR "));
            out.println("N_IMPORTE_QUOI");
            assertTrue(in.readLine().startsWith("ERROR "));
            assertEquals(0, host.state().moves());
        } finally {
            host.close();
        }
    }

    @Test
    void localSessionRunsBothSides() {
        GameEngine engine = new GameEngine(BoardLoader.loadDefault());
        LocalSession local = new LocalSession(engine);
        assertTrue(local.controls(Role.POLICE) && local.controls(Role.THIEF));
        assertNull(local.submitThiefMove(4));
        assertNull(local.submitPoliceMove(new int[] {5, 2, 3}));
        assertNull(local.submitThiefMove(0));
        assertEquals(Winner.THIEF, local.state().winner());
        assertNull(local.requestRestart());
        assertEquals(0, local.state().moves());
    }
}
