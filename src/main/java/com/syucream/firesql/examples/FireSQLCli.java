package com.syucream.firesql.examples;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.syucream.firesql.FirestoreQueryFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class FireSQLCli {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java <Main> <path to credential json file> <select statement>");
      System.exit(1);
    }

    final String pathToCredJson = args[0];
    final String sql = args[1];

    try {
      InputStream serviceAccount = new FileInputStream(pathToCredJson);
      GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

      FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).build();
      FirebaseApp.initializeApp(options);
    } catch (Exception e) {
      System.err.printf("Firestore client initialization failed: %s\n", e.toString());
      System.exit(1);
    }

    Firestore db = FirestoreClient.getFirestore();

    try {
      Query q = FirestoreQueryFactory.get(db, sql);
      List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
      for (QueryDocumentSnapshot d : docs) {
        System.out.println(d.toString());
      }
    } catch (Exception e) {
      System.err.printf("Firestore query execution failed: %s\n", e.toString());
      System.exit(1);
    }
  }
}
