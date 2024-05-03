/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.model;

import java.util.List;

public class Field {

    private String name;
    private String displayName;
    private String dataType;
    private List<String> defaultAliases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<String> getDefaultAliases() {
        return defaultAliases;
    }

    public void setDefaultAliases(List<String> defaultAliases) {
        this.defaultAliases = defaultAliases;
    }
}
