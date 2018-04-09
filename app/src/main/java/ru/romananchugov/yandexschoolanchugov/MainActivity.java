package ru.romananchugov.yandexschoolanchugov;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String FRAGMENT_TAG = "gallery";

    public static final String CLIENT_ID = "959666c7ee9942f6b9ffec283205e35c";

    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id="+CLIENT_ID;

    public static final String USERNAME = "ymra.username";
    public static final String TOKEN = "ymra.token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: onCreate()");
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getData() != null) {
            onLogin();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString(TOKEN, null);
        if (token == null) {
            startLogin();
            return;
        }

        if (savedInstanceState == null) {
            startFragment();
        }
    }

    private void startFragment() {
        Log.i(TAG, "startFragment: startFragment()");
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GalleryListFragment(), FRAGMENT_TAG)
                .commit();
    }

    public void startLogin(){
        Log.i(TAG, "startLogin: startLogin()");

        new AuthDialogFragment().show(getSupportFragmentManager(), "auth");
    }

    public void onLogin(){
        Log.i(TAG, "onLogin: onLogin()");

        Uri data = getIntent().getData();
        setIntent(null);

        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());

        if (matcher.find()) {
            final String token = matcher.group(1);
            if (!TextUtils.isEmpty(token)) {
                Log.d(TAG, "onLogin: token: " + token);
                saveToken(token);
            } else {
                Log.w(TAG, "onRegistrationSuccess: empty token");
            }
        } else {
            Log.w(TAG, "onRegistrationSuccess: token not found in return url");
        }
    }

    private void saveToken(String token) {
        Log.i(TAG, "saveToken: saveToken()");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "");
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public static class AuthDialogFragment extends DialogFragment {

        public AuthDialogFragment () {
            super();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.example_auth_title)
                    .setMessage(R.string.example_auth_message)
                    .setPositiveButton(R.string.example_auth_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL)));
                        }
                    })
                    .setNegativeButton(R.string.example_auth_negative_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    })
                    .create();
        }
    }
}
