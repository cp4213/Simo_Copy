package co.gov.cnsc.mobile.simo.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Clase que prepara el sistema de almacenamiento de cuentas para ser usado
 */
public class AuthenticatorService extends Service {

    /**
     * Objeto autenticador del sistema que guarda, obtiene y elimina cuentas
     */
    private static AccountAuthenticator sAccountAuthenticator;

    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = null;
        if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            binder = getAuthenticator().getIBinder();
        }
        return binder;
    }

    private AccountAuthenticator getAuthenticator() {
        if (null == AuthenticatorService.sAccountAuthenticator) {
            AuthenticatorService.sAccountAuthenticator = new AccountAuthenticator(this);
        }
        return AuthenticatorService.sAccountAuthenticator;
    }

}
