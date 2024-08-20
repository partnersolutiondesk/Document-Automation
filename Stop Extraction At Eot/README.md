In the project you will find a bot and python code which helps you to stop the extraction at the end of table indicator

### Find below the flow of the bot:

* The 'Document Extraction: Download Data' package downloads the engine data json of DA where all the metadata, document data, extraction data, ocr data etc.. is found.
* Using this command the engine data json needs to be downloaded to local.

* After downloading, the json is parsed to find the end of table indicator.

* Using this code we're filtering the data right when the end of the table indicator is found

* The input is a list, list[0] is getJsonData json, list[1] is filepath where engine data of DA is stored.


