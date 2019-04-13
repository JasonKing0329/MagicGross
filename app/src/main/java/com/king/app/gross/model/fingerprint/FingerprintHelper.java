package com.king.app.gross.model.fingerprint;

import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.king.app.gross.model.fingerprint.samsung.SamsungFingerPrint;

public class FingerprintHelper {

    public static boolean isDeviceSupport(Context context) {

        // common
        FingerprintManagerCompat compat = FingerprintManagerCompat.from(context);
        if (compat.isHardwareDetected()) {
            return true;
        }
        else {
            // samsung SDK
            SamsungFingerPrint fingerPrint = new SamsungFingerPrint(context);
            return fingerPrint.isSupported();
        }
    }
}
