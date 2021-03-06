package me.prettyprint.cassandra.dao;

import static me.prettyprint.cassandra.utils.StringUtils.bytes;
import static me.prettyprint.cassandra.utils.StringUtils.string;
import me.prettyprint.cassandra.service.CassandraClient;
import me.prettyprint.cassandra.service.CassandraClientPool;
import me.prettyprint.cassandra.service.Keyspace;

import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.NotFoundException;

public class ExampleSpringDao {
  // statics for default values
  private static String CASSANDRA_KEYSPACE = "Keyspace1";
  private static String CF_NAME = "Standard1";
  private static String COLUMN_NAME = "v";
  // configurable values
  private String keyspace = CASSANDRA_KEYSPACE;
  private String columnFamilyName = CF_NAME;
  private String columnName = COLUMN_NAME;
  private ConsistencyLevel consistencyLevel = CassandraClient.DEFAULT_CONSISTENCY_LEVEL;
  // collaborator
  private CassandraClientPool cassandraClientPool;

  /**
   * Must be constructed with a CassandraClientPool
   * @param cassandraClientPool
   */
  public ExampleSpringDao(CassandraClientPool cassandraClientPool) {
    this.cassandraClientPool = cassandraClientPool;
  }

  /**
   * Insert a new value keyed by key
   * @param key Key for the value
   * @param value the String value to insert
   */
  public void insert(final String key, final String value) throws Exception {
    execute(new SpringCommand<Void>(cassandraClientPool){
      public Void execute(final Keyspace ks) throws Exception {
        ks.insert(key, createColumnPath(columnName), bytes(value));
        return null;
      }
    });
  }

  /**
   * Get a string value.
   * @return The string value; null if no value exists for the given key.
   */
  public String get(final String key) throws Exception {
    return execute(new SpringCommand<String>(cassandraClientPool){
      public String execute(final Keyspace ks) throws Exception {
        try {
          return string(ks.getColumn(key, createColumnPath(columnName)).getValue());
        } catch (NotFoundException e) {
          return null;
        }
      }
    });
  }

  /**
   * Delete a key from cassandra
   */
  public void delete(final String key) throws Exception {
    execute(new SpringCommand<Void>(cassandraClientPool){
      public Void execute(final Keyspace ks) throws Exception {
        ks.remove(key, createColumnPath(columnName));
        return null;
      }
    });
  }


  protected ColumnPath createColumnPath(String columnName) {
    return new ColumnPath(columnFamilyName).setColumn(bytes(columnName));
  }

  protected <T> T execute(SpringCommand<T> command) throws Exception {
    return command.execute(keyspace, consistencyLevel);
  }


  public void setKeyspace(String keyspace) {
    this.keyspace = keyspace;
  }

  public void setColumnFamilyName(String columnFamilyName) {
    this.columnFamilyName = columnFamilyName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
  }
}




