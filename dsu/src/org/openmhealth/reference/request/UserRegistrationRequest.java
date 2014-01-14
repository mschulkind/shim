package org.openmhealth.reference.request;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openmhealth.reference.data.UserBin;
import org.openmhealth.reference.domain.User;
import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.reference.servlet.Version1;

/**
 * <p>
 * Creates a new registration for a new user.
 * </p>
 *
 * @author John Jenkins
 */
public class UserRegistrationRequest extends Request<Object> {
	/**
	 * The path to this API after the mandatory path and the version, e.g.
	 * /omh/v1.
	 */
	public static final String PATH = "/users/registration";
	
	/**
	 * The logger for this request.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(UserRegistrationRequest.class.toString());
	
	/**
	 * The algorithm to use to create the random registration ID.
	 */
	private static final String DIGEST_ALGORITHM = "SHA-512";

	/**
	 * The new user.
	 */
	private final User user;
	
	/**
	 * Creates a registration request.
	 * 
	 * @param username
	 *        The new user's user-name.
	 * 
	 * @param password
	 *        The new user's plain-text password.
	 * 
	 * @param email
	 *        The new user's email address.
	 * 
	 * @param rootUrl
	 *        The root URL for our domain, e.g. http://localhost:8080/omh.
	 * 
	 * @throws OmhException
	 *         A parameter was invalid.
	 */
	public UserRegistrationRequest(
		final String username,
		final String password,
		final String email,
		final String rootUrl)
		throws OmhException {
		
		if(rootUrl == null) {
			throw new OmhException("The root URL is null.");
		}
		
		// Create the new user from the parameters and a random registration
		// ID.
		this.user =
			new User(
				username,
				User.hashPassword(password),
				email,
                null,
                null,
				null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openmhealth.reference.request.Request#service()
	 */
	@Override
	public void service() throws OmhException {
		// First, short-circuit if this request has already been serviced.
		if(isServiced()) {
			return;
		}
		else {
			setServiced();
		}
		
		// Create the registration entry in the database.
		UserBin.getInstance().createUser(user);
	}
}