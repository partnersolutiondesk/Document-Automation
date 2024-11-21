import json


def extract_entries(rec):
    try:
        data = json.loads(rec,strict = False)
        extracted_data = []
        table_rows = data['tables']['table']
        for i in range(0, len(table_rows), 2):
            if i + 1 < len(table_rows):
                if not table_rows[i]["Material"]["value"] or not table_rows[i + 1]["Price"]["value"]:
                    continue
                # Prepare the entry
                entry = {
                    "Price": {
                        "value": table_rows[i + 1]["Price"]["value"],
                        "bounds": table_rows[i + 1]["Price"]["bounds"]
                    },
                    "Net Value": {
                        "value": table_rows[i + 1]["Net Value"]["value"],
                        "bounds": table_rows[i + 1]["Net Value"]["bounds"]
                    },
                    "Material": {
                        "value": table_rows[i]["Material"]["value"],
                        "bounds": table_rows[i]["Material"]["bounds"]
                    },
                    "Order quantity": {
                        "value": table_rows[i + 1]["Material"]["value"],
                        "bounds": ""
                    }
                }
                extracted_data.append(entry)
                data["tables"]["table"] = extracted_data
                updated_json = json.dumps(data, indent=3)
        return updated_json
    except Exception as e:
        f"Error processing item at line {str(e)}"



