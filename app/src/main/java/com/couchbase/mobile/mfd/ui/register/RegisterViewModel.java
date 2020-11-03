package com.couchbase.mobile.mfd.ui.register;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.couchbase.mobile.mfd.R;
import com.couchbase.mobile.mfd.data.LoginRepository;
import com.couchbase.mobile.mfd.data.model.User;
import com.couchbase.mobile.mfd.util.ResourceLocalizer;
import com.couchbase.mobile.mfd.util.Result;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterViewModel extends ViewModel {
    private static final String LOG_TAG = "MFD_RegisterViewModel";
    private int mMinUsernameLength = ResourceLocalizer.getSettingsInt(R.integer.min_username_length);
    private int mMinPasswordLength = ResourceLocalizer.getSettingsInt(R.integer.min_password_length);

    private LoginRepository mLoginRepository;

    private MutableLiveData<String> mUsername = new MutableLiveData<>();
    private MutableLiveData<String> mPassword1 = new MutableLiveData<>();
    private MutableLiveData<String> mPassword2 = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCommunicationOptIn = new MutableLiveData<Boolean>(false);
    private MutableLiveData<String> mFirstName = new MutableLiveData<>();
    private MutableLiveData<String> mLastName = new MutableLiveData<>();
    private MutableLiveData<String> mCompany = new MutableLiveData<>();
    private MutableLiveData<String> mEmail = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAttemptPossible = new MutableLiveData<>(false);
    private MutableLiveData<String> mRegistrationError = new MutableLiveData<>();
    private MutableLiveData<String> mProfileError = new MutableLiveData<>();

    public MutableLiveData<String> getUsername() {
        return mUsername;
    }
    public MutableLiveData<String> getPassword1() {
        return mPassword1;
    }
    public MutableLiveData<String> getPassword2() {
        return mPassword2;
    }
    public MutableLiveData<Boolean> getOptIn() {return mCommunicationOptIn;}
    public MutableLiveData<String> getFirstName() {return mFirstName;}
    public MutableLiveData<String> getLastName() {return mLastName;}
    public MutableLiveData<String> getCompany() {return mCompany;}
    public MutableLiveData<String> getEmail() {return mEmail;}
    public LiveData<Boolean> isAttemptPossible() {
        return mAttemptPossible;
    }
    public MutableLiveData<String> getRegistrationError() {
        return mRegistrationError;
    }
    public MutableLiveData<String> getProfileError() {
        return mProfileError;
    }

    public RegisterViewModel(LoginRepository loginRepository) {

        super();
        mLoginRepository = loginRepository;
        Observer<String> observer = (String s) -> {
            RegisterViewModel.this.checkIfAttemptPossible();
        };
        mUsername.observeForever(observer);
        mPassword1.observeForever(observer);
        mPassword2.observeForever(observer);
        mFirstName.observeForever(observer);
        mLastName.observeForever(observer);
//        mCompany.observeForever(observer);
        mEmail.observeForever(observer);
    }

    private void checkIfAttemptPossible() {
        String currentUsername = mUsername.getValue();
        if(currentUsername == null || usernameMeetsRequirements(currentUsername) ) {
            setErrorMessage(R.string.username_requirements);
            return;
        }
        String currentPassword1 = mPassword1.getValue();
        if(currentPassword1 == null || passwordMeetsRequirements(currentPassword1))  {
            setErrorMessage(R.string.password_requirements);
            return;
        }
        String currentPassword2 = mPassword2.getValue();
        if(currentPassword2 == null || !currentPassword2.equals(currentPassword1)) {
            setErrorMessage(R.string.password_mismatch);
            return;
        }
        if(mCommunicationOptIn.getValue()) {
            String firstName = mFirstName.getValue();
            if(firstName == null || firstName.isEmpty()) {
                setProfileErrorMessage(R.string.first_name_invalid);
                return;
            }
            String lastName = mLastName.getValue();
            if(lastName == null || lastName.isEmpty()) {
                setProfileErrorMessage(R.string.last_name_invalid);
                return;
            }
            String email = mEmail.getValue();
            if(email == null || !isValidEmail(email)) {
                setProfileErrorMessage(R.string.email_invalid);
                return;
            }
        }

        mAttemptPossible.setValue(true);
        mRegistrationError.setValue(null);
        mProfileError.setValue(null);
    }

    private void setErrorMessage(int key) {
        mAttemptPossible.setValue(false);
        mRegistrationError.setValue(ResourceLocalizer.getLocalizedString(key));
    }

    private void setProfileErrorMessage(int key) {
        mAttemptPossible.setValue(false);
        mProfileError.setValue(ResourceLocalizer.getLocalizedString(key));
    }

    private boolean usernameMeetsRequirements(String candidate) {
        if(candidate.length() >= mMinUsernameLength) {
            return false;
        }
        return true;
    }


    private boolean passwordMeetsRequirements(String candidate) {
        if(candidate.length() >= mMinPasswordLength) {
            return false;
        }
        return true;
    }

    public boolean isValidEmail(CharSequence candidate) {
        return (!TextUtils.isEmpty(candidate) && Patterns.EMAIL_ADDRESS.matcher(candidate).matches());
    }

    public JSONObject asJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("username", mUsername.getValue());
        result.put("password", mPassword1.getValue());
        result.put("marketingOptIn", mCommunicationOptIn.getValue());
        if(mCommunicationOptIn.getValue()) {
            JSONObject profile = new JSONObject();
            profile.put("firstName", mFirstName.getValue());
            profile.put("lastName", mLastName.getValue());
            profile.put("company", mCompany.getValue());
            profile.put("email", mEmail.getValue());
            result.put("profile", profile);
        }
//            JSONObject meta = new JSONObject();
//            meta.put("type", "user");
//            OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
//            meta.put("created", now.toEpochSecond());
//            meta.put("schema", "0.1");
//            result.put("doc", meta);

        return result;
    }

    public Result<User> registerLocally() {
        String displayName = mFirstName.getValue();
        if(displayName == null) {
            displayName = mUsername.getValue();
        }
        return mLoginRepository.registerUser(mUsername.getValue(), mPassword1.getValue(), mFirstName.getValue());
    }

}
