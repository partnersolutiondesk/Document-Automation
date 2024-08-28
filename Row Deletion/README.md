
# Bot to stop extraction at the end of the table indicator







## Documentation

The script contains methods to fetch the end of the table dynamically from the document automation engine.It also fetches the 'primary column' from it.

 Use conditional check which is a parameter "check_type" for the function 'eot' to stop extraction at end of the table, 'pc_null_check' to delete all rows if primary column is empty, 'empty_rows' to delete the rows where all the data is missing for rows.
 
 You can also use 'eot AND pc_null_check' , 'eot OR pc_null_check' as conditional statements.

The other parameters contain "json_data" that we get from the 'Document Extraction:Get Document Data', "file_path" which has the filepath for the DA engine data, "columns" this is a list type and all the columns need to be added in this attribute one by one,"logPath" and "botName" are for logging the events in the scripts

The bot in the repo has everything built, just add the values to the parameters and it ready to be executed. Look at the variables in the variable sections, and add values as and when required. The only values that needs to be given is for 'outputFolder' and 'listOfColumns' where you need to add the column names in the list. And the check_type.

Add this bot after your 'extraction bot' in the learning instance co-pilot process.



