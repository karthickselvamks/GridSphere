/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.beans;

/**
 * An abstract <code>InputBean</code> provides a generic input HTML element
 */
public abstract class InputBean extends BaseComponentBean implements TagBean {

    public static final String INPUT_STYLE = "portlet-form-input-field";

    protected String inputtype = "";
    protected int size = 0;
    protected int maxlength = 0;

    /**
     * Constructs a default input bean
     */
    public InputBean() {
        this.cssStyle = INPUT_STYLE;
    }

    /**
     * Constructs an input bean with a supplied name
     *
     * @param name the bean name
     */
    public InputBean(String name) {
        super(name);
        this.cssStyle = INPUT_STYLE;
    }

    /**
     * Returns the size of this input element
     *
     * @return the size of this input element
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size of this input element
     *
     * @param size the size of this input element
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns the maximum length of this input element
     *
     * @return the maximum length of this input element
     */
    public int getMaxLength() {
        return maxlength;
    }

    /**
     * Sets the maximum length of this input element
     *
     * @param maxlength the maximum length of this input element
     */
    public void setMaxLength(int maxlength) {
        this.maxlength = maxlength;
    }

    public String toStartString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<input class=\"" + cssStyle + "\" ");
        sb.append("type=\"" + inputtype + "\" ");


        String pname = (name == null) ? "" : name;
        String sname = pname;
        if (!beanId.equals("")) {
            sname = "ui_" + vbName + "_" + beanId + "_" + pname;
        }

        sb.append("name=\"" + sname + "\" ");
        if (value != null) sb.append("value=\"" + value + "\" ");
        if (size != 0) sb.append("size=\"" + size + "\" ");
        if (maxlength != 0) sb.append("maxlength=\"" + maxlength + "\" ");
        sb.append(checkReadOnly());
        sb.append(checkDisabled());
        sb.append("/>");
        return sb.toString();
    }

    public String toEndString() {
        return "";
    }

}
