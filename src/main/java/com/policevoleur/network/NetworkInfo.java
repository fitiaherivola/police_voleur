package com.policevoleur.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Adresses IPv4 locales a communiquer au joueur 2 pour qu'il se connecte (meme reseau Wi-Fi). */
public final class NetworkInfo {

    private NetworkInfo() {
    }

    public static List<String> localAddresses() {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback() || nic.isVirtual()) {
                    continue;
                }
                for (InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        result.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException ignored) {
            // on renvoie ce qu'on a
        }
        return result;
    }
}
