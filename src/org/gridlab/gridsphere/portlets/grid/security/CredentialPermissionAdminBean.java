/*
 * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlets.grid.security;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.services.grid.security.credential.*;
import org.gridlab.gridsphere.services.grid.security.credential.CredentialPermission;
import org.gridlab.gridsphere.provider.PortletBean;

import java.util.List;

public class CredentialPermissionAdminBean extends PortletBean {

    // Credential permission actions
    public static final String ACTION_CREDENTIAL_PERMISSION_LIST = "doListCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_VIEW = "doViewCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_EDIT = "doEditCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_EDIT_CONFIRM = "doConfirmEditCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_EDIT_CANCEL = "doCancelEditCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_DELETE = "doDeleteCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_DELETE_CONFIRM = "doConfirmDeleteCredentialPermission";
    public static final String ACTION_CREDENTIAL_PERMISSION_DELETE_CANCEL = "doCancelDeleteCredentialPermission";

    // Credential permission pages
    public static final String PAGE_CREDENTIAL_PERMISSION_LIST = "/jsp/security/credentialPermissionList.jsp";
    public static final String PAGE_CREDENTIAL_PERMISSION_VIEW = "/jsp/security/credentialPermissionView.jsp";
    public static final String PAGE_CREDENTIAL_PERMISSION_EDIT = "/jsp/security/credentialPermissionEdit.jsp";
    public static final String PAGE_CREDENTIAL_PERMISSION_DELETE = "/jsp/security/credentialPermissionDelete.jsp";
    public static final String PAGE_CREDENTIAL_PERMISSION_DELETE_CONFIRM = "/jsp/security/credentialPermissionDeleteConfirm.jsp";

    // Credential permission variables
    private List credentialPermissionList = null;
    private CredentialPermission credentialPermission = null;
    private String credentialPermissionID = null;
    private String permittedSubjects = null;
    private String description = null;

    // Credential manager service
    private CredentialManagerService credentialManagerService = null;

    /******************************************
     * Portlet bean methods
     ******************************************/

    public CredentialPermissionAdminBean() {
        initView();
    }

    public CredentialPermissionAdminBean(PortletConfig config, PortletRequest request, PortletResponse response)
            throws PortletException {
        init(config, request, response);
        initServices();
        initView();
    }

    protected void initView() {
        setTitle("Credential Permission Manager");
    }

    protected void initServices()
            throws PortletException {
        this.log.debug("Entering initServices()");
        this.credentialManagerService = (CredentialManagerService)getPortletService(CredentialManagerService.class);
        this.log.debug("Exiting initServices()");
    }

    public void doDefaultViewAction()
            throws PortletException {
        doListCredentialPermission();
    }

    /******************************************
     * Credential permission actions
     ******************************************/

    public void doListCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doListCredentialPermission");
        loadCredentialPermissionList();
        setPage(PAGE_CREDENTIAL_PERMISSION_LIST);
        this.log.debug("Exiting doListCredentialPermission");
    }

    public void doViewCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doViewCredentialPermission");
        loadCredentialPermission();
        setPage(PAGE_CREDENTIAL_PERMISSION_VIEW);
        this.log.debug("Exiting doViewCredentialPermission");
    }

    public void doEditCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doEditCredentialPermission");
        loadCredentialPermission();
        setPage(PAGE_CREDENTIAL_PERMISSION_EDIT);
        this.log.debug("Exiting doEditCredentialPermission");
    }

    public void doConfirmEditCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doConfirmEditCredentialPermission");
        loadCredentialPermission();
        try {
            editCredentialPermission();
            saveCredentialPermission();
            setPage(PAGE_CREDENTIAL_PERMISSION_VIEW);
        } catch (PortletException e) {
            setIsFormInvalid(true);
            setFormInvalidMessage(e.getMessage());
            setPage(PAGE_CREDENTIAL_PERMISSION_EDIT);
        }
        this.log.debug("Exiting doConfirmEditCredentialPermission");
    }

    public void doCancelEditCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doConfirmEditCredentialPermission");
        doListCredentialPermission();
        this.log.debug("Exiting doConfirmEditCredentialPermission");
    }

    public void doDeleteCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doDeleteCredentialPermission");
        loadCredentialPermission();
        setPage(PAGE_CREDENTIAL_PERMISSION_DELETE);
        this.log.debug("Exiting doDeleteCredentialPermission");
    }

    public void doConfirmDeleteCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doConfirmDeleteCredentialPermission");
        loadCredentialPermission();
        deleteCredentialPermission();
        setPage(PAGE_CREDENTIAL_PERMISSION_DELETE_CONFIRM);
        this.log.debug("Exiting doConfirmDeleteCredentialPermission");
    }

    public void doCancelDeleteCredentialPermission()
            throws PortletException {
        this.log.debug("Entering doCancelDeleteCredentialPermission");
        doListCredentialPermission();
        this.log.debug("Exiting doCancelDeleteCredentialPermission");
    }

    /******************************************
     * Credential permission methods
     ******************************************/

    public List getCredentialPermissionList() {
        return this.credentialPermissionList;
    }

    public CredentialPermission getCredentialPermission() {
        return this.credentialPermission;
    }

    public boolean isNewCredentialPermission() {
        return (this.credentialPermission == null);
    }

    public String getCredentialPermissionID() {
        return this.credentialPermissionID;
    }

    public String getPermittedSubjects() {
        return this.permittedSubjects;
    }

    public String getDescription() {
       return this.description;
    }

    public void loadCredentialPermissionList()
            throws PortletException {
        this.credentialPermissionList = this.credentialManagerService.getCredentialPermissions();
    }

    public void loadCredentialPermission()
            throws PortletException {
        // Get id of credential permission
        this.credentialPermissionID = getParameter("credentialPermissionID");
        // If id blank, init credential permission
        if (credentialPermissionID.equals("")) {
            initCredentialPermission();
            return;
        }
        // Get credential permission with id
        this.credentialPermission = this.credentialManagerService.getCredentialPermission(credentialPermissionID);
        // If no credential permission with id, init credential permission
        if (credentialPermission == null) {
            initCredentialPermission();
            return;
        }
        // Otherwise set credential permission properties
        this.permittedSubjects = this.credentialPermission.getPermittedSubjects();
        this.description = this.credentialPermission.getDescription();
    }

    public void initCredentialPermission() {
        this.permittedSubjects = new String();
        this.description = new String();
    }

    public void editCredentialPermission()
            throws PortletException {
        this.permittedSubjects = getParameter("permittedSubjects");
        if (this.permittedSubjects.equals("")) {
            throw new PortletException("Permitted subjects cannot be blank.");
        }
        this.description = getParameter("description");
        this.log.debug("Description = " + this.description);
        if (this.description.equals("")) {
            throw new PortletException("Description cannot be blank.");
        }
    }

    public void saveCredentialPermission()
            throws PortletException {
        this.log.debug("Description = " + this.description);
        this.credentialManagerService.createCredentialPermission(this.permittedSubjects, this.description);
        this.credentialPermissionID = this.permittedSubjects;
    }

    public void deleteCredentialPermission()
            throws PortletException {
        this.credentialManagerService.deleteCredentialPermission(this.permittedSubjects);
    }
}
