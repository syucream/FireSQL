package com.syucream.jiresql;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;

public class JireSql {
  private Firestore db = null;

  public JireSql(Firestore db) {
    this.db = db;
  }

  public ApiFuture<QuerySnapshot> query(String qs) throws JireSqlQueryException {
    Query q = FirestoreQueryFactory.get(db, qs);
    return q.get();
  }
}
