package com.syucream.jiresql;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.List;

public class FirestoreQueryFactory {

    public static Query get(Firestore db, String qs) throws JireSqlQueryException {
        Select select;
        try {
            Statement stmt = CCJSqlParserUtil.parse(qs);
            select = (Select) stmt;
        } catch (JSQLParserException e) {
            throw new JireSqlQueryException(e.getMessage());
        }

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        String[] items = plainSelect.getSelectItems().stream().map(Object::toString).toArray(String[]::new);

        TablesNamesFinder tableNamesFinder = new TablesNamesFinder();
        List<String> tables = tableNamesFinder.getTableList(select);
        if (tables.size() != 1) {
            throw new JireSqlQueryException("too many table names");
        }

        return db.collection(tables.get(0))
                .select(items);
    }

}
