/*
 * @author <a href="mailto:russell@aei-potsdam.mpg.de">Michael Paul Russell</a>
 * @version $Id$
 * <p>
 * This class is used for managing Globus credentials on behalf of portlet users.
 */
package org.gridlab.gridsphere.services.grid.security.credential.impl;

import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;

import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerRdbms;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
import org.gridlab.gridsphere.core.persistence.BaseObject;

import org.gridlab.gridsphere.services.grid.security.credential.*;

import org.globus.security.GlobusProxy;
import org.globus.security.GlobusProxyException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public final class GlobusCredentialManager
    implements PortletServiceProvider, CredentialManagerService {

    private static GlobusCredentialManager _instance = null;

    private static PortletLog _log = SportletLog.getInstance(GlobusCredentialManager.class);
    private PersistenceManagerRdbms pm = PersistenceManagerRdbms.getInstance();
    private GlobusCredentialRetrievalClient retrievalClient = null;
    private Map activeCredentialMaps = Collections.synchronizedSortedMap(new TreeMap());
    private String credentialPermissionImpl = GlobusCredentialPermission.class.getName();
    private String credentialMappingImpl = GlobusCredentialMapping.class.getName();
    private String credentialMappingRequestImpl = GlobusCredentialMappingRequest.class.getName();

    /****** CONSTRUCTOR METHODS *******/

    private GlobusCredentialManager() {
    }

    public static GlobusCredentialManager getInstance() {
        if (_instance == null) {
            _instance = new GlobusCredentialManager();
        }
        return _instance;
    }

    /****** PORTLET SERVICE METHODS *******/

    public void init(PortletServiceConfig config) {
        _log.info("Entering init()");
        initServices();
        initCredentialRetrievalClient(config);
        initPortalCredential(config);
        _log.info("Exiting init()");
    }

    private void initServices() {
        // Get instance of service factory
        PortletServiceFactory factory = SportletServiceFactory.getInstance();
        // Instantiate helper services
    }

    private void initCredentialRetrievalClient(PortletServiceConfig config) {
        // Hostname init parameter
        String host = config.getInitParameter("retrievalHost", GlobusCredentialRetrievalClient.DEFAULT_HOST);
        _log.info("Credential retrieval hostname = " + host);
        // Port init parameter
        int port = GlobusCredentialRetrievalClient.DEFAULT_PORT;
        try {
            port = (new Integer(config.getInitParameter("retrievalPort"))).intValue();
        } catch (Exception e) {
            _log.warn("Credential retrieval port not valid. Using default value " + port);
        }
        _log.info("Credential retrieval port = " + port);
        // Credential lifetime init parameter
        long lifetime =  GlobusCredentialRetrievalClient.DEFAULT_LIFETIME;
        try {
            lifetime = (new Long(config.getInitParameter("retrievalLifetime"))).longValue();
        } catch (Exception e) {
          _log.warn("Credential retrieval lifetime not valid. Using default value " + lifetime);
        }
        _log.info("Credential default lifetime = " + lifetime);
        // Save credential retrieval client
        this.retrievalClient = new GlobusCredentialRetrievalClient(host, port, lifetime);
    }

    private void initPortalCredential(PortletServiceConfig config) {
        // x509 user certificate parameter
        String x509UserCertificate = config.getInitParameter("x509UserCertificate", "");
        // x509 user key parameter
        String x509UserKey = config.getInitParameter("x509UserKey", "");
        // x509 certificates path
        String x509CertificatesPath = config.getInitParameter("x509CertificatesPath", "");
        if (!x509UserCertificate.equals("") && !x509UserKey.equals("") && !x509CertificatesPath.equals("")) {
            _log.info("Initializing portal credential with given x509 parameters...");
            _log.info("X509 user certificate = " + x509UserCertificate);
            _log.info("X509 user key = " + x509UserKey);
            _log.info("X509 certificates path = " + x509CertificatesPath);
            this.retrievalClient.setPortalCredential(x509UserCertificate, x509UserKey, x509CertificatesPath);
        }
    }

    public void destroy() {
        destroyCredentials();
    }

    /****** CREDENTIAL PERMISSION METHODS *******/

    public List getCredentialPermissions() {
        try {
            String query = "select cp from "
                         + this.credentialPermissionImpl
                         + " cp";
            _log.debug(query);
            return this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential permissions", e);
            return new Vector();
        }
    }

    public CredentialPermission getCredentialPermission(String pattern) {
        try {
            String query = "select cp from "
                         + this.credentialPermissionImpl
                         + " cp where cp.permittedSubjects=\"" + pattern + "\"";
            _log.debug(query);
            return (CredentialPermission)this.pm.restoreObject(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential permission", e);
            return null;
        }
    }

    public CredentialPermission createCredentialPermission(String pattern) {
        return createCredentialPermission(pattern, null);
    }

    public CredentialPermission createCredentialPermission(String pattern, String description) {
        GlobusCredentialPermission permission = new GlobusCredentialPermission();
        permission.setPermittedSubjects(pattern);
        permission.setDescription(description);
        createCredentialPermission(permission);
        return permission;
    }

    private void createCredentialPermission(CredentialPermission permission) {
        String pattern = permission.getPermittedSubjects();
        // Check that no permission (already) exists with given pattern
        if (existsCredentialPermission(pattern)) {
            _log.warn("Credential permission already exists with subject pattern " + pattern);
        } else {
            _log.debug("Creating credential permission " + pattern);
            try {
                this.pm.create(permission);
            } catch (PersistenceManagerException e) {
                _log.error("Error creating credential permission", e);
            }
        }
    }

    public void deleteCredentialPermission(String pattern) {
        _log.debug("Deleting credential permission " + pattern);
        try {
            CredentialPermission permission = getCredentialPermission(pattern);
            this.pm.delete(permission);
        } catch (PersistenceManagerException e) {
            _log.error("Error deleting credential permission", e);
        }
    }

    public boolean existsCredentialPermission(String pattern) {
        _log.debug("Testing if permission " + pattern + " exists");
        String value = null;
        try {
            String query = "select cp.permittedSubjects from "
                         + this.credentialPermissionImpl
                         + " cp where cp.permittedSubjects=\"" + pattern + "\"";
            _log.debug(query);
            value = (String)this.pm.restoreObject(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error checking if credential permission exists", e);
            return false;
        }
        return (value != null);
    }

    public List getPermittedCredentialSubjects() {
        List permittedSubjects = null;
        try {
            String query = "select cp.permittedSubjects from "
                         + this.credentialPermissionImpl
                         + " cp";
            _log.debug(query);
            permittedSubjects = this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving permitted subjects!", e);
            permittedSubjects = new Vector();
        }
        return permittedSubjects;
    }

    public boolean isCredentialPermitted(String subject) {
        _log.info("Testing if [" + subject + "] is permitted");
        boolean answer = false;
        Iterator permissions = getCredentialPermissions().iterator();
        while (permissions.hasNext()) {
            CredentialPermission permission = (CredentialPermission)permissions.next();
            if (permission.isCredentialPermitted(subject)) {
                answer = true;
                break;
            }
        }
        return answer;
    }

    /****** CREDENTIAL MAPPING REQUEST METHODS *******/

    public List getCredentialMappingRequests() {
        return selectCredentialMappingRequests("");
    }

    public List getCredentialMappingRequests(User user) {
        String criteria = " user=\"" + user.getID() + "\"";
        return selectCredentialMappingRequests(criteria);
    }

    private List selectCredentialMappingRequests(String criteria) {
        try {
            String query = "select cmr from "
                         + this.credentialMappingRequestImpl
                         + " cmr where "
                         + criteria;
            _log.debug(query);
            return this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential mapping requests", e);
            return new Vector();
        }
    }

    public CredentialMappingRequest getCredentialMappingRequest(String id) {
        String criteria = "cmr.ObjectID=\"" + id +"\"";
        return selectCredentialMappingRequest(criteria);
    }

    private GlobusCredentialMappingRequest selectCredentialMappingRequest(String criteria) {
        try {
            String query = "select cp from "
                         + this.credentialMappingRequestImpl
                         + " cmr where "
                         + criteria;
            _log.debug(query);
            return (GlobusCredentialMappingRequest)this.pm.restoreObject(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential mapping request ", e);
            return null;
        }
    }

    public CredentialMappingRequest createCredentialMappingRequest() {
        return new GlobusCredentialMappingRequest();
    }

    public CredentialMappingRequest createCredentialMappingRequest(CredentialMapping mapping) {
        GlobusCredentialMappingRequest mappingRequest = new GlobusCredentialMappingRequest();
        mappingRequest.setUser(mapping.getUser());
        mappingRequest.setSubject(mapping.getSubject());
        mappingRequest.setLabel(mapping.getLabel());
        mappingRequest.setTag(mapping.getTag());
        mappingRequest.setCredentialMappingAction(CredentialMappingAction.EDIT);
        return mappingRequest;
    }

    public void submitCredentialMappingRequest(CredentialMappingRequest mappingRequest) {
        try {
            this.pm.create(mappingRequest);
        } catch (PersistenceManagerException e) {
            _log.error("Error creating credential mapping request ", e);
        }
    }

    public CredentialMapping approveCredentialMappingRequest(CredentialMappingRequest mappingRequest) {
        CredentialMappingAction mappingAction = mappingRequest.getCredentialMappingAction();
        GlobusCredentialMapping credentialMapping = null;
       if (mappingAction.equals(CredentialMappingAction.ADD)) {
            // Instantiate credential mapping
            credentialMapping = new GlobusCredentialMapping();
            credentialMapping.setUser(mappingRequest.getUser());
            credentialMapping.setSubject(mappingRequest.getSubject().trim());
            credentialMapping.setLabel(mappingRequest.getLabel().trim());
            credentialMapping.setTag(mappingRequest.getTag().trim());
            // Create credential mapping
            createGlobusCredentialMapping(credentialMapping);
        } else if (mappingAction.equals(CredentialMappingAction.EDIT)) {
            // Retrieve associated credential mapping
            credentialMapping = getGlobusCredentialMapping(mappingRequest.getID());
            credentialMapping.setSubject(mappingRequest.getSubject().trim());
            credentialMapping.setLabel(mappingRequest.getLabel().trim());
            credentialMapping.setTag(mappingRequest.getTag().trim());
            // Update credential mapping
            updateCredentialMapping(credentialMapping);
        } else if (mappingAction.equals(CredentialMappingAction.REMOVE)) {
            // Retrieve associated credential mapping
            credentialMapping = getGlobusCredentialMapping(mappingRequest.getID());
            // Delete credential mapping
            deleteCredentialMappingRequest(mappingRequest);
        }
        // Delete mapping request
        deleteCredentialMappingRequest(mappingRequest);
        // Return the original mapping
        return credentialMapping;
    }

    public void denyCredentialMappingRequest(CredentialMappingRequest mappingRequest) {
        // Delete mapping request
        deleteCredentialMappingRequest(mappingRequest);
    }

    private void deleteCredentialMappingRequest(CredentialMappingRequest mappingRequest) {
        try {
            this.pm.delete(mappingRequest);
        } catch (PersistenceManagerException e) {
            _log.error("Error creating credential mapping request ", e);
        }
    }

    /****** CREDENTIAL MAPPING METHODS *******/

    public List getCredentialMappings() {
        try {
            String query = "select cm from "
                         + this.credentialMappingImpl
                         + " cm";
            _log.debug(query);
            return this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential mappings ", e);
            return new Vector();
        }
    }

    public CredentialMapping getCredentialMapping(String subject) {
        return getGlobusCredentialMapping(subject);
    }

    private GlobusCredentialMapping getGlobusCredentialMapping(String subject) {
        String dn = GlobusCredential.retrieveDN(subject);
        try {
            String query = "select cm from "
                         + this.credentialMappingImpl
                         + " cm where cm.subject=\"" + dn + "\"";
            _log.debug(query);
            return (GlobusCredentialMapping)this.pm.restoreObject(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential mapping " + e);
            return null;
        }
    }

    public CredentialMapping createCredentialMapping(String subject, User user)
            throws CredentialNotPermittedException {
        return createCredentialMapping(subject.trim(), user, null);
    }

    public CredentialMapping createCredentialMapping(String subject, User user, String tag)
            throws CredentialNotPermittedException {
        // Instantiate mapping and set properties
        GlobusCredentialMapping mapping = new GlobusCredentialMapping();
        mapping.setSubject(subject.trim());
        mapping.setUser(user);
        mapping.setTag(tag.trim());
        // Create record in database
        createCredentialMapping(mapping);
        return mapping;
    }


    private void createCredentialMapping(CredentialMapping mapping)
            throws CredentialNotPermittedException {
        String subject = mapping.getSubject();
        // Check that mapping is of right type
        if (!(mapping instanceof GlobusCredentialMapping))  {
            throw new CredentialNotPermittedException("Mapping is not a globus credential mapping.");
        }
        // Check that no mapping already exists for given subject
        if (existsCredentialMapping(subject)) {
            throw new CredentialNotPermittedException("Mapping already exists for given subject");
        // Check that the subject is permitted for use by this manager
        } else if (!isCredentialPermitted(subject)) {
            throw new CredentialNotPermittedException("Credential subject not permitted");
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Creating mapping " + mapping);
        }
        // Save a record of object to database
        if (_log.isDebugEnabled()) {
            _log.debug("Create mapping " + mapping);
        }
        try {
            this.pm.create(mapping);
        } catch (PersistenceManagerException e) {
            _log.error("Error creating credential mapping " + e);
        }
    }

    private void createGlobusCredentialMapping(GlobusCredentialMapping mapping) {
        if (_log.isDebugEnabled()) {
            _log.debug("Create mapping " + mapping);
        }
        try {
            this.pm.create(mapping);
        } catch (PersistenceManagerException e) {
            _log.error("Error creating credential mapping " + e);
        }
    }

    private void updateCredentialMapping(CredentialMapping mapping) {
        if (_log.isDebugEnabled()) {
            _log.debug("Updating mapping " + mapping);
        }
        try {
            this.pm.update(mapping);
        } catch (PersistenceManagerException e) {
            _log.error("Error updating credential mapping " + e);
        }
    }

    public void deleteCredentialMapping(String subject) {
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping != null) {
            deleteCredentialMapping(mapping);
        }
    }

    private void deleteCredentialMapping(CredentialMapping mapping) {
        try {
            this.pm.delete(mapping);
        } catch (PersistenceManagerException e) {
            _log.error("Error deleting credential mapping ", e);
        }
    }

    public List getCredentialMappings(User user) {
        try {
            String query = "select cm from "
                         + this.credentialMappingImpl
                         + " cm where cm.user=" + user.getID();
            _log.debug(query);
            return this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential mappings for user", e);
            return new Vector();
        }
    }

    public void deleteCredentialMappings(User user) {
        try {
            String query = "delete cm from "
                         + this.credentialMappingImpl
                         + " cm where cm.user=" + user.getID();
            _log.debug(query);
            this.pm.deleteList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error removing credential maps for user", e);
        }
    }

    public boolean hasCredentialMappings(User user) {
        return (getCredentialMappings(user).size() > 0);
    }

    public boolean existsCredentialMapping(String subject) {
        _log.debug("Testing if mapping for " + subject + " exists");
        return (getCredentialMapping(subject) != null);
    }

    public User getCredentialUser(String subject) {
        User user = null;
        CredentialMapping mapping = getCredentialMapping(subject);
        if (mapping != null) {
            user = mapping.getUser();
        }
        return user;
    }

    public List getCredentialSubjects(User user) {
        List subjects = null;
        String query = "select cm.subject from "
                     + this.credentialMappingImpl
                     + " cm where cm.user=" + user.getID();
        _log.debug(query);
        try {
            subjects = this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential subjects for user", e);
            subjects = new Vector();
        }
        return subjects;
    }

    public List getCredentialTags(User user) {
        List tags = new Vector();
        String query = "select cm.tag from "
                     + this.credentialMappingImpl
                     + " cm where cm.user=" + user.getID();
        _log.debug(query);
        try {
            tags = this.pm.restoreList(query);
        } catch (PersistenceManagerException e) {
            _log.error("Error retrieving credential tags for user", e);
            tags = new Vector();
        }
        return tags;
    }

    public String getCredentialTag(String subject)
            throws CredentialMappingNotFoundException {
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            return null;
        }
        return mapping.getTag();
    }

    public void setCredentialTag(String subject, String tag)
            throws CredentialMappingNotFoundException {
        // Retrieve credential mapping
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            throw new CredentialMappingNotFoundException("No credential mapping for " + subject);

        }
        // Set mapping tag
        mapping.setTag(tag);
        // Perform update
        updateCredentialMapping(mapping);
    }

    public String getCredentialLabel(String subject)
            throws CredentialMappingNotFoundException {
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            return null;
        }
        return mapping.getLabel();
    }

    public void setCredentialLabel(String subject, String tag)
            throws CredentialMappingNotFoundException {
        // Retrieve credential mapping
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            throw new CredentialMappingNotFoundException("No credential mapping for " + subject);
        }
        // Set mapping label
        mapping.setLabel(tag);
        // Perform update
        updateCredentialMapping(mapping);
    }

    public List getCredentialHosts(String subject)
            throws CredentialMappingNotFoundException {
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            return new Vector();
        }
        return mapping.getHosts();
    }

    public void addCredentialHost(String subject, String host)
            throws CredentialMappingNotFoundException {
        // Retrieve associated mapping
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            throw new CredentialMappingNotFoundException("No credential mapping exists for " + subject);
        }
        // Add host to mapping
        mapping.addHost(host);
        // Then perform update
        updateCredentialMapping(mapping);
    }

    public void addCredentialHosts(String subject, List hosts)
            throws CredentialMappingNotFoundException {
        // Retrieve associated mapping
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            throw new CredentialMappingNotFoundException("No credential mapping exists for " + subject);
        }
        // Add hosts to mapping
        mapping.addHosts(hosts);
        // Then perform update
        updateCredentialMapping(mapping);
    }

    public void removeCredentialHost(String subject, String host)
            throws CredentialMappingNotFoundException {
        // Retrieve associated mapping
        GlobusCredentialMapping mapping = getGlobusCredentialMapping(subject);
        if (mapping == null) {
            throw new CredentialMappingNotFoundException("No credential mapping exists for " + subject);
        }
        // Remove host from mapping
        mapping.removeHost(host);
        // Then perform update
        updateCredentialMapping(mapping);
    }

    public List getCredentialSubjectsForHost(String host) {
        List subjects = null;
        Iterator maps = getCredentialMappings().iterator();
        while (maps.hasNext()) {
            CredentialMapping mapping = (CredentialMapping)maps.next();
            if (mapping.hasHost(host)) {
                subjects.add(mapping.getSubject());
            }
        }
        return subjects;
    }

    public List getCredentialSubjectsForHost(User user, String host) {
        List subjects = new Vector();
        Iterator maps = getCredentialMappings(user).iterator();
        while (maps.hasNext()) {
            CredentialMapping mapping = (CredentialMapping)maps.next();
            if (mapping.hasHost(host)) {
                subjects.add(mapping.getSubject());
            }
        }
        return subjects;
    }

    /****** CREDENTIAL RETRIEVAL METHODS *******/

    public String getCredentialRetrievalProtocol() {
        return this.retrievalClient.getProtocol();
    }

    public String getCredentialRetrievalHost() {
        return this.retrievalClient.getHost();
    }

    public int getCredentialRetrievalPort() {
        return this.retrievalClient.getPort();
    }

    public long getCredentialRetrievalLifetime() {
        return this.retrievalClient.getCredentialLifetime();
    }

    public List retrieveCredentials(User user, String password)
        throws CredentialRetrievalException {
        _log.debug("Entering retrieveCredentials");
        List credentials = new Vector();
        StringBuffer msgs = null;
        _log.debug("Getting credential mappings");
        // Iterate through the credential mappings associated with user
        Iterator iterator = getCredentialMappings(user).iterator();
        _log.debug("Iterating through credential mappings");
        while (iterator.hasNext()) {
            // For each mapping, check that a retrieval tag exists
            CredentialMapping mapping = (CredentialMapping)iterator.next();
            _log.debug("Checking if mapping [" + mapping.getSubject() + "] has a tag");
            Credential credential = null;
            String tag = mapping.getTag();
            // If no retrieval tag, try next credential
            if (tag == null) {
                continue;
            }
            // Get subject for this credential
            String subject = mapping.getSubject();
            try {
                _log.debug("Retrieving credential with [" + tag + "]");
                // Retrieve credential based on credential tag, subject, and given password
                credential = this.retrievalClient.retrieveCredential(tag, password, subject);
                // Store the retrieved credential
                storeCredential(credential);
                // Add credential to returned list
                credentials.add(credential);
            } catch (CredentialRetrievalException e) {
                // Record each error message we come across
                if (msgs == null) {
                    msgs = new StringBuffer();
                }
                String msg = e.getMessage();
                msgs.append(msg);
                msgs.append("\n");
                _log.debug(msg);
            } catch (CredentialNotPermittedException e) {
                // Record each error message we come across
                if (msgs == null) {
                    msgs = new StringBuffer();
                }
                String msg = e.getMessage();
                msgs.append(msg);
                msgs.append("\n");
                _log.debug(msg);
            }
        }
        // Throw exception if no credentials were
        // successfully retrieved and stored
        if (credentials.size() == 0) {
            // Provide the error messages from above
            throw new CredentialRetrievalException(msgs.toString());
        }
        _log.debug("Exiting retrieveCredentials");
        return credentials;
   }

   public List refreshCredentials(User user)
        throws CredentialRetrievalException {
        throw new CredentialRetrievalException("Method not yet implemented!");
   }

    /****** CREDENTIAL STORAGE METHODS *******/

    public void storeCredential(Credential credential)
        throws CredentialNotPermittedException {
        if (! (credential instanceof GlobusCredential) ) {
            throw new CredentialNotPermittedException("Wrong type of credential provided!");
        }
        storeCredential((GlobusCredential)credential);
    }

    private void storeCredential(GlobusCredential credential)
        throws CredentialNotPermittedException {
        String subject = credential.getSubject();
        // Check if mapping exists
        User user = getCredentialUser(subject);
        if (user == null) {
            throw new CredentialNotPermittedException("Credential mapping not found for " + subject);
        }
        // Get user's credential collection
        Map userCredentials = getActiveCredentialMap(user);
        // Add this credential to that collection
        userCredentials.put(subject, credential);
    }

    public void storeCredentials(List credentials)
        throws CredentialNotPermittedException {
        // Store each credential in list
        Iterator iterator = credentials.iterator();
        while (iterator.hasNext()) {
            Credential credential = (Credential)iterator.next();
            storeCredential(credential);
        }
    }

    public void destroyCredential(String subject) {
        // Get user mapped to subject
        User user = getCredentialUser(subject);
        // If user mapping exists
        if (user != null) {
            // Get user's credential collection
            Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
            // If user collection exists
            if (userCredentials != null) {
                // Remove the credential from the collection
                Credential credential = (Credential)userCredentials.remove(subject);
                // Destroy credential if not null
                if (credential != null) {
                    credential.destroy();
                }
            }
        }
    }

    private void destroyCredentials() {
        synchronized (this.activeCredentialMaps) {
            // Iterate through each user collection
            Iterator users = this.activeCredentialMaps.keySet().iterator();
            while (users.hasNext()) {
                User user = (User)users.next();
                // Get user's credential collection
                Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
                // Just being safe...
                if (userCredentials != null) {
                    synchronized (userCredentials) {
                        // Iterate through each credential and destroy it
                        Iterator iterator = userCredentials.values().iterator();
                        while (iterator.hasNext()) {
                            Credential credential = (Credential)iterator.next();
                            credential.destroy();
                        }
                    }
                }
            }
            // Now clear everything
            this.activeCredentialMaps.clear();
        }
    }

    public void destroyCredentials(User user) {
        // Remove mapping associated with user
        Map userCredentials = (Map)this.activeCredentialMaps.remove(user.getID());
        // If not null, then destroy each credential in the mapping
        if (userCredentials != null) {
            synchronized (userCredentials) {
                // Destroy each credential in collection
                Iterator iterator = userCredentials.values().iterator();
                while (iterator.hasNext()) {
                    Credential credential = (Credential)iterator.next();
                    credential.destroy();
                }
            }
        }
    }

    /****** CREDENTIAL USEAGE METHODS *******/

    public List getActiveCredentials() {
        List activeCredentials = new Vector();
        // Iterate through user credential collections
        Iterator userCredentialMaps = this.activeCredentialMaps.values().iterator();
        synchronized (this.activeCredentialMaps) {
            while (userCredentialMaps.hasNext()) {
                // Get next user's credential collection
                Map userCredentialMap = (Map)userCredentialMaps.next();
                // Insert credentials from map into list
                insertActiveCredentialsIntoList(userCredentialMap, activeCredentials);
            }
        }
        return activeCredentials;
    }

    public List getActiveCredentials(User user) {
        List activeCredentials = new Vector();
        // Get user's credential collection
        Map userCredentialMap = (Map)this.activeCredentialMaps.get(user.getID());
        // If not null...
        if (userCredentialMap != null) {
            // Add active credentials to the list
            insertActiveCredentialsIntoList(userCredentialMap, activeCredentials);
        }
        return activeCredentials;
    }

    public List getActiveCredentialsForHost(User user, String host) {
        List activeCredentials = new Vector();
        // Get subjects mapped to user and host
        List userSubjects = getCredentialSubjectsForHost(user, host);
        // Get user's credential collection
        Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
        if (userCredentials != null) {
            synchronized (userCredentials) {
                // Iterate through user's mapped subjects
                Iterator iterator = userSubjects.iterator();
                while (iterator.hasNext()) {
                    String subject = (String)iterator.next();
                    // Get credential with that subject
                    Credential credential = (Credential)userCredentials.get(subject);
                    // Just being safe...
                    if (credential == null) {
                        _log.debug("Credential not active " + subject);
                        continue;
                    }
                    // If expired, add to list of expired credentials
                    if (credential.isExpired()) {
                        _log.debug("Credential has expired " + credential.toString());
                        userCredentials.remove(subject);
                    // Otherwise, add to list of active credentials
                    } else {
                        activeCredentials.add(credential);
                    }
                }
            }
        }
        return activeCredentials;
    }

    private void insertActiveCredentialsIntoList(Map credentialMap, List credentialList) {
        synchronized (credentialMap) {
            // Iterate through the subjects in user's credential collection
            Iterator iterator = credentialMap.keySet().iterator();
            while (iterator.hasNext()) {
                String subject = (String)iterator.next();
                // Get the credential associated with subject
                Credential credential = (Credential)credentialMap.get(subject);
                // Just being safe...
                if (credential == null) {
                    _log.debug("Credential not active " + subject);
                    continue;
                }
                // If expired then remove credential from collection
                if (credential.isExpired()) {
                    _log.debug("Credential has expired " + credential.toString());
                    credentialMap.remove(subject);
                // Otherwise, add to list of active credentials
                } else {
                    credentialList.add(credential);
                }
            }
        }
    }

    public Credential getActiveCredential(String subject) {
        Credential credential = null;
        // Get user mapping for subject
        User user = getCredentialUser(subject);
        // If mapping exists
        if (user != null) {
            // Get user's credential collection
            Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
            // If collection exists
            if (userCredentials != null) {
                // Get credential from collection
                credential = (Credential)userCredentials.get(subject);
                // Just being safe...
                if (credential != null) {
                   // Check if it is expired
                   if (credential.isExpired()) {
                       // If so remove it and return null
                       _log.debug("Credential has expired " + credential.toString());
                        userCredentials.remove(credential.getSubject());
                       return null;
                   }
                }
            }
        }
        return credential;
    }

    public boolean isActiveCredential(String subject) {
        boolean answer = false;
        // Get user mapping for subject
        User user = getCredentialUser(subject);
        // If mapppng doesn't exist, return false
        if (user == null) {
            // Get user's credential collection
            Map userCredentials = (Map)this.activeCredentialMaps.get(user);
            // If empty, return false
            if (userCredentials != null) {
                // Otherwise, check if credential in collection
                answer = userCredentials.containsKey(subject);
            }
        }
        return answer;
    }

    public boolean hasActiveCredentials(User user) {
        Map userCredentials = getActiveCredentialMap(user);
        return (userCredentials.size() > 0);
    }

    public List getActiveCredentialSubjects() {
        List subjectList = new Vector();
        synchronized (this.activeCredentialMaps) {
            Iterator allCredentials = this.activeCredentialMaps.values().iterator();
            while (allCredentials.hasNext()) {
                Map userCredentials = (Map)allCredentials.next();
                synchronized (userCredentials) {
                    Iterator iterator = userCredentials.keySet().iterator();
                    while (iterator.hasNext()) {
                        String subject = (String)iterator.next();
                        subjectList.add(subject);
                    }
                }
            }
        }
        return subjectList;
    }

    public List getActiveCredentialSubjects(User user) {
        List subjectList = new Vector();
        // Get user's credential collection
        Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
        if (userCredentials != null) {
            synchronized (userCredentials) {
                Iterator iterator = userCredentials.keySet().iterator();
                while (iterator.hasNext()) {
                    String subject = (String)iterator.next();
                    subjectList.add(subject);
                }
            }
        }
        return subjectList;
    }

    public List getActiveCredentialMappings() {
        List mapppingList = new Vector();
        // Get user active credential maps
        Iterator activeCredentialMaps = this.activeCredentialMaps.keySet().iterator();
        // For each user, add their active credential mappings to the list
        while (activeCredentialMaps.hasNext()) {
            Map activeCredentialMap = (Map)activeCredentialMaps.next();
            if (activeCredentialMap != null) {
                synchronized (activeCredentialMap) {
                    Iterator iterator = activeCredentialMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        String subject = (String)iterator.next();
                        CredentialMapping mapping = getCredentialMapping(subject);
                        mapppingList.add(mapping);
                    }
                }
            }
        }
        return mapppingList;
    }

    public List getActiveCredentialMappings(User user) {
        List mapppingList = new Vector();
        // Get user's credential collection
        Map activeCredentialMap = (Map)this.activeCredentialMaps.get(user.getID());
        if (activeCredentialMap != null) {
            synchronized (activeCredentialMap) {
                Iterator iterator = activeCredentialMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String subject = (String)iterator.next();
                    CredentialMapping mapping = getCredentialMapping(subject);
                    mapppingList.add(mapping);
                }
            }
        }
        return mapppingList;
    }

    private Map getActiveCredentialMap(User user) {
        // Get user's credentials
        Map userCredentials = (Map)this.activeCredentialMaps.get(user.getID());
        // If mapping is empty, create new mapping
        if (userCredentials == null) {
            userCredentials = Collections.synchronizedSortedMap(new TreeMap());
            activeCredentialMaps.put(user.getID(), userCredentials);
        }
        return userCredentials;
    }
}
