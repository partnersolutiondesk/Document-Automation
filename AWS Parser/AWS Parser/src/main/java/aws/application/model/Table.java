/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.model;

import java.util.List;

public class Table {

    private String name;

    private List<Field> tableHeaders;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getTableHeaders() {
        return tableHeaders;
    }

    public void setTableHeaders(List<Field> tableHeaders) {
        this.tableHeaders = tableHeaders;
    }
}
