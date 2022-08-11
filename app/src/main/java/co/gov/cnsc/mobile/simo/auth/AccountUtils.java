package co.gov.cnsc.mobile.simo.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import co.gov.cnsc.mobile.simo.BuildConfig;
import co.gov.cnsc.mobile.simo.SIMOApplication;
import co.gov.cnsc.mobile.simo.models.SIMO;
import co.gov.cnsc.mobile.simo.models.User;

/**
 * Esta clase comunica la interfaz gráfica con el sistema de almacenamiento de sesiones
 * para ser almacenadas o borradas
 */
public class AccountUtils {

    public static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";
    public static final String PARAM_USER_PASSWORD = "password";

    //public static IServerAuthenticator mServerAuthenticator = new MyServerAuthenticator();

	/*public static Account getAccount(Context context, String accountName) {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
		for (Account account : accounts) {
			if (account.name.equalsIgnoreCase(accountName)) {
				return account;
			}
		}
		return null;
	}*/

    /**
     * Obtiene la cuenta almacenada en el sistema
     *
     * @param activity             activity desde el cuál se está pididendo la sesión
     * @param authenticableAccount interfaz que tiene eventos de exito o error al obtener la sesión almacenada
     */
    public static void getAuthAccount(final Activity activity, final AuthenticableAccount authenticableAccount) {
        AccountManager accountManager = AccountManager.get(activity);
        Account[] accounts = accountManager.getAccountsByType(BuildConfig.APPLICATION_ID);
        if (accounts.length > 0) {
            final Account account = accounts[0];
            String jsonUser = accountManager.getUserData(account, "user");
            Gson gson = new Gson();
            final User.Session session = gson.fromJson(jsonUser, User.Session.class);
            final String password = accountManager.getPassword(account);
            final AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(account, "full_access", null, activity, null, null);
            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String token = null;

                    try {
                        Bundle bundle = accountManagerFuture.getResult();
                        token = bundle.get(AccountManager.KEY_AUTHTOKEN).toString();
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }

                    return token;
                }

                @Override
                protected void onPostExecute(String token) {
                    Log.i(SIMOApplication.TAG, "Access token retrieved:" + token);
                    if (token != null) {
                        authenticableAccount.onResultAccount(account, session, token, password);
                    } else {
                        authenticableAccount.onErrorAccount();
                    }
                }

            };
            task.execute();
        } else {
            authenticableAccount.onErrorAccount();
        }
    }

    /**
     * Guarda la sesión en el dispositivo
     *
     * @param activity Activity desde la cuál se hace el llamado del guardado
     * @param session  EL objeto sesión que se quiere almacenar
     * @param password Contraseña con la que se quiere guardar la sesión
     */
    public static void setAuthAccount(Activity activity, final User.Session session, String password) {
        Account account = new Account(session.getUsername(), BuildConfig.APPLICATION_ID);
        AccountManager accountManager = AccountManager.get(activity);
        Gson gson = new Gson();
        String json = gson.toJson(session);
        accountManager.addAccountExplicitly(account, password, null);
        accountManager.setUserData(account, "user", json);
        accountManager.setAuthToken(account, "full_access", session.getCookie());
        SIMOApplication.Companion.initializeFresco(activity);
    }

	/*public static void updateAuthAccount(Activity activity,User.Session session){
		AccountManager accountManager = AccountManager.get(activity);
		Gson gson = new Gson();
		String json = gson.toJson(session);
		Account[] accounts = accountManager.getAccountsByType(BuildConfig.APPLICATION_ID);
		if(accounts.length>0){
			final Account account = accounts[0];
			accountManager.setUserData(account,"user",json);
		}
	}*/

    /**
     * Borra una sesión guardada del dispositivo
     *
     * @param activity Activity desde la cuál se elimina la sesión
     * @param session  Objeto sessión que se quiere borrar del sistema
     */
    public static void removeAuthAccount(Activity activity, User.Session session) {
        SIMO.CREATOR.getInstance().setSession(null);
        AccountManager accountManager = AccountManager.get(activity);
        String token = session.getCookie();
        if (session != null && token != null) {
            accountManager.invalidateAuthToken(AccountUtils.ACCOUNT_TYPE, token);
        }
        Account[] accounts = accountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        for (Account account : accounts) {
            accountManager.removeAccount(account, null, null);
        }
    }

}
