import cv2
import numpy as np
import fitz  # PyMuPDF
import os
import json
from pathlib import Path
import tempfile

def resize_image(image, max_dimension=1200):
    """
    Resize image while maintaining aspect ratio.
    """
    height, width = image.shape[:2]
    scale = min(max_dimension / width, max_dimension / height)
    if scale < 1:
        new_width = int(width * scale)
        new_height = int(height * scale)
        resized = cv2.resize(image, (new_width, new_height))
        return resized, scale
    return image, 1.0

def visualize_checked_boxes_with_lines(image, checked_boxes, max_display_dimension=1200):
    """
    Draw boxes around checked checkboxes with line numbers and Yes/No labels.
    """
    resized_image, scale = resize_image(image, max_display_dimension)

    for line_num, box in enumerate(checked_boxes, 1):
        x, y, w, h = box
        scaled_x = int(x * scale)
        scaled_y = int(y * scale)
        scaled_w = int(w * scale)
        scaled_h = int(h * scale)

        # Determine if this is Yes or No based on position
        is_yes = determine_yes_no_by_position(x)

        label = "Yes" if is_yes else "No"
        color = (0, 255, 0) if is_yes else (0, 0, 255)  # Green for Yes, Red for No

        # Draw colored box
        cv2.rectangle(resized_image,
                      (scaled_x, scaled_y),
                      (scaled_x + scaled_w, scaled_y + scaled_h),
                      color, 2)

        # Add line number and Yes/No label
        label_scale = max(0.5 * scale, 0.3)
        full_label = f"Line {line_num}: {label}"
        cv2.putText(resized_image, full_label,
                    (scaled_x, scaled_y-5),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    label_scale, color, 1)

    return resized_image

def resize_image(image, max_dimension=1200):
    """
    Resize image while maintaining aspect ratio.
    """
    height, width = image.shape[:2]
    scale = min(max_dimension / width, max_dimension / height)
    if scale < 1:
        new_width = int(width * scale)
        new_height = int(height * scale)
        resized = cv2.resize(image, (new_width, new_height))
        return resized, scale
    return image, 1.0

def extract_images_from_pdf(pdf_path, output_dir=None):
    """
    Extract images from PDF and save them to a temporary directory.
    Returns a list of image file paths.
    """
    # print(f"Extracting images from PDF: {pdf_path}")

    # Create temporary directory if no output directory specified
    if output_dir is None:
        output_dir = tempfile.mkdtemp()

    image_paths = []
    pdf_document = fitz.open(pdf_path)

    for page_num, page in enumerate(pdf_document):
        # Get the page's image list
        image_list = page.get_images(full=True)
        # print(f"Found {len(image_list)} images in page {page_num + 1}")

        # Alternatively, render page as image
        pix = page.get_pixmap(matrix=fitz.Matrix(300/72, 300/72))  # 300 DPI
        image_path = os.path.join(output_dir, f"page_{page_num + 1}.png")
        pix.save(image_path)
        image_paths.append(image_path)
        # print(f"Saved page {page_num + 1} as image: {image_path}")

    pdf_document.close()
    return image_paths

# Change this ration(0.10<x<0.40) for checkbox recognition
def is_checkbox_checked(checkbox_region):
    """
    Determine if a checkbox is checked using multiple criteria.
    """
    kernel = np.ones((2,2), np.uint8)
    cleaned = cv2.morphologyEx(checkbox_region, cv2.MORPH_OPEN, kernel)

    total_pixels = checkbox_region.shape[0] * checkbox_region.shape[1]
    filled_pixels = np.count_nonzero(cleaned)
    filled_ratio = filled_pixels / total_pixels

    h, w = checkbox_region.shape
    center_y, center_x = h // 2, w // 2
    margin_y, margin_x = int(h * 0.2), int(w * 0.2)
    center_region = cleaned[
                    center_y - margin_y:center_y + margin_y,
                    center_x - margin_x:center_x + margin_x
                    ]

    center_total = center_region.shape[0] * center_region.shape[1]
    center_filled = np.count_nonzero(center_region)
    center_ratio = center_filled / center_total if center_total > 0 else 0

    return 0.10 <= filled_ratio <= 0.40 and center_ratio > 0.15

def determine_yes_no_by_position(x):
    """
    Determine if a checkbox represents Yes or No based on x-coordinate position.
    """
    return x > 1500 and x<2000

