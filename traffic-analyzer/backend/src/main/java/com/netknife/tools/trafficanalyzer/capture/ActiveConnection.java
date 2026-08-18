package com.netknife.tools.trafficanalyzer.capture;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/** Agregado en memoria de una conexion activa: se actualiza con cada paquete visto. */
public class ActiveConnection {

    private final ConnectionKey key;
    private final Instant firstSeen;
    private volatile Instant lastSeen;
    private volatile boolean encrypted;
    private final AtomicLong packetsSeen = new AtomicLong();
    private final AtomicLong bytesSeen = new AtomicLong();

    public ActiveConnection(ConnectionKey key, boolean encrypted, Instant now) {
        this.key = key;
        this.firstSeen = now;
        this.lastSeen = now;
        recordPacket(encrypted, 0, now);
    }

    /** Si algun paquete de la conexion parece cifrado, la conexion entera se marca como cifrada. */
    public void recordPacket(boolean packetEncrypted, int lengthBytes, Instant now) {
        this.lastSeen = now;
        this.packetsSeen.incrementAndGet();
        this.bytesSeen.addAndGet(lengthBytes);
        if (packetEncrypted) {
            this.encrypted = true;
        }
    }

    public ConnectionKey getKey() {
        return key;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public long getPacketsSeen() {
        return packetsSeen.get();
    }

    public long getBytesSeen() {
        return bytesSeen.get();
    }
}
