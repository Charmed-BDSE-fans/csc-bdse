package ru.csc.bdse.model.client;

import java.io.IOException;
import java.util.Optional;

/**
 * Represent client for kvnode
 *
 * @author alesavin
 */
public interface KeyValueStorageNodeClient {

    void upsert(String key, byte[] value) throws IOException;

    Optional<byte[]> get(String key) throws IOException;

    String[] keys(Optional<String> prefix) throws IOException;

    String status() throws IOException;

    void command(String node, String command) throws IOException;
}