def detect_checked_checkboxes(image_path, reference_width=41, reference_height=35):
    """
    Detect checked checkboxes in an image using reference dimensions.
    """
    # print(f"Processing image: {image_path}")

    image = cv2.imread(image_path)
    if image is None:
        # print(f"Failed to load image: {image_path}")
        return [], None

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    thresh = cv2.adaptiveThreshold(
        gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV, 11, 2
    )

    contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    # print(f"Found {len(contours)} contours")

    checked_boxes = []
    min_width = reference_width * 0.8
    max_width = reference_width * 1.3
    min_height = reference_height * 0.7
    max_height = reference_height * 1.3

    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)

        if (min_width <= w <= max_width and
                min_height <= h <= max_height):

            perimeter = cv2.arcLength(contour, True)
            approx = cv2.approxPolyDP(contour, 0.04 * perimeter, True)

            if len(approx) == 4:
                padding = 2
                y1 = max(0, y - padding)
                y2 = min(thresh.shape[0], y + h + padding)
                x1 = max(0, x - padding)
                x2 = min(thresh.shape[1], x + w + padding)
                checkbox_region = thresh[y1:y2, x1:x2]

                if is_checkbox_checked(checkbox_region):
                    checked_boxes.append((x, y, w, h))
                    # print(f"Found checked checkbox at: x={x}, y={y}")

    checked_boxes.sort(key=lambda box: box[1])
    return checked_boxes, image

def update_json_with_results(json_string, line_results):
    """
    Updates JSON string with checkbox results.
    """
    try:
        json_data = json.loads(json_string,strict=False)
        updated_json = json_data.copy()

        field_mapping = {
            1: "check cashing services",
            2: "issue or cash travelers checks or money orders",
            3: "offer prepaid cards",
            4: "provide money transmission or foreign exchange services"
        }

        for line_num, is_yes in enumerate(line_results, 1):
            field_name = field_mapping.get(line_num)
            if field_name and field_name in updated_json['fields']:
                updated_json['fields'][field_name]['value'] = "Yes" if is_yes else "No"

        return json.dumps(updated_json, indent=2)
    except Exception as e:
        # print(f"Error updating JSON: {e}")
        return json_string

def process_pdf_document(input_dict):
    """
    Process a PDF document by extracting and analyzing images.
    """
    pdf_path = input_dict.get("document_path")
    json_string = input_dict.get("json_string")

    # print(f"Processing PDF: {pdf_path}")

    try:
        # Extract images from PDF
        image_paths = extract_images_from_pdf(pdf_path)

        all_checked_boxes = []

        # Process each extracted image
        for image_path in image_paths:
            checked_boxes, image = detect_checked_checkboxes(image_path)
            all_checked_boxes.extend(checked_boxes)

            # Optional: Save annotated image for debugging
            if checked_boxes and image is not None:
                debug_path = image_path.replace('.png', '_annotated.png')
                for x, y, w, h in checked_boxes:
                    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.imwrite(debug_path, image)
                # print(f"Saved annotated image: {debug_path}")

        # Convert checked boxes to line results
        line_results = []
        for box in all_checked_boxes:
            x = box[0]
            is_yes = determine_yes_no_by_position(x)
            line_results.append(is_yes)

        # Update JSON with results
        updated_json = update_json_with_results(json_string, line_results)

        return updated_json

    except Exception as e:
        # print(f"Error in process_pdf_document: {e}")
        return None, json_string

if __name__ == "__main__":
    # Example usage
    input_dict = {
                "document_path": r'C:\Users\syed.hasnain\Downloads\KYC_Sample1.pdf',
                "json_string": '''
                {"pages":[{"width":2486,"height":3513}],"fields":{"check cashing services":{"value":"","bounds":""},"offer prepaid cards":{"value":"","bounds":""},"issue or cash travelers checks or money orders":{"value":"","bounds":""},"What is the company's estimated or projected annual revenue/budget (USD)? None and N/A are not allowed. If none, please indicate with $0":{"value":"$8,789,000","bounds":"0,0,-1,-1"},"provide money transmission or foreign exchange services":{"value":"","bounds":""}},"tables":{}}
                '''
            }
#
#     # Process document
    updated_json = process_pdf_document(input_dict)
    print(updated_json)
    # if checkboxes:
    #     print("\nDetected Checkboxes:")
    #     for checkbox in checkboxes:
    #         print(f"Position: ({checkbox[0]}, {checkbox[1]})")
    #         print(f"Size: {checkbox[2]}x{checkbox[3]}\n")
    #
    #     print("\nUpdated JSON:")
    #     print(updated_json)
    # else:
    #     print("\nNo checkboxes were detected or an error occurred")