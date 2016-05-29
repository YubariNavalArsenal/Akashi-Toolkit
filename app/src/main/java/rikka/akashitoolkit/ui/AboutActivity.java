package rikka.akashitoolkit.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import rikka.akashitoolkit.BuildConfig;
import rikka.akashitoolkit.R;
import rikka.akashitoolkit.support.Settings;
import rikka.akashitoolkit.support.StaticData;
import rikka.akashitoolkit.utils.ClipBoardUtils;
import rikka.akashitoolkit.utils.DonateUtils;
import rikka.akashitoolkit.utils.UpdateCheck;
import rikka.akashitoolkit.utils.Utils;
import rikka.materialpreference.PreferenceFragment;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            AboutFragment fragment = new AboutFragment();

            getFragmentManager().beginTransaction().replace(R.id.fragment,
                    fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onStop() {
            UpdateCheck.instance().recycle();
            super.onStop();
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            setPreferencesFromResource(R.xml.about, null);

            try {
                PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);

                findPreference("version").setSummary(String.format(getActivity().getString(R.string.version_format), info.versionName, info.versionCode));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            findPreference("qq_group").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ClipBoardUtils.putTextIntoClipboard(getActivity(), "437033068");
                    Toast.makeText(getActivity(), String.format(getString(R.string.copied_to_clipboard), "437033068"), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            findPreference("feedback").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SendReportActivity.sendEmail(getActivity(),
                            "Akashi Toolkit 反馈",
                            String.format("系统版本: %s (API %d)\n应用版本: %d\n是否为 Google Play 版: %s",
                                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                                    StaticData.instance(getActivity()).versionCode,
                                    BuildConfig.isGooglePlay ? "是" : "否"));
                    return false;
                }
            });

            findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DonateUtils.startActivity(getActivity());
                    return false;
                }
            });

            if (BuildConfig.isGooglePlay
                    && !Utils.isPackageInstalled(getActivity(), DonateUtils.PACKAGENAME_ALIPAY)) {
                ((PreferenceScreen) findPreference("screen")).removePreference(findPreference("donate"));
            }

            findPreference("version").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().getWindow().getDecorView().removeCallbacks(clearClickCount);
                    getActivity().getWindow().getDecorView().postDelayed(clearClickCount, 3000);

                    click ++;

                    if (click == 10) {
                        Settings.instance(getActivity()).putBoolean(Settings.DEVELOPER, true);
                        Toast.makeText(getActivity(), "You are now developer.", Toast.LENGTH_SHORT).show();
                        getActivity().recreate();
                    }

                    return false;
                }
            });

            if (!Settings.instance(getActivity()).getBoolean(Settings.DEVELOPER, false)) {
                ((PreferenceScreen) findPreference("screen")).removePreference(findPreference("developer"));
            } else {
                findPreference("developer").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), PushSendActivity.class));

                        return false;
                    }
                });
            }

        }

        private int click = 0;
        private Runnable clearClickCount = new Runnable() {
            @Override
            public void run() {
                click = 0;
            }
        };
    }
}
