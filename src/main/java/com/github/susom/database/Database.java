/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.susom.database;

import java.sql.Connection;

import javax.inject.Provider;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * Primary class for accessing a relational (SQL) database.
 *
 * @author garricko
 */
public interface Database extends Provider<Database> {
  /**
   * Create a SQL "insert" statement for further manipulation and execution.
   * Note this call does not actually execute the SQL.
   *
   * @param sql the SQL to execute, optionally containing indexed ("?") or
   *            named (":foo") parameters. To include the characters '?' or ':'
   *            in the SQL you must escape them with two ("??" or "::"). You
   *            MUST be careful not to pass untrusted strings in as SQL, since
   *            this will be executed in the database.
   * @return an interface for further manipulating the statement; never null
   */
  @NotNull
  SqlInsert insert(@Language("SQL") @NotNull String sql);

  /**
   * Create a SQL "select" statement for further manipulation and execution.
   * Note this call does not actually execute the SQL.
   *
   * @param sql the SQL to execute, optionally containing indexed ("?") or
   *            named (":foo") parameters. To include the characters '?' or ':'
   *            in the SQL you must escape them with two ("??" or "::"). You
   *            MUST be careful not to pass untrusted strings in as SQL, since
   *            this will be executed in the database.
   * @return an interface for further manipulating the statement; never null
   */
  @NotNull
  SqlSelect select(@Language("SQL") @NotNull String sql);

  /**
   * Create a SQL "update" statement for further manipulation and execution.
   * Note this call does not actually execute the SQL.
   *
   * @param sql the SQL to execute, optionally containing indexed ("?") or
   *            named (":foo") parameters. To include the characters '?' or ':'
   *            in the SQL you must escape them with two ("??" or "::"). You
   *            MUST be careful not to pass untrusted strings in as SQL, since
   *            this will be executed in the database.
   * @return an interface for further manipulating the statement; never null
   */
  @NotNull
  SqlUpdate update(@Language("SQL") @NotNull String sql);

  /**
   * Create a SQL "delete" statement for further manipulation and execution.
   * Note this call does not actually execute the SQL.
   *
   * @param sql the SQL to execute, optionally containing indexed ("?") or
   *            named (":foo") parameters. To include the characters '?' or ':'
   *            in the SQL you must escape them with two ("??" or "::"). You
   *            MUST be careful not to pass untrusted strings in as SQL, since
   *            this will be executed in the database.
   * @return an interface for further manipulating the statement; never null
   */
  @NotNull
  SqlUpdate delete(@Language("SQL") @NotNull String sql);

  /**
   * Create a DDL (schema modifying) statement for further manipulation and execution.
   * Note this call does not actually execute the SQL.
   *
   * @param sql the SQL to execute, optionally containing indexed ("?") or
   *            named (":foo") parameters. To include the characters '?' or ':'
   *            in the SQL you must escape them with two ("??" or "::"). You
   *            MUST be careful not to pass untrusted strings in as SQL, since
   *            this will be executed in the database.
   * @return an interface for further manipulating the statement; never null
   */
  @NotNull
  Ddl ddl(@Language("SQL") @NotNull String sql);

  /**
   * Read the next value from a sequence. This method helps smooth over the
   * syntax differences across databases.
   */
  Long nextSequenceValue(@NotNull String sequenceName);

  /**
   * Cause the underlying connection to commit its transaction immediately. This
   * must be explicitly enabled (see {@link com.github.susom.database.Options},
   * or it will throw a {@link com.github.susom.database.DatabaseException}.
   */
  void commitNow();

  /**
   * Cause the underlying connection to roll back its transaction immediately. This
   * must be explicitly enabled (see {@link com.github.susom.database.Options},
   * or it will throw a {@link com.github.susom.database.DatabaseException}.
   */
  void rollbackNow();

  /**
   * <p>Obtain direct access to the connection being used by this instance. Be very
   * careful as this is highly likely to be unsafe and cause you great pain and
   * suffering. This method is included to help ease into the library in large
   * codebases where some parts still rely on direct JDBC access.</p>
   *
   * <p>By default this method will throw a {@link DatabaseException}. If you want
   * to use this method you must explicitly enable it via
   * {@link com.github.susom.database.Options#allowConnectionAccess()}</p>
   */
  @NotNull
  Connection underlyingConnection();

  /**
   * Access information about what kind of database we are dealing with.
   */
  @NotNull
  Flavor flavor();

  /**
   * <p>A little syntax sugar to make it easier to customize your SQL based on the
   * specific database. For example:</p>
   *
   * <pre>"select 1" + db.when(Flavor.oracle, " from dual").otherwise("")</pre>
   *
   * @param flavor the current database will be checked to see if it is this flavor
   * @param sql this string will be the return value if the flavor matches
   * @return an interface for chaining or terminating the conditionals
   */
  @NotNull
  When when(@NotNull Flavor flavor, @NotNull String sql);

  /**
   * Convenience method to deal with mutually incompatible syntax for this. For example:
   *
   * <p>Oracle: 'drop sequence x'</p>
   * <p>Derby: 'drop sequence x restrict'</p>"
   */
  void dropSequenceQuietly(String sequenceName);

  /**
   * Convenience method to deal with dropping tables that may or may not exist. Some
   * databases make it hard to check and conditionally drop things, so we will just
   * try to drop it and ignore the errors.
   *
   * @param tableName the table to be dropped
   */
  void dropTableQuietly(String tableName);

  interface When {
    @NotNull
    When when(@NotNull Flavor flavor, @NotNull String sql);
    @NotNull String otherwise(@NotNull String sql);
  }
}
