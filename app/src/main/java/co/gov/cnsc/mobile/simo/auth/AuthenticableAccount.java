package co.gov.cnsc.mobile.simo.auth;

import android.accounts.Account;

import co.gov.cnsc.mobile.simo.models.User;

/**
 * Interfaz que ayuda a determinar si la sesión almacenada se puede obtener de forma exitosa o con errores
 */
public interface AuthenticableAccount {

    /**
     * Caso en el cuál la sesión se guarde correctamente
     */
    void onResultAccount(Account account, User.Session session, String token, String password);

    /**
     * Caso en el cuál haya algún error en el almacenamiento de la sesión
     */
    void onErrorAccount();
}
