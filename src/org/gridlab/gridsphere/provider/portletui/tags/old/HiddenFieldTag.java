/*
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.provider.portletui.tags.old;

import org.gridlab.gridsphere.provider.ui.beans.HiddenFieldBean;

import javax.servlet.jsp.JspException;

public class HiddenFieldTag extends BaseTag {

    public int doStartTag() throws JspException {
        if (bean.equals("")) {
            this.htmlelement = new HiddenFieldBean (name, value);
        }
        return super.doStartTag();
    }

}
