/*
 * Created by IntelliJ IDEA.
 * User: russell
 * Date: Jan 8, 2003
 * Time: 11:51:26 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.gridlab.gridsphere.services.security.password.impl;

import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.services.security.password.*;
import org.gridlab.gridsphere.services.user.UserManagerService;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerRdbms;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;

import java.util.List;
import java.util.Date;
import java.util.Vector;

public class DbmsPasswordManagerService
    implements PortletServiceProvider, PasswordManagerService {

    private static PortletLog _log = SportletLog.getInstance(DbmsPasswordManagerService.class);
    private PersistenceManagerRdbms pm = PersistenceManagerRdbms.getInstance();
    private String passwordImpl = DbmsPassword.class.getName();
    private long defaultPasswordLifetime = -1;

    /****** PORTLET SERVICE METHODS *******/

    public void init(PortletServiceConfig config) {
        _log.info("Entering init()");
        initServices();
        _log.info("Exiting init()");
    }

    private void initServices() {
        // Get instance of service factory
        PortletServiceFactory factory = SportletServiceFactory.getInstance();
        // Instantiate helper services
    }

    public void destroy() {
    }

    /****** PASSWORD SERVICE METHODS *******/

    public List getPasswords() {
        List passwords = null;
        String query = "select pw from "
                     + this.passwordImpl
                     + " pw";
        try {
            passwords = this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to retrieve passwords", e);
            passwords = new Vector();
        }
        return passwords;
    }

    public Password getPassword(User user) {
        return getDbmsPassword(user);
    }

    /**
     * This method returns the <code>DbmsPassword</code> associated with
     * a user and is called internally by other methods in this class.
     */
    private DbmsPassword getDbmsPassword(User user) {
        DbmsPassword password = null;
        String query = "select pw from "
                     + this.passwordImpl
                     + " pw where pw.user=" + user.getID();
        try {
            password = (DbmsPassword)this.pm.restoreObject(query);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to retrieve password for user", e);
        }
        return password;
    }

    public PasswordBean editPassword(User user) {
        PasswordBean editor = null;
        Password password = getDbmsPassword(user);
        if (password == null) {
            long lifetime = getDefaultPasswordLifetime();
            editor = new PasswordBean(user);
            editor.setLifetime(lifetime);
        } else {
            editor = new PasswordBean(password);
        }
        return editor;
    }

    public void savePassword(PasswordBean editor)
            throws PasswordInvalidException {
        savePassword(editor, true);
    }

    public void savePassword(PasswordBean editor, boolean validatePassword)
            throws PasswordInvalidException {
        // Get password attributes
        User user = editor.getUser();
        String hint = editor.getHint();
        String value = editor.getValue();
        Date dateExpires = editor.getDateExpires();
        // Create or update password
        Date now = new Date();
        // Check if user has password already
        DbmsPassword password = getDbmsPassword(user);
        if (password == null) {
            // Validate the value if requested
            if (validatePassword) {
                validatePassword(value);
            }
            password = new DbmsPassword();
            password.setUser(user);
            password.setValue(value);
            password.setDateExpires(dateExpires);
            password.setDateCreated(now);
            password.setDateLastModified(now);
            try {
                this.pm.create(password);
            } catch (PersistenceManagerException e) {
                _log.error("Unable to create password", e);
            }
        } else {
            // Validate the value if requested
            if (validatePassword) {
                validatePassword(password, value);
            }
            password.setValue(value);
            password.setDateExpires(dateExpires);
            password.setDateLastModified(now);
            try {
                this.pm.update(password);
            } catch (PersistenceManagerException e) {
                _log.error("Unable to update password", e);
            }
        }
    }

    public void savePassword(User user, String value)
            throws PasswordInvalidException {
        savePassword(user, value, true);
    }

    public void savePassword(User user, String value, boolean validatePassword)
            throws PasswordInvalidException {
        // Get password editor
        PasswordBean editor = editPassword(user);
        // Save new value
        editor.setValue(value);
        // Reset date expires
        editor.resetDateExpires();
        // Save password changes
        savePassword(editor, validatePassword);
    }

    public void deletePassword(User user) {
        Password password = getPassword(user);
        if (password != null) {
            try {
                this.pm.delete(password);
            } catch (PersistenceManagerException e) {
                _log.error("Unable to delete password", e);
            }
        }
    }

    public boolean hasPassword(User user) {
        Password password = getPassword(user);
        return (password != null);
    }

    public void validatePassword(User user, String newValue)
            throws PasswordInvalidException {
        DbmsPassword password = getDbmsPassword(user);
        if (password == null) {
            validatePassword(newValue);
        } else {
            validatePassword(password, newValue);
        }
    }

    private void validatePassword(DbmsPassword password, String newValue)
            throws PasswordInvalidException {
        String oldValue = password.getValue();
        if (oldValue.equals(newValue)) {
            String msg = "New password must be different from the old value.";
            throw new PasswordInvalidException(msg);
        }
        validatePassword(newValue);
    }

    public void validatePassword(String password)
          throws PasswordInvalidException {
        if (password == null) {
            String msg = "Password is not set.";
            throw new PasswordInvalidException(msg);
        }
        password = password.trim();
        if (password.length() == 0) {
            String msg = "Password is blank.";
            throw new PasswordInvalidException(msg);
        }
        if (password.length() < 5) {
            String msg = "Password must be longer than 5 characters.";
            throw new PasswordInvalidException(msg);
        }
    }

    public boolean isPasswordCorrect(User user, String value) {
        DbmsPassword password = getDbmsPassword(user);
        if (password == null) {
            _log.debug("No password found for user");
            return false;
        }
        _log.debug("Stored value is " + password.getValue());
        _log.debug("Provided value is " + value);
        return password.equals(value);
    }

    public long getDefaultPasswordLifetime() {
        return this.defaultPasswordLifetime;
    }

    /***
    public Password createPassword(User user, String value, boolean validatePassword)
            throws PasswordInvalidException {
        // First validate the value if requested
        if (validatePassword) {
            validatePassword(value);
        }
        // Then check if user already has password
        DbmsPassword password = getDbmsPassword(user);
        if (password != null) {
            String msg = "User already has password.";
            throw new PasswordInvalidException(msg);
        }
        password = new DbmsPassword();
        password.setUser(user);
        password.setValue(value);
        try {
            this.pm.create(password);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to create password", e);
        }
        return password;
    }

    public Password createPassword(User user, String value)
            throws PasswordInvalidException {
        return createPassword(user, value, true);
    }

    public void resetPassword(User user, String value)
            throws PasswordInvalidException {
        // First validate the new value
        validatePassword(value);
        // Next delete the old password
        deletePassword(user);
        // Then create a new password
        DbmsPassword password = new DbmsPassword();
        password.setUser(user);
        password.setValue(value);
        try {
            this.pm.create(password);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to create password", e);
        }
    }

    public void updatePassword(User user, String value)
            throws PasswordInvalidException, PasswordNotFoundException {
        // First check if user already has password
        DbmsPassword password = getDbmsPassword(user);
        if (password == null) {
            String msg = "A password must first be created for this user.";
            throw new PasswordNotFoundException(msg);
        }
        String oldValue = password.getValue();
        // Then validate the new value
        validatePassword(oldValue, value);
        // Then update the password
        password.setValue(value);
        try {
            this.pm.update(password);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to update password", e);
        }
    }

     public Date getDatePasswordExpires(User user)
             throws PasswordNotFoundException {
         DbmsPassword password = getDbmsPassword(user);
         if (password == null) {
             String msg = "No password found for user " + user.getUserID();
             _log.error(msg);
             throw new PasswordNotFoundException(msg);
         }
         return password.getDateExpires();
     }

     public void setDatePasswordExpires(User user, Date date)
             throws PasswordNotFoundException {
         DbmsPassword password = getDbmsPassword(user);
         if (password == null) {
             String msg = "No password found for user " + user.getUserID();
             _log.error(msg);
             throw new PasswordNotFoundException(msg);
         }
         password.setDateExpires(date);
     }

     public String getPasswordHint(User user)
             throws PasswordNotFoundException {
         DbmsPassword password = getDbmsPassword(user);
         if (password == null) {
             String msg = "No password found for user " + user.getUserID();
             _log.error(msg);
             throw new PasswordNotFoundException(msg);
         }
         return password.getHint();
     }

     public void setPasswordHint(User user, String hint)
             throws PasswordNotFoundException {
         DbmsPassword password = getDbmsPassword(user);
         if (password == null) {
             String msg = "No password found for user " + user.getUserID();
             _log.error(msg);
             throw new PasswordNotFoundException(msg);
         }
         password.setHint(hint);
     }
    ***/
}
