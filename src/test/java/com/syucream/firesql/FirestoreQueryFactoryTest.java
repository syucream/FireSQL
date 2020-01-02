package com.syucream.firesql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.parser.SimpleNode;
import org.apache.commons.lang3.tuple.Pair;
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
    when(queryMock.whereEqualTo(anyString(), any())).thenReturn(queryMock);
    when(queryMock.whereGreaterThan(anyString(), any())).thenReturn(queryMock);
    when(queryMock.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(queryMock);
    when(queryMock.whereLessThan(anyString(), any())).thenReturn(queryMock);
    when(queryMock.whereLessThanOrEqualTo(anyString(), any())).thenReturn(queryMock);
  }

  @Test
  void getTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
  }

  @Test
  void getWithWhereEqualsToTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item = 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereEqualTo("item", 42L);
  }

  @Test
  void getWithWhereGreaterThanTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item > 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereGreaterThan("item", 42L);
  }

  @Test
  void getWithWhereGreaterThanOrEqualsToTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item >= 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereGreaterThanOrEqualTo("item", 42L);
  }

  @Test
  void getWithWhereLessThanTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item < 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereLessThan("item", 42L);
  }

  @Test
  void getWithWhereLessThanOrEqualsToTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item <= 42";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereLessThanOrEqualTo("item", 42L);
  }

  @Test
  void getAndTest() throws FireSQLQueryException {
    final String qs = "SELECT item FROM table WHERE item > 0 AND item < 100";

    FirestoreQueryFactory.get(firestoreMock, qs);

    verify(firestoreMock, times(1)).collection("table");
    verify(collMock, times(1)).select("item");
    verify(queryMock, times(1)).whereGreaterThan("item", 0L);
    verify(queryMock, times(1)).whereLessThan("item", 100L);
  }

  @Test
  void extractValueTest() throws FireSQLQueryException {
    // pairs of <argument, expected>
    final List<Pair<Expression, Object>> validValues =
        Arrays.asList(
            Pair.of(new DoubleValue("4.2"), 4.2),
            Pair.of(new LongValue(42L), 42L),
            Pair.of(new StringValue("test"), "test"),
            Pair.of(
                new TimestampValue("2000-01-02 12:34:56"), new java.sql.Timestamp(946784096000L)),
            Pair.of(new NullValue(), null));

    for (Pair<Expression, Object> p : validValues) {
      Object actual = FirestoreQueryFactory.extractValue(p.getLeft());
      assertEquals(actual, p.getRight());
    }

    final List<Expression> invalidValues =
        Arrays.asList(
            null,
            new Expression() {
              @Override
              public void accept(ExpressionVisitor expressionVisitor) {}

              @Override
              public SimpleNode getASTNode() {
                return null;
              }

              @Override
              public void setASTNode(SimpleNode node) {}
            });

    for (Expression e : invalidValues) {
      assertThrows(FireSQLQueryException.class, () -> FirestoreQueryFactory.extractValue(e));
    }
  }
}
