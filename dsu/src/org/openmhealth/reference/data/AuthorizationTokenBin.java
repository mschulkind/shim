package org.openmhealth.reference.data;

import org.openmhealth.reference.domain.AuthorizationToken;
import org.openmhealth.reference.exception.OmhException;

/**
 * <p>
 * The collection of authorization tokens.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class AuthorizationTokenBin {
	/**
	 * The name of the DB document/table/whatever that contains the
	 * authorization tokens.
	 */
	public static final String DB_NAME = "authorization_token_bin";
	
	/**
	 * The instance of this AuthorizationTokenBin to use.
	 */
	private static AuthorizationTokenBin instance;
	
	/**
	 * Default constructor.
	 */
	protected AuthorizationTokenBin() {
		instance = this;
	}
	
	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static AuthorizationTokenBin getInstance() {
		return instance;
	}
	
	/**
	 * Stores an existing authorization token.
	 * 
	 * @param token
	 *        The token to be saved.
	 * 
	 * @throws OmhException
	 *         The code is null.
	 */
	public abstract void storeToken(
		final AuthorizationToken token)
		throws OmhException;
	
	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * access token string. It will still be returned even if it has expired.
	 * 
	 * @param accessToken
	 *        The authorization token's access token string.
	 * 
	 * @return The {@link AuthorizationToken} or null if no authorization token
	 *         has the given access token associated with it.
	 * 
	 * @throws OmhException
	 *         Multiple authorization tokens have the same access token string.
	 */
	public abstract AuthorizationToken getTokenFromAccessToken(
		final String accessToken)
		throws OmhException;
	
	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * refresh token string. It will still be returned even if it has expired.
	 * 
	 * @param refreshToken
	 *        The authorization token's refresh token string.
	 * 
	 * @return The {@link AuthorizationToken} or null if no authorization token
	 *         has the given refresh token associated with it.
	 * 
	 * @throws OmhException
	 *         Multiple authorization tokens have the same access token string.
	 */
	public abstract AuthorizationToken getTokenFromRefreshToken(
		final String refreshToken)
		throws OmhException;
}