# PDF Checkbox Detection

## Overview

This Python project extracts images from a PDF document, detects checkboxes within those images, determines whether the checkboxes are checked, and updates a JSON object with the results. The detection process uses OpenCV for image processing and PyMuPDF for PDF manipulation.

## Features

- Extracts images from PDF documents.

- Detects checkboxes in extracted images.

- Determines whether checkboxes are checked based on pixel density and position.

- Updates a given JSON structure with the checkbox values.

## Requirements

Ensure you have the following dependencies installed before running the script:

- pip install opencv-python numpy pymupdf

## Usage

1. Input Data

PDF Document: A PDF file containing checkboxes.

JSON String: A JSON structure with predefined fields where checkbox values need to be updated.

2. Running the Script

from your_script import process_pdf_document

input_data = {
    "document_path": "path/to/your/document.pdf",
    "json_string": "{\"fields\": {\"check cashing services\": {\"value\": \"No\"}, ...}}"
}

checked_boxes, updated_json = process_pdf_document(input_data)

print("Checked checkboxes:", checked_boxes)
print("Updated JSON:", updated_json)

3. Output

A list of checked checkbox coordinates.

An updated JSON string reflecting the checkbox states.

## Configuration

Adjust the checkbox detection ratio in is_checkbox_checked() to fine-tune detection accuracy.

Modify determine_yes_no_by_position() if checkboxes are positioned differently in your document.

Reference checkbox dimensions can be adjusted in detect_checked_checkboxes() if needed.

## Debugging

Annotated images with detected checkboxes are saved for debugging.

Print statements help track processing steps.

Use the 'resize_image' and 'visualize_checked_boxes_with_lines' to visualize the image with checkboxes detected

