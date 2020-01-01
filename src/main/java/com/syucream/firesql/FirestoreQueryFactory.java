package com.syucream.firesql;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class FirestoreQueryFactory {

  public static Query get(Firestore db, String qs) throws FireSQLQueryException {
    Select select;
    try {
      Statement stmt = CCJSqlParserUtil.parse(qs);
      select = (Select) stmt;
    } catch (JSQLParserException e) {
      throw new FireSQLQueryException(e.getMessage());
    }

    final PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    final String[] items =
        plainSelect.getSelectItems().stream().map(Object::toString).toArray(String[]::new);

    final TablesNamesFinder tableNamesFinder = new TablesNamesFinder();
    final List<String> tables = tableNamesFinder.getTableList(select);
    if (tables.size() != 1) {
      throw new FireSQLQueryException("too many table names");
    }

    Query q = db.collection(tables.get(0)).select(items);

    final Expression where = plainSelect.getWhere();
    if (Objects.nonNull(where)) {
      q = extractExpression(q, where);
    }

    return q;
  }

  public static Query extractExpression(Query q, Expression e) throws FireSQLQueryException {
    // TODO support nested

    // TODO support more expression types
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
    } else {
      throw new FireSQLQueryException("unsupported expression");
    }

    return q;
  }

  public static Object extractValue(Expression e) throws FireSQLQueryException {
    Object rv;

    if (e instanceof LongValue) {
      rv = ((LongValue) e).getValue();
    } else if (e instanceof DoubleValue) {
      rv = ((DoubleValue) e).getValue();
    } else if (e instanceof StringValue) {
      rv = ((StringValue) e).getValue();
    } else {
      throw new FireSQLQueryException("unsupported value in expression");
    }

    return rv;
  }
}
