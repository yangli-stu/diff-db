package com.diffdb.diff;

import com.diffdb.connection.ConnectionManager;
import com.diffdb.connection.ManagedConnection;
import com.diffdb.model.ConnectionConfig;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.structure.DatabaseObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link SchemaDiffService} backed by Liquibase's diff engine.
 */
public class LiquibaseSchemaDiffService implements SchemaDiffService {

    @Override
    public SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target, SecretResolver secrets)
            throws Exception {

        try (ManagedConnection srcConn = ConnectionManager.open(
                source, secrets.dbPassword(source), secrets.sshSecret(source));
             ManagedConnection tgtConn = ConnectionManager.open(
                     target, secrets.dbPassword(target), secrets.sshSecret(target))) {

            Database refDb = toLiquibaseDatabase(srcConn, source);
            Database cmpDb = toLiquibaseDatabase(tgtConn, target);

            DiffResult diffResult = DiffGeneratorFactory.getInstance()
                    .compare(refDb, cmpDb, new CompareControl());

            List<DiffNode> roots = buildTree(diffResult);
            boolean empty = roots.isEmpty();
            return new SchemaDiffResult(roots, diffResult, empty);
        }
    }

    private Database toLiquibaseDatabase(ManagedConnection mc, ConnectionConfig config) throws Exception {
        Database db = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(mc.getConnection()));
        if (config.getSchema() != null && !config.getSchema().isBlank()) {
            db.setDefaultSchemaName(config.getSchema());
        }
        return db;
    }

    private List<DiffNode> buildTree(DiffResult diff) {
        java.util.List<DiffNode> roots = new java.util.ArrayList<>();

        DiffNode missing = buildCategory("Missing in target (to create)",
                DiffCategory.MISSING, collectByType(diff.getMissingObjects()));
        DiffNode unexpected = buildCategory("Only in target (to drop)",
                DiffCategory.UNEXPECTED, collectByType(diff.getUnexpectedObjects()));
        DiffNode changed = buildChangedCategory(diff.getChangedObjects());

        if (missing.hasChildren()) roots.add(missing);
        if (unexpected.hasChildren()) roots.add(unexpected);
        if (changed.hasChildren()) roots.add(changed);
        return roots;
    }

    private Map<String, java.util.List<DatabaseObject>> collectByType(Set<? extends DatabaseObject> objects) {
        Map<String, java.util.List<DatabaseObject>> byType = new LinkedHashMap<>();
        for (DatabaseObject obj : objects) {
            byType.computeIfAbsent(typeName(obj), k -> new java.util.ArrayList<>()).add(obj);
        }
        return byType;
    }

    private DiffNode buildCategory(String label, DiffCategory category,
                                   Map<String, java.util.List<DatabaseObject>> byType) {
        DiffNode root = DiffNode.container(label);
        for (Map.Entry<String, java.util.List<DatabaseObject>> e : byType.entrySet()) {
            DiffNode typeNode = root.addChild(DiffNode.container(e.getKey()));
            for (DatabaseObject obj : e.getValue()) {
                typeNode.addChild(new DiffNode(e.getKey(), objectName(obj), category, ""));
            }
        }
        return root;
    }

    private DiffNode buildChangedCategory(Map<? extends DatabaseObject, ObjectDifferences> changed) {
        DiffNode root = DiffNode.container("Changed");
        Map<String, java.util.List<Map.Entry<? extends DatabaseObject, ObjectDifferences>>> byType =
                new LinkedHashMap<>();
        for (Map.Entry<? extends DatabaseObject, ObjectDifferences> e : changed.entrySet()) {
            byType.computeIfAbsent(typeName(e.getKey()), k -> new java.util.ArrayList<>()).add(e);
        }
        for (Map.Entry<String, java.util.List<Map.Entry<? extends DatabaseObject, ObjectDifferences>>> typeEntry
                : byType.entrySet()) {
            DiffNode typeNode = root.addChild(DiffNode.container(typeEntry.getKey()));
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> e : typeEntry.getValue()) {
                String detail = summarize(e.getValue());
                typeNode.addChild(new DiffNode(typeEntry.getKey(),
                        objectName(e.getKey()), DiffCategory.CHANGED, detail));
            }
        }
        return root;
    }

    private String summarize(ObjectDifferences differences) {
        StringBuilder sb = new StringBuilder();
        if (differences != null) {
            differences.getDifferences().forEach(d -> {
                if (sb.length() > 0) sb.append("; ");
                sb.append(d.getField());
            });
        }
        return sb.toString();
    }

    private String typeName(DatabaseObject obj) {
        return obj.getClass().getSimpleName();
    }

    private String objectName(DatabaseObject obj) {
        String name = obj.getName();
        return name == null ? obj.toString() : name;
    }
}
