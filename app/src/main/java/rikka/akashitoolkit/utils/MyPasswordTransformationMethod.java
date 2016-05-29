package rikka.akashitoolkit.utils;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * Created by Rikka on 2016/5/28.
 */
public class MyPasswordTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source; // Store char sequence
        }

        public char charAt(int index) {
            return '■';
        }

        public int length() {
            return mSource.length();
        }

        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }
    }

    private static MyPasswordTransformationMethod sInstance;

    public static MyPasswordTransformationMethod getInstance() {
        if (sInstance != null)
            return sInstance;

        sInstance = new MyPasswordTransformationMethod();
        return sInstance;
    }
}
