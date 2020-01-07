package com.syucream.firesql;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;

/**
 * FireSQL client class, generate a Firestore query with Firestore connection and SQL query string
 */
public class FireSQL {
  private Firestore db;

  public FireSQL(Firestore db) {
    this.db = db;
  }

  /**
   * Get a Firestore query from SQL query string
   *
   * @param qs a SQL query string
   * @return a Firestore query
   * @throws FireSQLQueryException a query creation exception
   */
  public ApiFuture<QuerySnapshot> query(String qs) throws FireSQLQueryException {
    Query q = FirestoreQueryFactory.get(db, qs);
    return q.get();
  }
}
