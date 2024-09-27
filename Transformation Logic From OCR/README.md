In the project you will find a bot and python code which helps you to find the data from the OCR and map it to it's respective field

### Find below the flow of the bot:

* The 'Document Extraction: Download Data' package downloads the engine data json of DA where all the metadata, document data, extraction data, ocr data etc.. is found.
* Using this command the engine data json needs to be downloaded to local.

* After downloading, the json is parsed to find 2 or more consecutive words of a single field inside the OCR data, once it's found, the next block in the json is going to be the value for it. This is achieved using python code.(Ex: if fieldname is 'total amount', and DA is not able to recognize it as a single field then in the OCR result, it will be present as 'total' in a block and 'amount' in another block).

* The same can be done for multiple fields using the same code. 

* The input is dictionary and inside the dictionary a list of inputs(field names) needs to be passed.  

* The value needs to be retreived and needs to be passed as the input for the field that was left unrecognized.
