package org.microg.gms.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import org.microg.gms.common.HttpFormClient;
import org.microg.tools.ui.AbstractSettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.microg.gms.common.HttpFormClient.RequestContent;

public class MasterTokenRetriever extends AbstractSettingsFragment {

        private static final String TAG = "GmsMasterTokenRetriever";
        private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

        @RequestContent("Token")
        public String token;

        @HttpFormClient.RequestContentDynamic
        public Map<Object, Object> dynamicFields;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
                // No preferences to add
        }

        @RequiresApi(api = 21)
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                // Create a LinearLayout to hold the components
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));

                authenticateUser(layout);

                return layout;
        }

        @RequiresApi(api = 21)
        private void authenticateUser(LinearLayout layout) {
                KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager.isKeyguardSecure()) {
                        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Authentication required", "Please enter your PIN to access the master token.");
                        startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
                } else {
                        Toast.makeText(getContext(), "No security setup on device", Toast.LENGTH_SHORT).show();
                        displayTokens(layout);
                }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
                        if (resultCode == FragmentActivity.RESULT_OK) {
                                // Authentication successful, show the tokens
                                LinearLayout layout = (LinearLayout) getView();
                                if (layout != null) {
                                        displayTokens(layout);
                                }
                        } else {
                                // Authentication failed
                                Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                }
        }

        private void displayTokens(LinearLayout layout) {
                AccountManager accountManager = AccountManager.get(getContext());
                Account[] accounts = accountManager.getAccounts();
                List<String> tokenList = new ArrayList<>();

                for (Account account : accounts) {
                        String accountName = account.name;
                        String masterToken = accountManager.getPassword(account); // Assuming password is used as token here for demonstration
                        tokenList.add(accountName + ": " + masterToken);

                        // Create CardView for each token
                        CardView cardView = new CardView(getActivity());
                        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardLayoutParams.setMargins(16, 16, 16, 16);
                        cardView.setLayoutParams(cardLayoutParams);
                        cardView.setRadius(8);
                        // cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                        cardView.setContentPadding(16, 16, 16, 16);
                        cardView.setMaxCardElevation(8);
                        cardView.setCardElevation(8);

                        // Create a TextView for the account name and master token
                        TextView textView = new TextView(getActivity());
                        textView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        textView.setText(accountName);
                        textView.setTextIsSelectable(false);
                        textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        textView.setText(masterToken);
                                        textView.setTextIsSelectable(true);
                                }
                        });

                        // textView.setTextSize(16);

                        // Add the TextView to the CardView
                        cardView.addView(textView);

                        // Add the CardView to the layout
                        layout.addView(cardView);
                }

                if (tokenList.isEmpty()) {
                        Toast.makeText(getContext(), "No accounts found", Toast.LENGTH_SHORT).show();
                }
        }
}
