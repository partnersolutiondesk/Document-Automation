import json
import logging
import traceback
import datetime
import os

def logConfig(path, botName):
    # if you are invoking directly or indirectly from a bot, give a path other than the default path of python which
    # is os.getcwd(). The reason is file creation will not be allowed in default path of bot run time which will also
    # be the python run time.

    if path != "":
        os.chdir(path)
    currDate = str(datetime.date.today())
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s:%(levelname)s:%(name)s:%(message)s')
    file_handler = logging.FileHandler(botName+'_'+currDate+".log")
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    return logger

def data_manipulation(py_manipulation_input):
    try:
        getJsonData = py_manipulation_input['getDataJson']
        fieldName = py_manipulation_input['fieldName']
        logPath = py_manipulation_input['logPath']
        botName = py_manipulation_input['botName']
        ocrResult = py_manipulation_input['ocrResult']
        y = json.loads(getJsonData,strict = False)
        y['fields'][fieldName]['value'] = ocrResult
        updated_data = json.dumps(y)
        logger = logConfig(logPath, botName)
        return updated_data

    except Exception as innerException:
        logger.info("------------------------EXCEPTION FOUND--------------------")
        # Get the current line number where the exception occurred
        line_number = traceback.extract_tb(innerException.__traceback__)[-1].lineno
        # Get the error message
        error_message = str(innerException)
        # print(line_number, error_message)
        logger.error(f"line_number: {line_number}, error_message:{error_message}")
        return "Python Error Encountered in line number:"+ str(line_number)+' with error message:'+error_message


def find_values_after_sequences(input_data):
    try:
        # Extract file path and phrases from the input dictionary
        file_path = input_data['file_path']
        phrases = input_data['phrases']
        logPath = input_data['logPath']
        botName = input_data['botName']

        # Load JSON data from the file
        with open(file_path, 'r') as file:
            ocr_result = json.load(file)

        # Parse the blocks from the JSON data
        blocks = ocr_result['engineData']['ocrResult']['blocks']

        results = {}

        for phrase in phrases:
            # Split the phrase into sequence of words
            sequence = phrase.split()
            sequence_len = len(sequence)

            # Traverse the blocks to find the sequence and extract the value that follows
            found = False
            for i in range(len(blocks) - sequence_len):
                match = True
                for j in range(sequence_len):
                    if blocks[i + j]['text'].upper() != sequence[j].upper():
                        match = False
                        break
                if match:
                    # Store the text of the block that follows the sequence
                    results[phrase] = blocks[i + sequence_len]['text']
                    found = True
                    break

            if not found:
                results[phrase] = ""
                logger = logConfig(logPath, botName)
                logger.info("------------------------EXCEPTION FOUND--------------------")
                logger.info(" one or more "+"sequence not found")

        return json.dumps(results)

    except Exception as innerException:
        logger.info("------------------------EXCEPTION FOUND--------------------")
        # Get the current line number where the exception occurred
        line_number = traceback.extract_tb(innerException.__traceback__)[-1].lineno
        # Get the error message
        error_message = str(innerException)
        # print(line_number, error_message)
        logger.error(f"line_number: {line_number}, error_message:{error_message}")
        return "Python Error Encountered in line number:"+ str(line_number)+' with error message:'+error_message

# Example usage
# input_data = {
#     "file_path":r'C:\Users\syed.hasnain\Downloads\02e2c853-e65f-4483-a921-94c441b55b40_GXO Ankeny 04.01.23 Storage.json',
#     "phrases": ["STORAGE BILLED",'Total pallet billed','Total pallets billed'],
#     "botName": "gxo_data_manipulation",
#     "logPath":r'C:\Users\syed.hasnain\Downloads\Python Logs'
# }
#
# # Find and print the values after the sequences
# values = find_values_after_sequences(input_data)
# print(values)

input_data = {
    "getDataJson":'{"pages":[{"width":2550,"height":3300},{"width":2550,"height":3300},{"width":2550,"height":3300},{"width":2550,"height":3300}],"fields":{"Total Pallets Billed":{"value":"203","bounds":"413,2255,81,32"},"Storage Billed":{"value":"$10,940.62","bounds":"1702,1062,199,32"},"po_number":{"value":"1400072914","bounds":"949,608,290,33"},"total_amount":{"value":"250753.65","bounds":"1642,2254,262,32"},"dummyField":{"value":"","bounds":""},"receiver_address":{"value":"GXO WAREHOUSE COMPANY INC. \nC/O JPMORGAN \n29561 NETWORK PLACE \nCHICAGO, IL 60673-1295","bounds":"1443,557,792,193"},"invoice_number":{"value":"10543807","bounds":"2110,253,228,32"},"invoice_date":{"value":"4/01/23","bounds":"2155,400,200,39"}},"tables":{}}',
    "fieldName": "Storage Billed",
    "botName": "gxo_data_manipulation",
    "logPath":r'C:\Users\syed.hasnain\Downloads\Python Logs',
    "ocrResult":"1234"
}

# Find and print the values after the sequences
values = data_manipulation(input_data)
print(values)
