import json
import re
import traceback

def clean_country_of_origin(json_data):
    try:
    # Function to extract country name from the string with regex matching
        data = json.loads(json_data,strict=False)
        def extract_country(text):
            pattern = r"Country of Origin[:\s]*([A-Za-z\s]+)|Country of Origim[:\s]*([A-Za-z\s]+)|Country of Origindtal[:\s]*([A-Za-z\s]+)"
            match = re.search(pattern, text)
            if match:
                return next(group for group in match.groups() if group).strip()
            return ""

        # Update country names in the data
        for index, item in enumerate(data["tables"]["table"], start=1):

                country_value = item["Country Of Origin"]["value"]
                item["Country Of Origin"]["value"] = extract_country(country_value)

                # traceback.print_exc()  # Print the traceback of the error

        # Return the cleaned JSON as a string
        return json.dumps(data, indent=2, ensure_ascii=False)
    except Exception as e:
        return f"Error processing item at line {index}: {str(e)}"

