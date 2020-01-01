package com.syucream.jiresql;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class FirestoreQueryFactoryTest {

    @Test
    void getTest() throws JireSqlQueryException {
        Firestore firestoreMock = mock(Firestore.class);
        CollectionReference collMock = mock(CollectionReference.class);

        when(firestoreMock.collection(any())).thenReturn(collMock);

        String qs = "SELECT item FROM table";
        FirestoreQueryFactory.get(firestoreMock, qs);

        verify(firestoreMock, times(1)).collection("table");
        verify(collMock, times(1)).select("item");
    }
}
