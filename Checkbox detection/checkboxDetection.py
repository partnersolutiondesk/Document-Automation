import cv2
import numpy as np
import pytesseract
from PIL import Image
import fitz
import re
import json
import os
import tempfile
from pathlib import Path

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# def print_all_detections_single_line(text_regions):
#     """
#     Print all detected text, checkboxes, and yes/no statuses in a single line.
#     """
#     results = []
#
#     for text, box, field_name, checkbox_status, checked_checkboxes, all_checkboxes in text_regions:
#         checkbox_info = f"Checked" if checked_checkboxes else "Unchecked"
#         status = checkbox_status if checkbox_status else "None"
#         results.append(f"{field_name}({checkbox_info}:{status})")
#
#     print(" | ".join(results))

def calculate_iou(box1, box2):
    """
    Calculate Intersection over Union (IoU) between two boxes.
    Each box should be in format (x, y, w, h)
    """
    # Convert to x1, y1, x2, y2 format
    box1_x1, box1_y1 = box1[0], box1[1]
    box1_x2, box1_y2 = box1[0] + box1[2], box1[1] + box1[3]

    box2_x1, box2_y1 = box2[0], box2[1]
    box2_x2, box2_y2 = box2[0] + box2[2], box2[1] + box2[3]

    # Calculate intersection coordinates
    x_left = max(box1_x1, box2_x1)
    y_top = max(box1_y1, box2_y1)
    x_right = min(box1_x2, box2_x2)
    y_bottom = min(box1_y2, box2_y2)

    if x_right < x_left or y_bottom < y_top:
        return 0.0

    intersection_area = (x_right - x_left) * (y_bottom - y_top)

    # Calculate union area
    box1_area = (box1_x2 - box1_x1) * (box1_y2 - box1_y1)
    box2_area = (box2_x2 - box2_x1) * (box2_y2 - box2_y1)
    union_area = box1_area + box2_area - intersection_area

    iou = intersection_area / union_area if union_area > 0 else 0.0
    return iou

def remove_overlapping_checkboxes(checkboxes, iou_threshold=0.3):
    """
    Remove overlapping checkboxes using IoU.
    """
    if not checkboxes:
        return []

    sorted_boxes = sorted(checkboxes,
                          key=lambda x: x[2] * x[3],
                          reverse=True)

    kept_boxes = []
    for current_box in sorted_boxes:
        should_keep = True

        for kept_box in kept_boxes:
            iou = calculate_iou(
                (current_box[0], current_box[1], current_box[2], current_box[3]),
                (kept_box[0], kept_box[1], kept_box[2], kept_box[3])
            )

            if iou > iou_threshold:
                should_keep = False
                break

        if should_keep:
            kept_boxes.append(current_box)

    return kept_boxes

def find_yes_no_text(ocr_data, checkbox_x, line_num):
    """
    Find 'yes' or 'no' text that appears after the checkbox on the same line.
    """
    # print(f"\nDEBUG - Looking for yes/no after checkbox at x={checkbox_x} on line {line_num}")
    yes_variants = ['yes', 'y']
    no_variants = ['no', 'n']
    candidates = []

    for i in range(len(ocr_data['text'])):
        text = ocr_data['text'][i].lower().strip()
        text_x = ocr_data['left'][i]
        curr_line = ocr_data['line_num'][i]

        # print(f"  Checking text: '{text}' at x={text_x} on line {curr_line}")

        if curr_line != line_num:
            continue
        # this condition is when 'yes/no' values are on the right side of the checkbox
        # if in case 'yes/no' values come before the checkbox the condition is inverted
        # text_x < checkbox_x
        if text_x > checkbox_x and (text in yes_variants or text in no_variants):
            candidates.append((text_x, 'Yes' if text in yes_variants else 'No'))
            # print(f"    Found candidate: {text} at x={text_x}")

    if candidates:
        candidates.sort(key=lambda x: x[0])
        result = candidates[0][1]
        # print(f"  Selected result: {result}")
        return result

    # print("  No yes/no text found")
    return None

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
    margin_y, margin_x = int(h * 0.25), int(w * 0.25)
    center_region = cleaned[
                    center_y - margin_y:center_y + margin_y,
                    center_x - margin_x:center_x + margin_x
                    ]

    center_total = center_region.shape[0] * center_region.shape[1]
    center_filled = np.count_nonzero(center_region)
    center_ratio = center_filled / center_total if center_total > 0 else 0

    if 0.10 <= filled_ratio <= 0.45 and center_ratio > 0.12:
        return True

    return False

