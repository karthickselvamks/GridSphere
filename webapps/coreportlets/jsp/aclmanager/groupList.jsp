<%@ page import="org.gridlab.gridsphere.portlet.PortletGroup,
                 org.gridlab.gridsphere.portlets.core.beans.AccessControllerBean,
                 java.util.List" %>
<%@ taglib uri="/portletWidgets" prefix="gs" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>
<portletAPI:init/>
<jsp:useBean id="aclManagerBean"
             class="org.gridlab.gridsphere.portlets.core.beans.AccessControllerBean"
             scope="request"/>
<form name="AccessControlManagerPortlet" method="POST" action="<%=aclManagerBean.getGroupListURI()%>">
  <input type="hidden" name="groupID" value=""/>
  <script language="JAVASCRIPT">
    function AccessControlManagerPortlet_listGroup_onClick() {
      document.AccessControlManagerPortlet.action="<%=aclManagerBean.getGroupListURI()%>";
      document.AccessControlManagerPortlet.submit();
    }

    function AccessControlManagerPortlet_newGroup_onClick(groupID) {
      document.AccessControlManagerPortlet.groupID.value="";
      document.AccessControlManagerPortlet.action="<%=aclManagerBean.getGroupEditURI()%>";
      document.AccessControlManagerPortlet.submit();
    }

    function AccessControlManagerPortlet_viewGroup_onClick(groupID) {
      document.AccessControlManagerPortlet.groupID.value=groupID;
      document.AccessControlManagerPortlet.action="<%=aclManagerBean.getGroupViewURI()%>";
      document.AccessControlManagerPortlet.submit();
    }
  </script>
<table border="0" cellspacing="1" cellpadding="2" width="100%">
  <tr>
    <td>
      <table bgcolor="BLACK" border="0" cellspacing="1" cellpadding="2" width="100%">
        <tr>
          <td bgcolor="#CCCCCC">
            <input type="button"
                   name="<%=AccessControllerBean.ACTION_GROUP_LIST%>"
                   value="Refresh List"
                   onClick="javascript:AccessControlManagerPortlet_listGroup_onClick()"/>
            &nbsp;&nbsp;<input type="button"
                   name="<%=AccessControllerBean.ACTION_GROUP_EDIT%>"
                   value="New Group"
                   onClick="javascript:AccessControlManagerPortlet_newGroup_onClick()"/>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table bgcolor="BLACK" border="0" cellspacing="1" cellpadding="2" width="100%">
        <tr>
          <td bgcolor="#CCCCCC">
            ID
          </td>
          <td bgcolor="#CCCCCC">
            Group Name
          </td>
        </tr>
<% List groupList = aclManagerBean.getGroupList();
   int numGroups = groupList.size();
   if (numGroups == 0) { %>
        <tr>
          <td bgcolor="WHITE" colspan="4">
            <font color="DARKRED">
              No group accounts in database.
            </font>
          </td>
        </tr>
<% } else {
     for (int ii = 0; ii < numGroups; ++ii) {
       PortletGroup group = (PortletGroup)groupList.get(ii); %>
        <tr>
          <td bgcolor="WHITE">
            <a href="javascript:AccessControlManagerPortlet_viewGroup_onClick('<%=group.getID()%>')">
              <%=group.getID()%>
            </a>
          </td>
          <td bgcolor="WHITE">
            <%=group.getName()%>
          </td>
        </tr>
<%   }
   } %>
      </table>
    </td>
  </tr>
</table>
</form>
