package com.couchbase.mobile.mfd.data;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.lite.DatabaseManager;
import com.couchbase.mobile.mfd.util.Result;

import java.util.Date;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;
    private User mUser = null;

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

    private void setLoggedInUser(User user) {
        this.mUser = user;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Result<User> login(String username, String password) {

        Database repo = DatabaseManager.getSharedInstance().getLocalUserRepository();
        if (repo == null) {
            return new Result.Error(R.string.user_repo_unavailable);
        }

        Document userDoc = repo.getDocument(username);
        if (userDoc == null) {
            return new Result.Error(R.string.no_local_user_found);
        }

        String existingPwd = userDoc.getString(User.ATR_PASSWORD);
        if( !existingPwd.equals(password)) {
            return new Result.Error(R.string.wrong_password);
        }
        MutableDocument userMDoc = userDoc.toMutable();
        userMDoc.setDate(User.ATR_LAST_LOGIN, new Date());

        try {
            repo.save(userMDoc);
        } catch (CouchbaseLiteException e) {
            return new Result.Error(R.string.unable_to_update_local_user, e);
        }

        mUser = new User(username, userDoc.getString(User.ATR_DISPLAY_NAME));
        return new Result.Success<>(mUser);

    }

    public Result<User>  registerUser(String username, String password, String displayName) {
        Database repo = DatabaseManager.getSharedInstance().getLocalUserRepository();
        if (repo == null) {
            return new Result.Error(R.string.user_repo_unavailable);
        }
        MutableDocument registrant = new MutableDocument(username);
        registrant.setString(User.ATR_USERNAME, username);
        registrant.setString(User.ATR_PASSWORD, password);
        registrant.setString(User.ATR_DISPLAY_NAME, displayName);
        registrant.setDate(User.ATR_LAST_LOGIN, new Date());
        try {
            repo.save(registrant);
        } catch (CouchbaseLiteException e) {
            return new Result.Error(R.string.unable_to_update_local_user, e);
        }
        mUser = new User(username, displayName);
        return new Result.Success<>(mUser);
    }
}