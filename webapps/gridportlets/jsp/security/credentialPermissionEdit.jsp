<%@ page import="org.gridlab.gridsphere.services.grid.security.credential.CredentialPermission,
                 org.gridlab.gridsphere.portlets.grid.security.CredentialPermissionAdminBean,
                 java.util.List" %>
<%@ taglib uri="/portletWidgets" prefix="gs" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>
<portletAPI:init/>
<jsp:useBean id="credentialPermissionAdminBean"
             class="org.gridlab.gridsphere.portlets.grid.security.CredentialPermissionAdminBean"
             scope="request"/>
<form name="CredentialPermissionPortlet" method="POST"
      action="<%=credentialPermissionAdminBean.getPortletActionURI(CredentialPermissionAdminBean.ACTION_CREDENTIAL_PERMISSION_EDIT)%>">
  <input type="hidden" name="credentialPermissionID" value="<%=credentialPermissionAdminBean.getCredentialPermissionID()%>"/>
  <script type="text/javascript">
    function CredentialPermissionPortlet_editConfirmCredentialPermission_onClick() {
      document.CredentialPermissionPortlet.action="<%=credentialPermissionAdminBean.getPortletActionURI(CredentialPermissionAdminBean.ACTION_CREDENTIAL_PERMISSION_EDIT_CONFIRM)%>";
      document.CredentialPermissionPortlet.submit();
    }

    function CredentialPermissionPortlet_editCancelCredentialPermission_onClick() {
      document.CredentialPermissionPortlet.action="<%=credentialPermissionAdminBean.getPortletActionURI(CredentialPermissionAdminBean.ACTION_CREDENTIAL_PERMISSION_EDIT_CANCEL)%>";
      document.CredentialPermissionPortlet.submit();
    }
  </script>
<table class="portlet-pane" cellspacing="1">
  <tr>
    <td>
      <table class="portlet-frame" cellspacing="1" width="100%">
        <tr>
          <td class="portlet-frame-title">
              New Credential Permission
          </td>
        </tr>
        <tr>
          <td class="portlet-frame-actions">
            <input type="button"
                   name="<%=CredentialPermissionAdminBean.ACTION_CREDENTIAL_PERMISSION_EDIT_CONFIRM%>"
                   value="Save Permission"
                   onClick="javascript:CredentialPermissionPortlet_editConfirmCredentialPermission_onClick()"/>
            &nbsp;&nbsp;<input type="button"
                   name="<%=CredentialPermissionAdminBean.ACTION_CREDENTIAL_PERMISSION_EDIT_CANCEL%>"
                   value="Cancel Edit"
                   onClick="javascript:CredentialPermissionPortlet_editCancelCredentialPermission_onClick()"/>
          </td>
        </tr>
        <tr>
          <td class="portlet-frame-message-alert">
            <%=credentialPermissionAdminBean.getFormInvalidMessage()%>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table class="portlet-frame" cellspacing="1" width="100%">
       <tr>
         <td class="portlet-frame-label" width="200">
           Permitted Subjects
         </td>
         <td class="portlet-frame-input" width="250">
             <input type="text"
                    name="permittedSubjects"
                    value="<%=credentialPermissionAdminBean.getPermittedSubjects()%>"/>
         </td>
       </tr>
       <tr>
         <td class="portlet-frame-label">
           Description
         </td>
         <td class="portlet-frame-input">
             <input type="text"
                    name="description"
                    value="<%=credentialPermissionAdminBean.getDescription()%>"/>
         </td>
       </tr>
      </table>
    </td>
  </tr>
</table>
</form>
