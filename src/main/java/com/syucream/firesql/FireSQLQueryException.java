package com.syucream.firesql;

/** An exception indicates SQL query is not able to convert to Firestore query */
public class FireSQLQueryException extends Exception {
  public FireSQLQueryException(String message) {
    super(message);
  }
}
