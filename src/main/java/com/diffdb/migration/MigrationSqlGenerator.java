package com.diffdb.migration;

import com.diffdb.diff.SchemaDiffResult;

/**
 * Generates migration SQL that brings the target schema in line with the source,
 * based on a previously computed {@link SchemaDiffResult}.
 */
public interface MigrationSqlGenerator {

    String generate(SchemaDiffResult diff, MigrationOptions options) throws Exception;
}