def detect_checkboxes(image, text_regions):
    """
    Detect checkboxes using text height as reference.
    """
    if len(image.shape) == 3:
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    else:
        gray = image.copy()

    blurred = cv2.GaussianBlur(gray, (3, 3), 0)
    thresh = cv2.adaptiveThreshold(
        blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV, 11, 2
    )

    kernel = np.ones((2,2), np.uint8)
    thresh = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

    contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

    # Fix the tuple unpacking to match the structure
    heights = [box[3] for text, box, field_name, checkbox_status, checkboxes, all_boxes in text_regions]
    reference_height = np.median(heights) if heights else 50

    min_height = reference_height * 0.75
    max_height = reference_height * 1.3

    checkboxes = []
    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)
        aspect_ratio = w / float(h)

        if not (min_height <= h <= max_height and 0.8 <= aspect_ratio <= 1.2):
            continue

        hull = cv2.convexHull(contour)
        hull_area = cv2.contourArea(hull)
        contour_area = cv2.contourArea(contour)
        solidity = contour_area / hull_area if hull_area > 0 else 0

        if cv2.isContourConvex(hull) and solidity > 0.7:
            padding = 2
            y1 = max(0, y - padding)
            y2 = min(thresh.shape[0], y + h + padding)
            x1 = max(0, x - padding)
            x2 = min(thresh.shape[1], x + w + padding)

            if y2 <= y1 or x2 <= x1:
                continue

            checkbox_region = thresh[y1:y2, x1:x2]
            is_checked = is_checkbox_checked(checkbox_region)
            checkboxes.append((x, y, w, h, is_checked))

    return remove_overlapping_checkboxes(checkboxes)

def extract_text_regions(image_path, field_names):
    """
    Extract text regions and detect checkboxes.
    """
    try:
        pil_image = Image.open(image_path)
        ocr_data = pytesseract.image_to_data(pil_image, output_type=pytesseract.Output.DICT)
        cv_image = cv2.imread(image_path)

        if cv_image is None:
            raise ValueError(f"Failed to load image at {image_path}")
    except Exception as e:
        # print(f"Error loading image: {e}")
        return [], None

    text_regions = []
    n_boxes = len(ocr_data['text'])
    current_phrase = []
    current_boxes = []
    current_line_num = None

    for i in range(n_boxes):
        if int(ocr_data['conf'][i]) < 0:
            continue

        text = ocr_data['text'][i].strip()
        if not text:
            continue

        x = ocr_data['left'][i]
        y = ocr_data['top'][i]
        w = ocr_data['width'][i]
        h = ocr_data['height'][i]
        line_num = ocr_data['line_num'][i]

        current_phrase.append(text)
        current_boxes.append((x, y, w, h))
        current_line_num = line_num

        if i + 1 == n_boxes or ocr_data['line_num'][i] != ocr_data['line_num'][i + 1]:
            full_phrase = ' '.join(current_phrase).lower()

            for field_name in field_names:
                field_pattern = re.escape(field_name.lower())
                if re.search(field_pattern, full_phrase):
                    x_min = min(box[0] for box in current_boxes)
                    y_min = min(box[1] for box in current_boxes)
                    x_max = max(box[0] + box[2] for box in current_boxes)
                    y_max = max(box[1] + box[3] for box in current_boxes)

                    combined_box = (x_min, y_min, x_max - x_min, y_max - y_min)
                    text_regions.append((full_phrase, combined_box, field_name, None, [], current_line_num))
                    # print(text_regions)

            current_phrase = []
            current_boxes = []
            current_line_num = None

    checkboxes = detect_checkboxes(cv_image, text_regions)

    # Inside extract_text_regions function, where checkboxes are processed:
    final_text_regions = []
    for text, box, field_name, _, _, line_num in text_regions:
        # print(f"\nDEBUG - Processing field: {field_name}")
        field_y = box[1] + box[3]/2
        associated_checkboxes = []
        all_checkboxes_in_line = []

        # print(f"  Field center y: {field_y}")

        for checkbox in checkboxes:
            checkbox_x, checkbox_y, checkbox_w, checkbox_h, is_checked = checkbox
            checkbox_y_center = checkbox_y + checkbox_h/2
            vertical_distance = abs(checkbox_y_center - field_y)

            # print(f"  Checking checkbox at ({checkbox_x}, {checkbox_y}) - checked: {is_checked}")
            # print(f"    Vertical distance: {vertical_distance}")

            if vertical_distance < max(30, box[3]):
                all_checkboxes_in_line.append((checkbox_x, checkbox_y, checkbox_w, checkbox_h))
                # print(f"    Added to line")
                if is_checked:
                    associated_checkboxes.append((checkbox_x, checkbox_y, checkbox_w, checkbox_h))
                    # print(f"    Checkbox is checked - added to associated")

        checkbox_status = None
        if associated_checkboxes:
            # print(f"  Found {len(associated_checkboxes)} checked checkbox(es)")
            associated_checkboxes.sort(key=lambda x: x[0])
            checked_x = associated_checkboxes[0][0]
            checkbox_status = find_yes_no_text(ocr_data, checked_x, line_num)
            # print(f"  Final status for {field_name}: {checkbox_status}")

        final_text_regions.append((text, box, field_name, checkbox_status, associated_checkboxes, all_checkboxes_in_line))
        # print(final_text_regions)
    return final_text_regions, cv_image

