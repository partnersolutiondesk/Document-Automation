import json
import requests

def split_logic(rec):

    try:
        data = json.loads(rec, strict = False)

        table_array = data["tables"]["table"]

        i = 0
        while i < len(table_array) - 1:
            current_entry = table_array[i]
            next_entry = table_array[i + 1]

            current_description = current_entry.get("description", {"value": ""})["value"]
            description_parts = current_description.split(",")

            if len(description_parts) > 1:
                current_entry["description"]["value"] = description_parts[0]
                next_entry["description"]["value"] = description_parts[1] + " " + next_entry["description"]["value"]

            i += 1

        updated_json = json.dumps(data, indent=3)
        return updated_json
    except Exception as e:
        return "Error:", e

def read_date_from_api(rec):
    try:
        #calling an api to get data
        api_url = 'http://worldtimeapi.org/api/timezone/Asia/Kolkata'
        response = requests.get(api_url)

        #checking if status code is 200, if 200, getting "utc_datetime"
        if(response.status_code == 200):
            new_date_data = response.json()
            new_date = new_date_data.get('utc_datetime')
        else:
            print(f"API request failed with status code: ",{response.status_code})

        #loading the json data extracted from DA
        y = json.loads(rec,strict = False)
        #setting invoice date same as 'utc_datetime
        y['fields']['invoice_date']['value'] = new_date[:10]
        updated_data = json.dumps(y)
        return updated_data
    except Exception as e:
        return "Error: ",e
        #print("Error: ",e)

def row_merge(rec):
    try:
        # Load the JSON data
        data = json.loads(rec,strict = False)

        # Get the "table" array
        table_array = data["tables"]["table"]

        # Create a new list for updated table items
        updated_table = []

        i = 0
        while i < len(table_array):
            current_entry = table_array[i]

            # Check if quantity, total price, and unit price are zero
            if (
                    current_entry["quantity"]["value"] == "0" and
                    current_entry["unit_price"]["value"] == "0" and
                    current_entry["total_price"]["value"] == "0.00"
            ):
                # Append the description to the previous line item
                if i > 0:
                    previous_entry = updated_table.pop()
                    previous_entry["description"]["value"] += " " + current_entry["description"]["value"]
                    updated_table.append(previous_entry)
            else:
                updated_table.append(current_entry)

            i += 1

            # Update the "table" array with the filtered list
        data["tables"]["table"] = updated_table

        # Print the updated JSON data
        updated_json = json.dumps(data, indent=3)
        return updated_json

    except Exception as e:
        return "Error:", e

#Uncomment to run row_merge
#print(row_merge('{"pages":[{"height":3507,"width":2480}],"fields":{"total_amount":{"value":"15.40","bounds":"2123,2487,134,39"},"receiver_address":{"value":"Del RPA \nBangalore \nKarnataka \nUnited States","bounds":"139,946,338,385"},"ship_to_address":{"value":"Del RPA \nKarnataka \nUnited States","bounds":"137,332,343,276"},"Sale Tax":{"value":"1.40","bounds":"2159,2317,102,39"},"invoice_number":{"value":"31","bounds":"1746,828,59,39"},"invoice_date":{"value":"172023","bounds":"1825,956,210,45"},"Sub Total":{"value":"14.00","bounds":"2127,2168,134,39"}},"tables":{"table":[{"quantity":{"value":"2","bounds":"1456,1667,26,39"},"total_price":{"value":"6.00","bounds":"2144,1667,104,40"},"description":{"value":"Item 1, Description Item 2","bounds":"184,1668,434,112"},"unit_price":{"value":"3","bounds":"1828,1667,25,40"}},{"quantity":{"value":"0","bounds":"1455,1911,29,39"},"total_price":{"value":"0","bounds":"2144,1911,104,39"},"description":{"value":"Description","bounds":"184,1911,267,49"},"unit_price":{"value":"0","bounds":"1828,1911,26,39"}}]}}'))

#Uncomment to run split_logic
#print(split_logic('{"pages":[{"height":3507,"width":2480}],"fields":{"total_amount":{"value":"15.40","bounds":"2123,2487,134,39"},"receiver_address":{"value":"Del RPA \nBangalore \nKarnataka \nUnited States","bounds":"139,946,338,385"},"ship_to_address":{"value":"Del RPA \nKarnataka \nUnited States","bounds":"137,332,343,276"},"Sale Tax":{"value":"1.40","bounds":"2159,2317,102,39"},"invoice_number":{"value":"31","bounds":"1746,828,59,39"},"invoice_date":{"value":"172023","bounds":"1825,956,210,45"},"Sub Total":{"value":"14.00","bounds":"2127,2168,134,39"}},"tables":{"table":[{"quantity":{"value":"2","bounds":"1456,1667,26,39"},"total_price":{"value":"6.00","bounds":"2144,1667,104,40"},"description":{"value":"Item 1, Description Item 2","bounds":"184,1668,434,112"},"unit_price":{"value":"3","bounds":"1828,1667,25,40"}},{"quantity":{"value":"0","bounds":"1455,1911,29,39"},"total_price":{"value":"0","bounds":"2144,1911,104,39"},"description":{"value":"Description","bounds":"184,1911,267,49"},"unit_price":{"value":"0","bounds":"1828,1911,26,39"}}]}}'))

#Uncomment to run read_date_from_api
#print(read_date_from_api('{"pages":[{"height":3507,"width":2480}],"fields":{"total_amount":{"value":"220.00","bounds":"2087,2317,170,39"},"receiver_address":{"value":"Del RPA \nBangalore \nKarnataka \nUnited States","bounds":"139,947,338,384"},"ship_to_address":{"value":"Del RPA \nKarnataka \nUnited States","bounds":"137,332,343,276"},"Sale Tax":{"value":"20.00","bounds":"2124,2147,137,40"},"invoice_number":{"value":"027","bounds":"1746,828,92,39"},"invoice_date":{"value":"Jul 17, 2023","bounds":"1738,956,298,44"},"Sub Total":{"value":"200.00","bounds":"2091,1999,170,39"}},"tables":{"table":[{"quantity":{"value":"2","bounds":"1456,1667,26,39"},"total_price":{"value":"200.00","bounds":"2080,1667,168,40"},"description":{"value":"Invoice 5 Item Description. E.g. Spare \npart for Project X","bounds":"183,1667,889,123"},"unit_price":{"value":"100.00","bounds":"1689,1667,165,40"}}]}}'))
