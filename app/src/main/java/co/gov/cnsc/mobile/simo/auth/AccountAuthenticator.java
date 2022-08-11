package co.gov.cnsc.mobile.simo.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import co.gov.cnsc.mobile.simo.activities.LoginActivity;

/**
 * Esta clase hace uso de una clase nativa para el almacenamiento de sesiones
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    //Contexto desde el que se llama al administrador de cuentas
    private final Context mContext;

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;

    }

    /**
     * Almacena una cuenta o sesión al sistema
     *
     * @param response         respuesta del almacenamiento
     * @param accountType      tipo de cuenta que se asocia
     * @param authTokenType    token o cookie a almacenar
     * @param requiredFeatures requisitos adicionales de almacenamiento
     * @param options          opciones adicionales de almacenamiento
     * @return
     */
    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse response,
            String accountType,
            String authTokenType,
            String[] requiredFeatures,
            Bundle options) {
        Bundle reply = new Bundle();

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountUtils.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountUtils.ARG_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AccountUtils.ARG_IS_ADDING_NEW_ACCOUNT, true);

        // return our AccountAuthenticatorActivity
        reply.putParcelable(AccountManager.KEY_INTENT, intent);

        return reply;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse arg0,
                                     Account arg1, Bundle arg2) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse arg0, String arg1) {
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options) {
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        // Lets give another try to authenticate the user
        if (null != authToken) {
            if (authToken.isEmpty()) {
                final String password = am.getPassword(account);
                if (password != null) {
                    //authToken = AccountUtils.mServerAuthenticator.signIn(account.name, password);
                }
            }
        }

        // If we get an authToken - we return it
        if (null != authToken) {
            if (!authToken.isEmpty()) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountUtils.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(AccountUtils.ARG_AUTH_TOKEN_TYPE, authTokenType);

        // This is for the case multiple accounts are stored on the device
        // and the AccountPicker dialog chooses an account without auth token.
        // We can pass out the account name chosen to the user of write it
        // again in the Login activity intent returned.
        if (null != account) {
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String arg0) {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse arg0, Account arg1,
                              String[] arg2) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse arg0,
                                    Account arg1, String arg2, Bundle arg3) {
        return null;
    }

}
