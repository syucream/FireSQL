# FireSQL

A SQL-like interface to [Google Cloud Firestore](https://firebase.google.com/docs/firestore) in Java

# Synopsis

SDKs for Google Cloud Firestore provides high-level query builder stuff like:

```java
...
Firestore db = FirestoreClient.getFirestore();

Query query = db
    .collection("users")
    .select("id", "name", "age")
    .whereGreaterThanOrEqualTo("age", 30L)
    .whereLessThan("age", 40L)
    .orderBy("id", Query.Direction.ASCENDING)
    .limit(100)
    .offset(200);

query.get();
...
```

But SQL will be sometimes wanted, e.g. executing extraction queries periodically, non coders want to execute custom query on their operations, etc.
So that, FireSQL supports primitive SQL-like interface, like below:

```java 
...
Firestore db = FirestoreClient.getFirestore();

FireSQL firesql = new FireSQL(db);
Query query = firesql.query("SELECT id, name, age FROM users WHERE age >= 30 AND age < 40 ORDER BY id LIMIT 100 OFFSET 200");

query.get();
...
```

# Limitations

- Only `SELECT` supported
- `JOIN`, `GROUP BY`, aggregation functions, sub queries are unsupported
- `NOT`, `IN` and some other expression are unsupported
- `array-contains` expression in Firestore query is unsuported for now

# References

- JavaScript implementation https://github.com/jsayol/FireSQL
