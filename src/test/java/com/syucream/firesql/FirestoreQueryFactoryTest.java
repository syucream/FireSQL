package com.syucream.firesql;

import static org.mockito.Mockito.*;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FirestoreQueryFactoryTest {
  private Firestore firestoreMock;
  private CollectionReference collMock;
  private Query queryMock;

  @BeforeEach
  void init() {
    firestoreMock = mock(Firestore.class);
    collMock = mock(CollectionReference.class);
    queryMock = mock(Query.class);

    when(firestoreMock.collection(any())).thenReturn(collMock);
    when(collMock.select(anyString())).thenReturn(queryMock);
  }

  @Test
  void getTest() throws FireSQLQueryException {
    String qs = "SELECT item FROM table";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
  }

  @Test
  void getTestWithWhereEqualsTo() throws FireSQLQueryException {
    String qs = "SELECT item FROM table WHERE item = 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereEqualTo("item", 42L);
  }

  @Test
  void getTestWithWhereGreaterThan() throws FireSQLQueryException {
    String qs = "SELECT item FROM table WHERE item > 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereGreaterThan("item", 42L);
  }

  @Test
  void getTestWithWhereGreaterThanOrEqualsTo() throws FireSQLQueryException {
    String qs = "SELECT item FROM table WHERE item >= 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereGreaterThanOrEqualTo("item", 42L);
  }

  @Test
  void getTestWithWhereLessThan() throws FireSQLQueryException {
    String qs = "SELECT item FROM table WHERE item < 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereLessThan("item", 42L);
  }

  @Test
  void getTestWithWhereLessThanOrEqualsTo() throws FireSQLQueryException {
    String qs = "SELECT item FROM table WHERE item <= 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereLessThanOrEqualTo("item", 42L);
  }
}
