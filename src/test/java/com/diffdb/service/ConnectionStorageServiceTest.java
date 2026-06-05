package com.diffdb.service;

import com.diffdb.model.ConnectionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConnectionStorageServiceTest {

    @Test
    void findByNameReturnsMatching() {
        ConnectionStorageService service = new ConnectionStorageService();
        ConnectionConfig config = new ConnectionConfig();
        config.setName("my-test-db");
        service.save(config);

        ConnectionConfig found = service.findByName("my-test-db");
        assertEquals(config, found);
    }

    @Test
    void findByNameReturnsNullIfNotFound() {
        ConnectionStorageService service = new ConnectionStorageService();

        assertNull(service.findByName("nonexistent"));
    }
}