package com.syucream.firesql;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;

public class FireSQL {
  private Firestore db;

  public FireSQL(Firestore db) {
    this.db = db;
  }

  public ApiFuture<QuerySnapshot> query(String qs) throws FireSQLQueryException {
    Query q = FirestoreQueryFactory.get(db, qs);
    return q.get();
  }
}