def visualize_text_and_checkboxes(image, text_regions):
    """
    Draw bounding boxes around text and checkboxes.
    """
    annotated_image = image.copy()

    for text, box, field_name, checkbox_status, checked_checkboxes, all_checkboxes in text_regions:
        x, y, w, h = box

        cv2.rectangle(annotated_image, (x, y), (x + w, y + h), (255, 0, 0), 2)

        for checkbox in all_checkboxes:
            cx, cy, cw, ch = checkbox
            cv2.rectangle(annotated_image, (cx, cy), (cx + cw, cy + ch), (0, 255, 255), 2)

        if checked_checkboxes and checkbox_status:
            status_color = (0, 255, 0) if checkbox_status == "Yes" else (0, 0, 255)

            for checkbox in checked_checkboxes:
                cx, cy, cw, ch = checkbox
                cv2.rectangle(annotated_image, (cx, cy), (cx + cw, cy + ch), status_color, 2)

            label = f"{field_name}: {checkbox_status}"
            font_scale = 0.5
            font_thickness = 1
            font = cv2.FONT_HERSHEY_SIMPLEX
            (label_w, label_h), _ = cv2.getTextSize(label, font, font_scale, font_thickness)

            cv2.rectangle(annotated_image,
                          (x, y - label_h - 10),
                          (x + label_w + 10, y),
                          status_color, -1)

            cv2.putText(annotated_image, label,
                        (x + 5, y - 5),
                        font, font_scale, (255, 255, 255),
                        font_thickness)

    return annotated_image

def update_json_with_results(json_string, text_regions):
    """
    Update JSON string with checkbox detection results.
    """
    data = json.loads(json_string,strict=False)

    for text, box, field_name, checkbox_status, checked_checkboxes, _ in text_regions:
        if checkbox_status and field_name in data["fields"]:
            data["fields"][field_name] = {
                "value": checkbox_status,
                "bounds": f"{box[0]},{box[1]},{box[0]+box[2]},{box[1]+box[3]}"
            }

    return json.dumps(data, indent=3)

def process_document(input_dic):
    """
    Process document for text and checkbox visualization and JSON updating.
    """
    document_path = input_dic.get('document_path')
    field_names = input_dic.get('field_names')
    output_dir = input_dic.get('output_dir')
    json_string = input_dic.get('json_string', '')

    os.makedirs(output_dir, exist_ok=True)

    file_extension = Path(document_path).suffix.lower()
    all_text_regions = []
    annotated_image = None

    if file_extension == '.pdf':
        with tempfile.TemporaryDirectory() as temp_dir:
            doc = fitz.open(document_path)

            for page_num in range(len(doc)):
                page = doc[page_num]
                pix = page.get_pixmap(matrix=fitz.Matrix(300/72, 300/72))
                temp_image_path = os.path.join(temp_dir, f"page_{page_num + 1}.png")
                pix.save(temp_image_path)

                text_regions, image = extract_text_regions(temp_image_path, field_names)
                all_text_regions.extend(text_regions)

                if text_regions and image is not None:
                    annotated_image = visualize_text_and_checkboxes(image, text_regions)
                    output_path = os.path.join(output_dir, f"annotated_page_{page_num + 1}.png")
                    cv2.imwrite(output_path, annotated_image)

            doc.close()
    else:
        # Process single image
        text_regions, image = extract_text_regions(document_path, field_names)
        all_text_regions.extend(text_regions)

        if text_regions and image is not None:
            annotated_image = visualize_text_and_checkboxes(image, text_regions)
            output_path = os.path.join(output_dir, "annotated_image.png")
            cv2.imwrite(output_path, annotated_image)

    # Update JSON if provided
    updated_json = None
    if json_string:
        updated_json = update_json_with_results(json_string, all_text_regions)
    # if all_text_regions:
    #     print_all_detections_single_line(all_text_regions)

    return updated_json,annotated_image

if __name__ == "__main__":
    field_names = [
        "check cashing services",
        "issue or cash travelers checks",
        "offer prepaid cards",
        'provide money transmission or foreign exchange services',
        'issue or cash travelers checks or money orders',
        'Are you a smoker?',
        'Have you traveled abroad',
        'Do you own a car?',
        'Do you have pets?'
    ]

    json_string = '''{"pages":[{"width":2550,"height":3299},{"width":2550,"height":3299}],"fields":{"check cashing services":{"value":"","bounds":""},"Do you own a car?":{"value":"","bounds":"0,3299,-1,-1"},"Foreign exchange services":{"value":"","bounds":""},"Do you have pets?":{"value":"","bounds":""},"offer prepaid cards":{"value":"","bounds":""},"issue or cash travelers checks or money orders":{"value":"","bounds":""},"Have you traveled abroad":{"value":"","bounds":"0,3299,-1,-1"},"What is the company's estimated or projected annual revenue/budget (USD)? None and N/A are not allowed. If none, please indicate with $0":{"value":"$0","bounds":"0,0,-1,-1"},"provide money transmission or foreign exchange services":{"value":"","bounds":""}},"tables":{}}'''

    input_dic = {
        'field_names': field_names,
        'document_path': r'C:\Users\syed.hasnain\Downloads\KYC_Sample1.pdf',  # Use your actual path
        'output_dir': r'C:\Users\syed.hasnain\Downloads\Output',  # Use your actual output directory
        'json_string': json_string
    }

    updated_json,annotated_image = process_document(input_dic)
    # annotated_image = process_document(input_dic)

    print(updated_json)