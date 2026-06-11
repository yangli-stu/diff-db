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
    public SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target, SecretResolver secrets,
                                 ProgressListener listener)
            throws Exception {

        return LiquibaseScope.run(() -> {
            step(listener, "Connecting to source database...");
            try (ManagedConnection srcConn = ConnectionManager.open(
                    source, secrets.dbPassword(source), secrets.sshSecret(source))) {

                step(listener, "Connecting to target database...");
                try (ManagedConnection tgtConn = ConnectionManager.open(
                        target, secrets.dbPassword(target), secrets.sshSecret(target))) {

                    step(listener, "Reading source schema...");
                    Database refDb = toLiquibaseDatabase(srcConn, source);

                    step(listener, "Reading target schema...");
                    Database cmpDb = toLiquibaseDatabase(tgtConn, target);

                    step(listener, "Comparing schemas...");
                    DiffResult diffResult = DiffGeneratorFactory.getInstance()
                            .compare(refDb, cmpDb, new CompareControl());

                    step(listener, "Building diff result...");
                    List<DiffNode> roots = buildTree(diffResult);
                    boolean empty = roots.isEmpty();
                    String srcSchema = source.getEffectiveSchema();
                    if (srcSchema == null || srcSchema.isBlank()) srcSchema = refDb.getDefaultSchemaName();
                    String tgtSchema = target.getEffectiveSchema();
                    if (tgtSchema == null || tgtSchema.isBlank()) tgtSchema = cmpDb.getDefaultSchemaName();
                    return new SchemaDiffResult(roots, diffResult, empty, srcSchema, tgtSchema);
                }
            }
        });
    }

    private static void step(ProgressListener listener, String message) {
        if (listener != null) {
            listener.onStep(message);
        }
    }

    private Database toLiquibaseDatabase(ManagedConnection mc, ConnectionConfig config) throws Exception {
        Database db = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(mc.getConnection()));
        if (config.getSchema() != null && !config.getSchema().isBlank()) {
            db.setDefaultSchemaName(config.getSchema());
        }
        // Avoid repeated Liquibase metadata table lookups during snapshot; speeds up large schemas
        db.setCanCacheLiquibaseTableInfo(true);
        return db;
    }

    /** Exposed for TwoStepDiffService. */
    public List<DiffNode> buildTreePublic(DiffResult diff) {
        return buildTree(diff);
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
                // Skip if the only difference was column ordering
                if (detail.isEmpty()) {
                    continue;
                }
                typeNode.addChild(new DiffNode(typeEntry.getKey(),
                        objectName(e.getKey()), DiffCategory.CHANGED, detail));
            }
        }
        return root;
    }

    private String summarize(ObjectDifferences differences) {
        StringBuilder sb = new StringBuilder();
        if (differences != null) {
            for (liquibase.diff.Difference d : differences.getDifferences()) {
                String field = d.getField();
                // Skip column ordering / position changes — irrelevant for structural diff
                if (field != null && (field.equalsIgnoreCase("ordering") || field.equalsIgnoreCase("order"))) {
                    continue;
                }
                if (sb.length() > 0) sb.append("; ");
                Object oldVal = d.getReferenceValue();
                Object newVal = d.getComparedValue();
                String oldStr = oldVal == null ? "null" : oldVal.toString();
                String newStr = newVal == null ? "null" : newVal.toString();
                // Skip meaningless diffs where string representation is identical
                // (e.g. Integer(0) vs Long(0) — different types but same value)
                if (oldStr.equals(newStr)) {
                    continue;
                }
                // Truncate very long values
                if (oldStr.length() > 40) oldStr = oldStr.substring(0, 37) + "...";
                if (newStr.length() > 40) newStr = newStr.substring(0, 37) + "...";
                sb.append(field).append(": ").append(oldStr).append(" -> ").append(newStr);
            }
        }
        return sb.toString();
    }

    private String typeName(DatabaseObject obj) {
        return obj.getClass().getSimpleName();
    }

    private String objectName(DatabaseObject obj) {
        String name = obj.getName();
        if (name == null) {
            return obj.toString();
        }
        // Try multiple attribute names for parent table/relation
        DatabaseObject parent = findParent(obj);
        if (parent != null && parent.getName() != null) {
            name = parent.getName() + "." + name;
        }
        return name;
    }

    private DatabaseObject findParent(DatabaseObject obj) {
        String[] attrNames = {"relation", "table", "foreignKeyTable", "primaryKeyTable"};
        for (String attr : attrNames) {
            Object raw = obj.getAttribute(attr, Object.class);
            if (raw instanceof DatabaseObject parent) {
                return parent;
            }
        }
        return null;
    }
}
