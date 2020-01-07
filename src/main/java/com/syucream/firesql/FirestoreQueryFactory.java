package com.syucream.firesql;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class FirestoreQueryFactory {

  /**
   * Get Firestore query object from SQL query string with parsing it as a SQL statement.
   *
   * @param db Firestore connection
   * @param qs a SQL query string
   * @return a Firestore query object
   * @throws FireSQLQueryException a query creation exception
   */
  public static Query get(Firestore db, String qs) throws FireSQLQueryException {
    Select select;
    try {
      Statement stmt = CCJSqlParserUtil.parse(qs);
      select = (Select) stmt;
    } catch (JSQLParserException | ClassCastException e) {
      throw new FireSQLQueryException(e.getMessage());
    }

    // SELECT
    final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    for (SelectItem i : selectItems) {
      if (i instanceof SelectExpressionItem) {
        SelectExpressionItem ii = (SelectExpressionItem) i;
        if (ii.getExpression() instanceof Function) {
          throw new FireSQLQueryException("The function is unsupported: " + i.toString());
        }
      }
    }
    final String[] items = selectItems.stream().map(Object::toString).toArray(String[]::new);

    // FROM
    final TablesNamesFinder tableNamesFinder = new TablesNamesFinder();
    final List<String> tables = tableNamesFinder.getTableList(select);
    if (tables.size() != 1) {
      throw new FireSQLQueryException("too many table names");
    }

    Query q = db.collection(tables.get(0)).select(items);

    // WHERE
    final Expression where = plainSelect.getWhere();
    if (Objects.nonNull(where)) {
      q = extractExpression(q, where);
    }

    // LIMIT
    final Limit limit = plainSelect.getLimit();
    if (Objects.nonNull(limit) && !limit.isLimitNull() && !limit.isLimitAll()) {
      final long v = ((LongValue) limit.getRowCount()).getValue();
      q = q.limit(Math.toIntExact(v));
    }

    // OFFSET
    final Offset offset = plainSelect.getOffset();
    if (Objects.nonNull(offset)) {
      final long offsetVal = offset.getOffset();
      q = q.offset(Math.toIntExact(offsetVal));
    }

    // ORDER BY
    final List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
    if (Objects.nonNull(orderByElements)) {
      for (OrderByElement e : orderByElements) {
        final String field = e.getExpression().toString();
        final Direction direction = e.isAsc() ? Direction.ASCENDING : Direction.DESCENDING;
        q = q.orderBy(field, direction);
      }
    }

    return q;
  }

  /**
   * Extract a SQL expression be able to handle in Firestore
   *
   * <p>TODO support array-contains
   *
   * @param q
   * @param e
   * @return
   * @throws FireSQLQueryException
   */
  static Query extractExpression(Query q, Expression e) throws FireSQLQueryException {
    if (e instanceof EqualsTo) {
      final EqualsTo eq = (EqualsTo) e;
      final String left = eq.getLeftExpression().toString();
      final Object right = extractValue(eq.getRightExpression());

      q = q.whereEqualTo(left, right);
    } else if (e instanceof GreaterThan) {
      final GreaterThan gt = (GreaterThan) e;
      final String left = gt.getLeftExpression().toString();
      final Object right = extractValue(gt.getRightExpression());

      q = q.whereGreaterThan(left, right);
    } else if (e instanceof GreaterThanEquals) {
      final GreaterThanEquals gteq = (GreaterThanEquals) e;
      final String left = gteq.getLeftExpression().toString();
      final Object right = extractValue(gteq.getRightExpression());

      q = q.whereGreaterThanOrEqualTo(left, right);
    } else if (e instanceof MinorThan) {
      final MinorThan mt = (MinorThan) e;
      final String left = mt.getLeftExpression().toString();
      final Object right = extractValue(mt.getRightExpression());

      q = q.whereLessThan(left, right);
    } else if (e instanceof MinorThanEquals) {
      final MinorThanEquals mteq = (MinorThanEquals) e;
      final String left = mteq.getLeftExpression().toString();
      final Object right = extractValue(mteq.getRightExpression());

      q = q.whereLessThanOrEqualTo(left, right);
    } else if (e instanceof AndExpression) {
      final AndExpression and = (AndExpression) e;
      q = extractExpression(q, and.getLeftExpression());
      q = extractExpression(q, and.getRightExpression());
    } else {
      throw new FireSQLQueryException("unsupported expression: " + e.toString());
    }

    return q;
  }

  /**
   * Extract a SQL value as a Firestore value
   *
   * @param e
   * @return
   * @throws FireSQLQueryException
   */
  static Object extractValue(Expression e) throws FireSQLQueryException {
    Object rv;

    if (e instanceof DoubleValue) {
      rv = ((DoubleValue) e).getValue();
    } else if (e instanceof LongValue) {
      rv = ((LongValue) e).getValue();
    } else if (e instanceof StringValue) {
      rv = ((StringValue) e).getValue();
    } else if (e instanceof TimestampValue) {
      rv = ((TimestampValue) e).getValue();
    } else if (e instanceof NullValue) {
      rv = null;
    } else {
      throw new FireSQLQueryException("unsupported value in expression");
    }

    return rv;
  }
}
