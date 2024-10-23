# Logic to Display Cluster ID

Every document that is processed in Document Automation falls into a cluster. If feedback is applied to a document that feedback is applied across the whole cluster. But there are times when the feedback is not applied and there is a need to find out which cluster the document has fallen into and if it is able/not able to retrieve the feedback. The below bot can be used to display the cluster ID of any document in any learning instance in the form of form field in the learning instance.

## Instructions

Step 1: Add a form form field in the learning instance and name it ‘clusterID’.

Step 2: Create a new task bot and follow the steps from step 3.

Step 3: Download the Document Engine data following the steps in the below article
https://apeople.automationanywhere.com/s/article/How-to-download-the-Document-Automation-Engine-Data-Json-to-local

Step 4: Use the ‘Document Extraction:Get Document Data’ to fetch the Dictionary data.


Step 5: Use the Json packages to skim through engine data(step 3) and find the cluster ID from it. As shown in the snapshot below, it is in the ‘engineData.metadata.clusterId’

Step 6: Once found, update it in the Dictionary Data that we have from step 4.


Step 7: Use this dictionary as input parameter for ‘Document Extraction: Update Document Data’


Step 8: Once the bot is ready, it needs to be included inside the co-pilot process of the learning instance.

docID and filename are the input variables in the bot which enables us to use Document Extraction package dynamically,
docID results from Extraction bot output.
fileName can be fetched from the input variables of the process.

