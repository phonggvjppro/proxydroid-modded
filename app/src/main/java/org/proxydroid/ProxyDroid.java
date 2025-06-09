/* proxydroid - Global / Individual Proxy App for Android
 * Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */

package org.proxydroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.analytics.FirebaseAnalytics;

import org.proxydroid.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProxyDroid extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = "ProxyDroid";
    private static final int MSG_UPDATE_FINISHED = 0;
    private static final int MSG_NO_ROOT = 1;

    final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_FINISHED:
                    Toast.makeText(requireActivity(), getString(R.string.update_finished), Toast.LENGTH_LONG)
                            .show();
                    break;
                case MSG_NO_ROOT:
                    showAToast(getString(R.string.require_root_alert));
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private AlertDialog pd = null;
    private String profile;
    private final Profile mProfile = new Profile();
    private CheckBoxPreference isGlobalProxy;
    private CheckBoxPreference isAuthCheck;
    private CheckBoxPreference isNTLMCheck;
    private CheckBoxPreference isPACCheck;
    private ListPreference profileList;
    private EditTextPreference hostText;
    private EditTextPreference portText;
    private EditTextPreference userText;
    private EditTextPreference passwordText;
    private EditTextPreference domainText;
    private EditTextPreference certificateText;
    private ListPreference proxyTypeList;
    private Preference isRunningCheck;
    private CheckBoxPreference isBypassAppsCheck;
    private Preference proxyedApps;
    private Preference bypassAddrs;

    private void showAbout() {

        WebView web = new WebView(requireActivity());
        web.loadUrl("file:///android_asset/pages/about.html");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });

        String versionName = "";
        try {
            versionName = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException ex) {
            versionName = "";
        }

        new AlertDialog.Builder(requireActivity()).setTitle(
                String.format(getString(R.string.about_title), versionName))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.ok_iknow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setView(web)
                .create()
                .show();
    }

    private void CopyAssets() {
        AssetManager assetManager = requireActivity().getAssets();
        String[] files = null;
        String abi = null;
        abi = Build.SUPPORTED_ABIS[0];
        try {
            if (abi.matches("armeabi-v7a|arm64-v8a"))
                files = assetManager.list("armeabi-v7a");
            else
                files = assetManager.list("x86");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        if (files != null) {
            for (String file : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    if (abi.matches("armeabi-v7a|arm64-v8a"))
                        in = assetManager.open("armeabi-v7a/" + file);
                    else
                        in = assetManager.open("x86/" + file);
                    out = new FileOutputStream(requireActivity().getFilesDir().getAbsolutePath() + "/" + file);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void loadProfileList() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        String[] profileEntries = settings.getString("profileEntries", "").split("\\|");
        String[] profileValues = settings.getString("profileValues", "").split("\\|");

        profileList.setEntries(profileEntries);
        profileList.setEntryValues(profileValues);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "home_screen");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ProxyDroid");
        ((ProxyDroidApplication)requireActivity().getApplication())
                .firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        hostText = findPreference("host");
        portText = findPreference("port");
        userText = findPreference("user");
        passwordText = findPreference("password");
        domainText = findPreference("domain");
        certificateText = findPreference("certificate");
        bypassAddrs = findPreference("bypassAddrs");
        proxyTypeList = findPreference("proxyType");
        proxyedApps = findPreference("proxyedApps");
        profileList = findPreference("profile");

        isRunningCheck = findPreference("isRunning");
        isGlobalProxy = findPreference("isGlobalProxy");
        isAuthCheck = findPreference("isAuth");
        isNTLMCheck = findPreference("isNTLM");
        isPACCheck = findPreference("isPAC");
        isBypassAppsCheck = findPreference("isBypassApps");

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        Log.d(TAG, settings.getString("ssid", ""));
        String profileValuesString = settings.getString("profileValues", "");

        if ("".equals(profileValuesString)) {
            Editor ed = settings.edit();
            profile = "1";
            ed.putString("profileValues", "1|0");
            ed.putString("profileEntries",
                    getString(R.string.profile_default) + "|" + getString(R.string.profile_new));
            ed.putString("profile", "1");
            ed.commit();

            profileList.setDefaultValue("1");
        }

        loadProfileList();

        new Thread() {
            @Override
            public void run() {

                try {
                    // Try not to block activity
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                    // Nothing
                }

                if (!Utils.isRoot()) {
                    handler.sendEmptyMessage(MSG_NO_ROOT);
                }

                String versionName;
                try {
                    versionName = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                    versionName = "NONE";
                }

                if (!settings.getBoolean(versionName, false)) {

                    String version;
                    try {
                        version = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;
                    } catch (NameNotFoundException e) {
                        version = "NONE";
                    }

                    reset();

                    Editor edit = settings.edit();
                    edit.putBoolean(version, true);
                    edit.commit();

                    handler.sendEmptyMessage(MSG_UPDATE_FINISHED);
                }
            }
        }.start();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case Menu.FIRST + 1:
                        new Thread() {
                            @Override
                            public void run() {
                                reset();
                            }
                        }.start();
                        return true;
                    case Menu.FIRST + 2:
                        AlertDialog ad = new AlertDialog.Builder(requireActivity()).setTitle(R.string.profile_del)
                                .setMessage(R.string.profile_del_confirm)
                                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked OK so do some stuff */
                                        delProfile(profile);
                                    }
                                })
                                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* User clicked Cancel so do some stuff */
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        ad.show();

                        return true;
                    case Menu.FIRST + 3:
                        showAbout();
                        return true;
                    case Menu.FIRST + 4:
                        rename();
                        return true;
                }

                return false;
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                menu.add(Menu.NONE, Menu.FIRST + 1, 4, getString(R.string.recovery))
                        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.add(Menu.NONE, Menu.FIRST + 2, 2, getString(R.string.profile_del))
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                menu.add(Menu.NONE, Menu.FIRST + 3, 5, getString(R.string.about))
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                menu.add(Menu.NONE, Menu.FIRST + 4, 1, getString(R.string.change_name))
                        .setIcon(android.R.drawable.ic_menu_edit)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }

        });
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.proxydroid_preference, rootKey);
    }

    /**
     * Called when the activity is closed.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private boolean serviceStop() {

        if (!Utils.isWorking()) return false;

        try {
            requireActivity().stopService(new Intent(requireActivity(), ProxyDroidService.class));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Called when connect button is clicked.
     */
    private boolean serviceStart() {

        if (Utils.isWorking()) return false;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        mProfile.getProfile(settings);

        try {

            Intent it = new Intent(requireActivity(), ProxyDroidService.class);
            Bundle bundle = new Bundle();
            bundle.putString("host", mProfile.getHost());
            bundle.putString("user", mProfile.getUser());
            bundle.putString("bypassAddrs", mProfile.getBypassAddrs());
            bundle.putString("password", mProfile.getPassword());
            bundle.putString("domain", mProfile.getDomain());
            bundle.putString("certificate", mProfile.getCertificate());

            bundle.putString("proxyType", mProfile.getProxyType());
            bundle.putBoolean("isGlobalProxy", mProfile.isGlobalProxy());
            bundle.putBoolean("isBypassApps", mProfile.isBypassApps());
            bundle.putBoolean("isAuth", mProfile.isAuth());
            bundle.putBoolean("isNTLM", mProfile.isNTLM());
            bundle.putBoolean("isPAC", mProfile.isPAC());

            bundle.putInt("port", mProfile.getPort());
            it.putExtras(bundle);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(it);
            } else {
                requireActivity().startService(it);
            }
        } catch (Exception ex) {
            // Nothing
            return false;
        }

        return true;
    }

    private void onProfileChange(String oldProfile) {

        Profile.ProfileUtils.switchProfile(oldProfile,
                PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("profile", "1"),
                requireActivity());
        mProfile.getProfile(PreferenceManager.getDefaultSharedPreferences(requireActivity()));

        hostText.setText(mProfile.getHost());
        userText.setText(mProfile.getUser());
        passwordText.setText(mProfile.getPassword());
        domainText.setText(mProfile.getDomain());
        certificateText.setText(mProfile.getCertificate());
        proxyTypeList.setValue(mProfile.getProxyType());

        isAuthCheck.setChecked(mProfile.isAuth());
        isNTLMCheck.setChecked(mProfile.isNTLM());
        isGlobalProxy.setChecked(mProfile.isGlobalProxy());
        isBypassAppsCheck.setChecked(mProfile.isBypassApps());
        isPACCheck.setChecked(mProfile.isPAC());

        portText.setText(Integer.toString(mProfile.getPort()));

        Log.d(TAG, mProfile.toString());
    }

    private void showAToast(String msg) {
        if (!requireActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage(msg)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.ok_iknow), (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void disableAll() {
        hostText.setEnabled(false);
        portText.setEnabled(false);
        userText.setEnabled(false);
        passwordText.setEnabled(false);
        domainText.setEnabled(false);
        certificateText.setEnabled(false);
        proxyTypeList.setEnabled(false);
        proxyedApps.setEnabled(false);
        profileList.setEnabled(false);
        bypassAddrs.setEnabled(false);

        isAuthCheck.setEnabled(false);
        isNTLMCheck.setEnabled(false);
        isGlobalProxy.setEnabled(false);
        isPACCheck.setEnabled(false);
        isBypassAppsCheck.setEnabled(false);
    }

    private void enableAll() {
        hostText.setEnabled(true);

        if (!isPACCheck.isChecked()) {
            portText.setEnabled(true);
            proxyTypeList.setEnabled(true);
        }

        bypassAddrs.setEnabled(true);

        if (isAuthCheck.isChecked()) {
            userText.setEnabled(true);
            passwordText.setEnabled(true);
            isNTLMCheck.setEnabled(true);
            if (isNTLMCheck.isChecked()) domainText.setEnabled(true);
        }
        if ("https".equals(proxyTypeList.getValue())) {
            certificateText.setEnabled(true);
        }
        if (!isGlobalProxy.isChecked()) {
            proxyedApps.setEnabled(true);
            isBypassAppsCheck.setEnabled(true);
        }

        profileList.setEnabled(true);
        isGlobalProxy.setEnabled(true);
        isAuthCheck.setEnabled(true);
        isPACCheck.setEnabled(true);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey() != null && preference.getKey().equals("bypassAddrs")) {
            Intent intent = new Intent(requireActivity(), BypassListActivity.class);
            startActivity(intent);
        } else if (preference.getKey() != null && preference.getKey().equals("proxyedApps")) {
            Intent intent = new Intent(requireActivity(), AppManager.class);
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preference);
    }



    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        if (settings.getBoolean("isAutoSetProxy", false)) {
            proxyedApps.setEnabled(false);
            isBypassAppsCheck.setEnabled(false);
        } else {
            proxyedApps.setEnabled(true);
            isBypassAppsCheck.setEnabled(true);
        }

        if (settings.getBoolean("isPAC", false)) {
            portText.setEnabled(false);
            proxyTypeList.setEnabled(false);
            hostText.setTitle(R.string.host_pac);
            hostText.setSummary(R.string.host_pac_summary);
        }

        if (!settings.getBoolean("isAuth", false)) {
            userText.setEnabled(false);
            passwordText.setEnabled(false);
            isNTLMCheck.setEnabled(false);
        }

        if (!settings.getBoolean("isAuth", false) || !settings.getBoolean("isNTLM", false)) {
            domainText.setEnabled(false);
        }

        if (!"https".equals(settings.getString("proxyType", ""))) {
            certificateText.setEnabled(false);
        }

        Editor edit = settings.edit();

        if (Utils.isWorking()) {
            if (settings.getBoolean("isConnecting", false)) isRunningCheck.setEnabled(false);
            edit.putBoolean("isRunning", true);
        } else {
            if (settings.getBoolean("isRunning", false)) {
                new Thread() {
                    @Override
                    public void run() {
                        reset();
                    }
                }.start();
            }
            edit.putBoolean("isRunning", false);
        }

        edit.commit();

        if (settings.getBoolean("isRunning", false)) {
            ((SwitchPreference) isRunningCheck).setChecked(true);
            disableAll();
        } else {
            ((SwitchPreference) isRunningCheck).setChecked(false);
            enableAll();
        }

        // Setup the initial values
        profile = settings.getString("profile", "1");
        profileList.setValue(profile);

        profileList.setSummary(Profile.ProfileUtils.getProfileName(profile, requireActivity()));

        if (!"".equals(settings.getString("user", ""))) {
            userText.setSummary(settings.getString("user", getString(R.string.user_summary)));
        }
        if (!"".equals(settings.getString("certificate", ""))) {
            certificateText.setSummary(settings.getString("certificate", getString(R.string.certificate_summary)));
        }
        if (!"".equals(settings.getString("bypassAddrs", ""))) {
            bypassAddrs.setSummary(
                    settings.getString("bypassAddrs", getString(R.string.set_bypass_summary))
                            .replace("|", ", "));
        } else {
            bypassAddrs.setSummary(R.string.set_bypass_summary);
        }
        if (!"-1".equals(settings.getString("port", "-1")) && !"".equals(settings.getString("port", "-1"))) {
            portText.setSummary(settings.getString("port", getString(R.string.port_summary)));
        }
        if (!"".equals(settings.getString("host", ""))) {
            hostText.setSummary(settings.getString("host", getString(
                    settings.getBoolean("isPAC", false) ? R.string.host_pac_summary
                            : R.string.host_summary)));
        }
        if (!"".equals(settings.getString("password", ""))) passwordText.setSummary("*********");
        if (!"".equals(settings.getString("proxyType", ""))) {
            proxyTypeList.setSummary(settings.getString("proxyType", "").toUpperCase());
        }
        if (!"".equals(settings.getString("domain", ""))) {
            domainText.setSummary(settings.getString("domain", ""));
        }

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences settings, String key) {
        // Let's do something a preference value changes
        if(key == null) return;

        if(!key.equals("profile") && key.startsWith("profile")) {
            loadProfileList();
            profileList.setSummary(Profile.ProfileUtils.getProfileName(profile, requireActivity()));
            return;
        }

        if (key.equals("profile")) {
            String profileString = settings.getString("profile", "");
            if (profileString.equals("0")) {
                Profile.ProfileUtils.addProfile(requireActivity());

                loadProfileList();
            } else {
                String oldProfile = profile;
                profile = profileString;
                profileList.setValue(profile);
                onProfileChange(oldProfile);
                profileList.setSummary(Profile.ProfileUtils.getProfileName(profileString, requireActivity()));
            }
        }
        isBypassAppsCheck.setChecked(settings.getBoolean("isBypassApps", false));
        if (key.equals("isConnecting")) {
            if (settings.getBoolean("isConnecting", false)) {
                Log.d(TAG, "Connecting start");
                isRunningCheck.setEnabled(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage(R.string.connecting)
                        .setCancelable(true)
                        .setTitle("");
                pd = builder.create();
                pd.show();
            } else {
                Log.d(TAG, "Connecting finish");
                if (pd != null) {
                    pd.dismiss();
                    pd = null;
                }
                isRunningCheck.setEnabled(true);
            }
        }

        if (key.equals("isPAC")) {
            if (settings.getBoolean("isPAC", false)) {
                portText.setEnabled(false);
                proxyTypeList.setEnabled(false);
                hostText.setTitle(R.string.host_pac);
            } else {
                portText.setEnabled(true);
                proxyTypeList.setEnabled(true);
                hostText.setTitle(R.string.host);
            }
            if (settings.getString("host", "").equals("")) {
                hostText.setSummary(settings.getBoolean("isPAC", false) ? R.string.host_pac_summary
                        : R.string.host_summary);
            } else {
                hostText.setSummary(settings.getString("host", ""));
            }
            hostText.setText(settings.getString("host", ""));
            isPACCheck.setChecked(settings.getBoolean("isPAC", false));
        }

        if (key.equals("isAuth")) {
            if (!settings.getBoolean("isAuth", false)) {

                userText.setEnabled(false);
                passwordText.setEnabled(false);
                isNTLMCheck.setEnabled(false);
                domainText.setEnabled(false);
            } else {
                userText.setEnabled(true);
                passwordText.setEnabled(true);
                isNTLMCheck.setEnabled(true);
                domainText.setEnabled(isNTLMCheck.isChecked());
            }
            isAuthCheck.setChecked(settings.getBoolean("isAuth", false));
        }

        if (key.equals("isNTLM")) {
            if (!settings.getBoolean("isAuth", false) || !settings.getBoolean("isNTLM", false)) {
                domainText.setEnabled(false);
            } else {
                domainText.setEnabled(true);
            }
            isNTLMCheck.setChecked(settings.getBoolean("isNTLM", false));
        }

        if (key.equals("proxyType")) {
            certificateText.setEnabled("https".equals(settings.getString("proxyType", "")));
        }

        if (key.equals("isGlobalProxy")) {
            if (settings.getBoolean("isGlobalProxy", false)) {
                proxyedApps.setEnabled(false);
                isBypassAppsCheck.setEnabled(false);
            } else {
                proxyedApps.setEnabled(true);
                isBypassAppsCheck.setEnabled(true);
            }
            isGlobalProxy.setChecked(settings.getBoolean("isGlobalProxy", false));
        }

        if (key.equals("isRunning")) {
            if (settings.getBoolean("isRunning", false)) {
                disableAll();
                ((SwitchPreference) isRunningCheck).setChecked(true);
                if (!Utils.isConnecting()) serviceStart();
            } else {
                enableAll();
                ((SwitchPreference) isRunningCheck).setChecked(false);
                if (!Utils.isConnecting()) serviceStop();
            }
        }

        if (key.equals("user")) {
            if (settings.getString("user", "").equals("")) {
                userText.setSummary(getString(R.string.user_summary));
            } else {
                userText.setSummary(settings.getString("user", ""));
            }
            userText.setText(settings.getString("user", ""));
        } else if (key.equals("domain")) {
            if (settings.getString("domain", "").equals("")) {
                domainText.setSummary(getString(R.string.domain_summary));
            } else {
                domainText.setSummary(settings.getString("domain", ""));
            }
            domainText.setText(settings.getString("domain", ""));

        } else if (key.equals("proxyType")) {
            if (settings.getString("proxyType", "").equals("")) {
                proxyTypeList.setSummary(getString(R.string.proxy_type_summary));
                certificateText.setSummary(getString(R.string.certificate_summary));
            } else {
                proxyTypeList.setSummary(settings.getString("proxyType", "").toUpperCase());
                certificateText.setSummary(settings.getString("certificate", ""));
            }
            proxyTypeList.setValue(settings.getString("proxyType", ""));
        } else if (key.equals("bypassAddrs")) {
            if (settings.getString("bypassAddrs", "").equals("")) {
                bypassAddrs.setSummary(getString(R.string.set_bypass_summary));
            } else {
                bypassAddrs.setSummary(settings.getString("bypassAddrs", "").replace("|", ", "));
            }
        } else if (key.equals("port")) {
            if (settings.getString("port", "-1").equals("-1") || settings.getString("port", "-1")
                    .equals("")) {
                portText.setSummary(getString(R.string.port_summary));
            } else {
                portText.setSummary(settings.getString("port", ""));
            }
            portText.setText(settings.getString("port", "3123"));
        } else if (key.equals("host")) {
            if (settings.getString("host", "").equals("")) {
                hostText.setSummary(settings.getBoolean("isPAC", false) ? R.string.host_pac_summary
                        : R.string.host_summary);
            } else {
                hostText.setSummary(settings.getString("host", ""));
            }
            hostText.setText(settings.getString("host", ""));
        } else if (key.equals("password")) {
            if (!settings.getString("password", "").equals("")) {
                passwordText.setSummary("*********");
            } else {
                passwordText.setSummary(getString(R.string.password_summary));
            }
            passwordText.setText(settings.getString("password", ""));
        }
    }

    private void rename() {
        LayoutInflater factory = LayoutInflater.from(requireActivity());
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        final EditText profileName = textEntryView.findViewById(R.id.text_edit);
        profileName.setText(Profile.ProfileUtils.getProfileName(profile, requireActivity()));

        AlertDialog ad = new AlertDialog.Builder(requireActivity()).setTitle(R.string.change_name)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, (dialog, whichButton) -> {
                    String name = profileName.getText().toString();
                    Profile.ProfileUtils.renameProfile(profile, name, requireActivity());
                    loadProfileList();
                })
                .setNegativeButton(R.string.alert_dialog_cancel, (dialog, whichButton) -> {})
                .create();
        ad.show();
    }
    private void delProfile(String profile) {
        Profile.ProfileUtils.delProfile(profile, requireActivity());
    }

    private void reset() {
        try {
            requireActivity().stopService(new Intent(requireActivity(), ProxyDroidService.class));
        } catch (Exception e) {
            // Nothing
        }

        CopyAssets();

        String filePath = requireActivity().getFilesDir().getAbsolutePath();

        Utils.runRootCommand(Utils.getIptables()
                + " -t nat -F OUTPUT\n"
                + requireActivity().getFilesDir().getAbsolutePath()
                + "/proxy.sh stop\n"
                + "kill -9 `cat " + filePath + "cntlm.pid`\n");

        Utils.runRootCommand(
                "chmod 700 " + filePath + "/redsocks2\n"
                + "chmod 700 " + filePath + "/proxy.sh\n"
                + "chmod 700 " + filePath + "/cntlm\n");
    }
}
