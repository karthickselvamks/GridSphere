package org.gridlab.gridsphere.provider.portletui.tags.gs;

import org.gridlab.gridsphere.provider.portletui.beans.ActionMenuItemBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

public class ActionMenuItemTagImpl extends ContainerTagImpl {

    protected boolean seperator = false;
    protected ActionMenuItemBean actionMenuItemBean = null;

    public boolean isSeperator() {
        return seperator;
    }

    public void setSeperator(boolean seperator) {
        this.seperator = seperator;
    }

    public int doStartTag() throws JspException {

        if (!beanId.equals("")) {
            actionMenuItemBean = (ActionMenuItemBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (actionMenuItemBean == null) {
                actionMenuItemBean = new ActionMenuItemBean();
            }
        } else {
            actionMenuItemBean = new ActionMenuItemBean();
        }

        // set info if not already set and we have something to set
        //if (info!=null && actionMenuItemBean.getInfo()==null) actionMenuItemBean.setInfo(info);

        Tag parent = getParent();
        if (parent instanceof ActionMenuTagImpl) {
            ActionMenuTagImpl actionMenuTag = (ActionMenuTagImpl) parent;
            if (actionMenuTag.getLayout() != null) actionMenuItemBean.setAlign(actionMenuTag.getLayout());
            if (actionMenuTag.getMenuType() != null) actionMenuItemBean.setMenuType(actionMenuTag.getMenuType());
        }

        try {
            JspWriter out = pageContext.getOut();

            out.print(actionMenuItemBean.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        try {
            JspWriter out = pageContext.getOut();
            out.print(actionMenuItemBean.toEndString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return EVAL_PAGE;
    }
}
