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
import java.util.Date;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Syntax;

/**
 * Primary class for accessing a relational (SQL) database.
 *
 * @author garricko
 */
public interface Database extends Supplier<Database> {
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
  @Nonnull
  @CheckReturnValue
  SqlInsert toInsert(@Syntax("SQL") @Nonnull String sql);

  @Nonnull
  @CheckReturnValue
  SqlInsert toInsert(@Nonnull Sql sql);

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
  @Nonnull
  @CheckReturnValue
  SqlSelect toSelect(@Syntax("SQL") @Nonnull String sql);

  @Nonnull
  @CheckReturnValue
  SqlSelect toSelect(@Nonnull Sql sql);

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
  @Nonnull
  @CheckReturnValue
  SqlUpdate toUpdate(@Syntax("SQL") @Nonnull String sql);

  @Nonnull
  @CheckReturnValue
  SqlUpdate toUpdate(@Nonnull Sql sql);

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
  @Nonnull
  @CheckReturnValue
  SqlUpdate toDelete(@Syntax("SQL") @Nonnull String sql);

  @Nonnull
  @CheckReturnValue
  SqlUpdate toDelete(@Nonnull Sql sql);

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
  @Nonnull
  @CheckReturnValue
  Ddl ddl(@Syntax("SQL") @Nonnull String sql);

  /**
   * Read the next value from a sequence. This method helps smooth over the
   * syntax differences across databases.
   */
  @CheckReturnValue
  Long nextSequenceValue(@Nonnull String sequenceName);

  /**
   * Get the value that would be used if you specify an argNowPerApp() parameter.
   */
  Date nowPerApp();

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
  @Nonnull
  Connection underlyingConnection();

  @Nonnull
  Options options();

  /**
   * Access information about what kind of database we are dealing with.
   */
  @Nonnull
  Flavor flavor();

  /**
   * <p>A little syntax sugar to make it easier to customize your SQL based on the
   * specific database. For example:</p>
   *
   * <pre>"select 1" + db.when().oracle(" from dual")</pre>
   * <pre>"select " + db.when().postgres("date_trunc('day',").other("trunc(") + ") ..."</pre>
   *
   * @return an interface for chaining or terminating the conditionals
   */
  @Nonnull
  When when();

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

  /**
   * Convenience method to check if a table or view exists so that caller can decide
   * whether to create or update a table. The table name's case is normalized using
   * the database's convention unless tableName is enclosed in double quotes.
   * The default catalog and schema from the DB connection will be used.
   *
   * @param tableName the table to be checked
   * @return true if the table or view exists
   */
  boolean tableExists(@Nonnull String tableName);

  /**
   * Convenience method to check whether a table or view exists or not.
   * The table name's case is normalized using the database's convention
   * unless tableName is enclosed in double quotes.  The default catalog
   * from the DB connection will be used.
   *
   * @param tableName the table to be checked
   * @param schemaName the schema expected to contain the table
   * @return true if the table or view exists
   */
  boolean tableExists(@Nonnull String tableName, String schemaName);

  /**
   * Return the DB table name in the normalized form in which it is stored.
   * Databases like Oracle, Derby, HSQL store their tables in upper case.
   * Databases like postgres and sqlserver use lower case unless configured otherwise.
   * If the caller passes in a quoted string, we will leave the name as is, removing
   * the quotes.
   *
   * @param tableName this should be a name, not a pattern
   * @return table name in appropriate format for DB lookup - original case, uppercase, or lowercase
   */
  public String normalizeTableName(String tableName);

  /**
   * Check the JVM time (and timezone) against the database and log a warning
   * or throw an error if they are too far apart. It is a good idea to do this
   * before you store and dates, and maybe make it part of your health checks.
   * If the clocks differ by more than an hour, a DatabaseException is thrown
   * suggesting you check the timezones (under the assumptions the JVM and
   * database are running in different timezones).
   *
   * @param millisToWarn if the clocks disagree by more than this and less than
   *                     millisToError, a warning will be dropped in the log
   * @param millisToError if the clocks disagree by more than this a
   *                      DatabaseEception will be thrown
   */
  void assertTimeSynchronized(long millisToWarn, long millisToError);

  /**
   * Convenience method, same as {@link #assertTimeSynchronized(long, long)}
   * with millisToWarn=10000 and millisToError=30000.
   */
  void assertTimeSynchronized();
}
