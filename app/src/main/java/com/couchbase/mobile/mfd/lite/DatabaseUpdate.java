package com.couchbase.mobile.mfd.lite;

import com.couchbase.lite.Document;

import java.util.Date;

public class DatabaseUpdate {
    private DatabaseWrapper mDatabase;
    private String mDocId;
    private Document mDocument;
    private Date mTimestamp;

    public DatabaseUpdate (DatabaseWrapper database, String docId, Document document) {
        mDatabase = database;
        mDocId = docId;
        mDocument = document;
        mTimestamp = new Date();
    }

    public boolean isDelete() {
        return mDocument == null;
    }

    public DatabaseWrapper getDatabase() {
        return mDatabase;
    }

    public String getDocId() {
        return mDocId;
    }

    public Document getDocument() {
        return mDocument;
    }

    public Date getTimestamp() {
        return mTimestamp;
    }
}
