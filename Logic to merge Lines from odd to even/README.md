
# Logic to pick values from next row and add it to the current row







## Documentation

The script contains methods to manipulate the JSON data that is retreived from the Get Data Package of Document Automation.

This logic primarily focuses on tables where data is on multiple levels. 
Only 'Material' is fetched in the odd rows. The rest of the columns are fetched in even lines because of the difference in levels of the same rows.

This logic handles those kind of tables.