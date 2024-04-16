# AWS Texract Parser 

In this project we see how we can create a custom parser which DA uses for it's extraction

We have used AWS-textractor as a third party parser for this demo

# Pre-requisites to run the bot
* Minimum v29 Control room
* IQBot admin role in the same control room
* AWS account with textract access.
* IDPDWPages License
* AWS secret key and access key.
* JSON file with all the specified fields in the format that can be found in the JSON in the resources of the project

#### Step 1:
* From the repository go to AWS Parser -> build -> libs
* Find the jar file and download it to local


#### Step 2:
* Upload it to the control room by browsing to manage->packages 
* Click on add package and upload the jar file

#### Step 3:
* Go to Security credentials
* Create your credentials(secret key and access key)

#### Step 4:
* Go to the learning instance page and click on 'configure parser' button
* Click on the 'create parser configuration'
* Fill out the details on the form like parser name, provider name, document type etc.
* In the package section, select the package that you uploaded in step 2.
* Pick the credentials that you created in step 2.
* Click on next.
* Upload the JSON file with the field details


#### Step 5:
* Go back to the learning instance and click on learning instance.
* Name the learning instance 
* Select the document that was given during parser configuration.
* The other fields will be automatically populated.
* Click on create


 


