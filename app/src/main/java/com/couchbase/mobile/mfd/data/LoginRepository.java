package com.couchbase.mobile.mfd.data;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.model.LoggedInUser;
import com.couchbase.mobile.mfd.lite.DatabaseManager;
import com.couchbase.mobile.mfd.util.Result;

import java.util.Date;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;
    private LoggedInUser mUser = null;

    // private constructor : singleton access
    private LoginRepository() {
    }

    public static LoginRepository getInstance() {
        if (instance == null) {
            instance = new LoginRepository();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return mUser != null;
    }

    public void logout() {
        mUser = null;
        // TODO: Do we want to record the logout timestamp in the repository
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.mUser = user;
    }

    public Result<LoggedInUser> login(String username, String password) {

        Database repo = DatabaseManager.getSharedInstance().getLocalUserRepository();
        if (repo == null) {
            return new Result.Error(R.string.user_repo_unavailable);
        }

        Document userDoc = repo.getDocument(username);
        if (userDoc == null) {
            return new Result.Error(R.string.no_local_user_found);
        }

        MutableDocument userMDoc = userDoc.toMutable();
        userMDoc.setDate("lastLogin", new Date());

        try {
            repo.save(userMDoc);
        } catch (CouchbaseLiteException e) {
            return new Result.Error(R.string.unable_to_update_local_user, e);
        }

        mUser = new LoggedInUser(username, userDoc.getString("displayName"));
        return new Result.Success<>(mUser);

    }
}