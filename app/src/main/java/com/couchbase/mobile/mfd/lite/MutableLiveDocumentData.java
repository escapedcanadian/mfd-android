package com.couchbase.mobile.mfd.lite;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;

public class MutableLiveDocumentData<T> extends MutableLiveData<T> implements Observer<T> {

    private String[] mPath;
    private MutableDocument mDocument;
    private int mBranches;
    private boolean mPost;

    public MutableLiveDocumentData(MutableDocument document, boolean post, String ... path) {
        super();
        mDocument = document;
        mPath = path;
        mBranches = mPath.length - 1;
        mPost = post;
        readValue();
        observeForever(this);
    }

    @SuppressWarnings("unchecked")
    private void readValue() {
        T value;
        if (mBranches < 1) {
            value = (T) mDocument.getValue(mPath[mBranches]);
        } else {
            Dictionary dictionary = mDocument.getDictionary(mPath[0]);
            for (int i = 1; i < mBranches; i++) {
                dictionary = dictionary.getDictionary(mPath[i]);
            }
            value = (T) dictionary.getValue(mPath[mBranches]);
        }
        if (mPost) {
            postValue(value);
        } else {
            setValue(value);
        }
    }

    public void reload() {
        readValue();
    }

    private void saveValue(T value) {

        if (mBranches < 1) {
            mDocument.setValue(mPath[mBranches], value);
        } else {
            MutableDictionary baseDictionary = mDocument.getDictionary(mPath[0]);
            if (baseDictionary == null) {
                baseDictionary = new MutableDictionary();
                mDocument.setDictionary(mPath[0], baseDictionary);
            }
            for (int i=1; i < mBranches; i++) {
                MutableDictionary branchDictionary = baseDictionary.getDictionary(mPath[i]);
                if (branchDictionary == null) {
                    branchDictionary = new MutableDictionary();
                    baseDictionary.setDictionary(mPath[i], branchDictionary);
                }
                baseDictionary = branchDictionary;
            }
            baseDictionary.setValue(mPath[mBranches], value);
        }
    }

    public void onChanged(T t) {
        saveValue(t);
    }

}
