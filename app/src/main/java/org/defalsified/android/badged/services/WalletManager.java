package org.defalsified.android.badged.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.defalsified.android.badged.utils.PrefsManager;

import java.util.UUID;

/**
 * Temporary wallet manager
 *TO  replaced with the native C implementation
 */

public class WalletManager {
    private static final String PREF_HAS_WALLET = "has_wallet";
    private static final String PREF_WALLET_ADDRESS = "wallet_address";

    private final Context context;
    private final PrefsManager prefsManager;

    public WalletManager(Context context) {
        this.context = context;
        this.prefsManager = new PrefsManager(context);
    }


    public boolean hasWallet() {
        return prefsManager.getBoolean(PREF_HAS_WALLET, false);
    }


    public String getWalletAddress() {
        return prefsManager.getString(PREF_WALLET_ADDRESS, null);
    }

    /**
     * Create a new wallet
     *
     * @param callback Callback for wallet creation result
     */

    public void createWallet(WalletCallback callback) {
        // Simulate wallet creation delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // random wallet address for now

            String uuid = UUID.randomUUID().toString().replace("-", "");
            String walletAddress = "0x" + uuid;

            // Save wallet info
            prefsManager.setBoolean(PREF_HAS_WALLET, true);
            prefsManager.setString(PREF_WALLET_ADDRESS, walletAddress);

            // Call success callback
            if (callback != null) {
                callback.onSuccess(walletAddress);
            }
        }, 1500); // Simulate 1.5 second delay
    }


    public interface WalletCallback {
        void onSuccess(String walletAddress);
        void onError(String errorMessage);
    }
}