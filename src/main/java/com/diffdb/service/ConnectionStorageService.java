package com.diffdb.service;

import com.diffdb.model.ConnectionConfig;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists the list of {@link ConnectionConfig} (without secrets) at the
 * application level. Secrets live in PasswordSafe via {@link CredentialService}.
 */
@State(name = "DiffDbConnections", storages = @Storage("diffdb-connections.xml"))
public final class ConnectionStorageService
        implements PersistentStateComponent<ConnectionStorageService.State> {

    public static class State {
        public List<ConnectionConfig> connections = new ArrayList<>();
    }

    private State state = new State();

    public static ConnectionStorageService getInstance() {
        return ApplicationManager.getApplication().getService(ConnectionStorageService.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public List<ConnectionConfig> getConnections() {
        return new ArrayList<>(state.connections);
    }

    /** Inserts or updates by id. */
    public void save(ConnectionConfig config) {
        for (int i = 0; i < state.connections.size(); i++) {
            if (state.connections.get(i).getId().equals(config.getId())) {
                state.connections.set(i, config);
                return;
            }
        }
        state.connections.add(config);
    }

    public void delete(String id) {
        state.connections.removeIf(c -> c.getId().equals(id));
    }

    @Nullable
    public ConnectionConfig findById(String id) {
        return state.connections.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
