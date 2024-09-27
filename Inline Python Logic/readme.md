# Python logic to manipulate the extraction data

This project consists of codes to merge a row, split a row on the basis of presence of a ',' and read the data from the API and replace that in our extraction result

### Dependencies

* json
* requests

### Flow

* Have 3 methods(split_logic, read_date_from_api, row_merge) respectively, 
* split_logic splits the data from a row where ',' is present in the description and adds the second part(after comma) to the next row
* read_date_from_api invokes an api and gets the response, stores the date from the response and replaces the extracted 'invoice_date' with the response_date
* row_merge method merges the rows to the previous row in case the 'quantity','unit_price' and 'total_price' is 0 for any particular row 

#### Note : Please replace the field names with actual field names in your code and try running the code.
#### A sample to run the code on an IDE has been provided , uncomment the lines to run and test the code
