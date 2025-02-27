import cv2
import numpy as np
import fitz
import re
import json
import os
import tempfile
from pathlib import Path
import traceback
import customLogger

# Set up logger
# logger = customLogger.aa_custom_logger(r'C:\Users\syed.hasnain\Downloads\log.txt', 'INFO')
# logger.info("Application started")
logger = None  # Has been initialized in process_document

def calculate_iou(box1, box2):
    """
    Calculate Intersection over Union (IoU) between two boxes.
    Each box should be in format (x, y, w, h)
    """
    try:
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
    except Exception as e:
        logger.error(f"Error calculating IoU: {str(e)}")
        return 0.0

def remove_overlapping_checkboxes(checkboxes, iou_threshold=0.3):
    """
    Remove overlapping checkboxes using IoU.
    """
    try:
        if not checkboxes:
            return []

        sorted_boxes = sorted(checkboxes, key=lambda x: x[2] * x[3], reverse=True)
        kept_boxes = []

        for current_box in sorted_boxes:
            should_keep = True
            for kept_box in kept_boxes:
                iou = calculate_iou(current_box[:4], kept_box[:4])
                if iou > iou_threshold:
                    should_keep = False
                    break
            if should_keep:
                kept_boxes.append(current_box)

        logger.debug(f"Removed {len(checkboxes) - len(kept_boxes)} overlapping checkboxes")
        return kept_boxes
    except Exception as e:
        logger.error(f"Error removing overlapping checkboxes: {str(e)}")
        return []

def is_checkbox_checked(checkbox_region,x,y):
    """
    Determine if a checkbox is checked using multiple criteria.
    """
    try:
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

        if 0.10 <= filled_ratio <= 0.50 and center_ratio > 0.10:
            return True
    except Exception as e:
        logger.error(f"Error recognizing checkboxes: {str(e)}")
        return None
    # else:
    #     print(x,y,filled_ratio,center_ratio)
    #     return False

def extract_text_regions(image_path, field_names, ocr_data,checkbox_values):
    """
    Extract text regions and detect checkboxes using provided OCR data.
    """
    try:
        logger.info(f"Extracting text regions from {image_path}")
        if not os.path.exists(image_path):
            logger.error(f"Image path does not exist: {image_path}")
            return [], None

        cv_image = cv2.imread(image_path)
        if cv_image is None:
            logger.error(f"Failed to load image with OpenCV: {image_path}")
            return [], None

        structured_ocr = convert_ocr_blocks_to_structured_data(ocr_data['blocks'])
        text_regions = []
        n_boxes = len(structured_ocr['text'])
        values_part = checkbox_values.split("{")[1].split("}")[0].strip()
        checkbox_values = []
        for item in values_part.split(","):
            # Clean each value by removing quotes and extra whitespace
            clean_item = item.strip().strip('"\'')
            if clean_item:  # Only add non-empty items
                checkbox_values.append(clean_item)
        # print(checkbox_values)


        for i in range(n_boxes):
            text = structured_ocr['text'][i].lower()

            x = structured_ocr['left'][i]
            y = structured_ocr['top'][i]
            w = structured_ocr['width'][i]
            h = structured_ocr['height'][i]
            line_num = structured_ocr['line_num'][i]

            for field_name in field_names:
                # Change this in extract_text_regions
                field_pattern = r'\b' + re.escape(field_name.lower()) + r'\b'
                if re.search(field_pattern, text) or field_name.lower() in text:
                    # ...
                    box = (x, y, w, h)
                    text_regions.append((text, box, field_name, None, [], line_num))
            # print(text_regions)

        checkboxes = detect_checkboxes(cv_image, text_regions)

        final_text_regions = []
        for text, box, field_name, _, _, line_num in text_regions:
            field_y = box[1] + box[3]/2
            associated_checkboxes = []
            all_checkboxes_in_line = []

            # First, find all checkboxes in this line
            for checkbox in checkboxes:
                checkbox_x, checkbox_y, checkbox_w, checkbox_h, is_checked = checkbox
                checkbox_y_center = checkbox_y + checkbox_h/2
                vertical_distance = abs(checkbox_y_center - field_y)

                if vertical_distance < max(30, box[3]):
                    all_checkboxes_in_line.append((checkbox_x, checkbox_y, checkbox_w, checkbox_h))
                    if is_checked:
                        associated_checkboxes.append((checkbox_x, checkbox_y, checkbox_w, checkbox_h))

            checkbox_status = None
            if associated_checkboxes:
                # Sort checkboxes by x-coordinate
                associated_checkboxes.sort(key=lambda x: x[0])
                checked_x = associated_checkboxes[0][0]
                checked_y = associated_checkboxes[0][1]

                # Find all text blocks that are on the same line and to the right of the checkbox
                potential_values = []
                for block in ocr_data['blocks']:
                    block_x = block['geometry']['x1']
                    block_y = block['geometry']['y1']
                    block_text = block['text'].lower().strip()

                    # Check if block is on same line (within vertical threshold)
                    vertical_dist = abs(block_y - checked_y)

                    # Only consider blocks that are to the right of the checkbox
                    if vertical_dist < 30 and block_x > checked_x:
                        # Store the block with its distance from checkbox
                        horizontal_dist = block_x - checked_x
                        # print(block['text'],block_x,checked_x,horizontal_dist)
                        potential_values.append((block_text, horizontal_dist))

                # Sort potential values by horizontal distance
                potential_values.sort(key=lambda x: x[1])  # Sort by distance
                # print(potential_values)
                # print('checkboxvalues',checkbox_values)
                # Find the closest valid value
                # This should
                # for value, _ in potential_values:
                #     if value in checkbox_values:
                #         checkbox_status = value
                #         # print(str(checkbox_values[value]))
                #         break
                #     elif value in checkbox_values:
                #         checkbox_status = value
                #         print(checkbox_values[value])
                #         break
                for value, _ in potential_values:
                    if value in checkbox_values:
                        checkbox_status = value
                        break
            final_text_regions.append((text, box, field_name, checkbox_status, associated_checkboxes, all_checkboxes_in_line))

        logger.info(f"Extracted {len(final_text_regions)} text regions with field names")
        # print(final_text_regions)
        return final_text_regions, cv_image

    except Exception as e:
        logger.error(f"Error extracting text regions: {str(e)}\n{traceback.format_exc()}")
        return [], None



def convert_ocr_blocks_to_structured_data(ocr_blocks):
    """
    Convert OCR blocks to a structured format for processing.
    First processes LINE blocks, then falls back to WORD blocks if needed.
    Filters out blocks containing 'yes', 'no', or similar affirmations/negations.
    """
    try:
        # Define terms to filter out
        filter_terms = ["yes", "no", "y", "n", "true", "false",'own','rent']

        # Separate LINE and WORD blocks
        line_blocks = [block for block in ocr_blocks if block['blockType'] == 'LINE']
        word_blocks = [block for block in ocr_blocks if block['blockType'] == 'WORD']

        # If no blocks found, return empty structure
        if not line_blocks and not word_blocks:
            return {
                'text': [], 'left': [], 'top': [],
                'width': [], 'height': [], 'conf': [], 'line_num': []
            }

        # Process LINE blocks first (they're already grouped)
        lines = []
        processed_regions = []

        # Sort line blocks by vertical position
        sorted_line_blocks = sorted(line_blocks, key=lambda x: x['geometry']['y1'])

        # Filter LINE blocks and use them directly
        for block in sorted_line_blocks:
            text = block['text'].strip()

            # Skip this line if it contains any of the filter terms
            # In convert_ocr_blocks_to_structured_data
            should_skip = False
            for term in filter_terms:
                if re.search(r'\b' + re.escape(term) + r'\b', text.lower()):
                    should_skip = True
                    break

            if not should_skip:
                lines.append([block])
                # Track the region this LINE block covers
                processed_regions.append((
                    block['geometry']['y1'] - 5,  # Add a small buffer
                    block['geometry']['y2'] + 5   # Add a small buffer
                ))

        # Now process WORD blocks for areas not covered by LINE blocks
        remaining_words = []
        for block in word_blocks:
            word_y1 = block['geometry']['y1']
            word_y2 = block['geometry']['y2']

            # Check if this word is already covered by a LINE block
            is_covered = False
            for y1, y2 in processed_regions:
                if word_y1 >= y1 and word_y2 <= y2:
                    is_covered = True
                    break

            if not is_covered:
                # Skip words that match filter terms
                text = block['text'].lower().strip()
                if text not in filter_terms:
                    remaining_words.append(block)

        # Group remaining words into lines based on vertical position
        if remaining_words:
            vertical_threshold = 10  # pixels tolerance for same line
            current_line = []

            # Sort words by vertical position first, then horizontal
            sorted_words = sorted(
                remaining_words,
                key=lambda x: (x['geometry']['y1'], x['geometry']['x1'])
            )

            current_y = sorted_words[0]['geometry']['y1']

            for block in sorted_words:
                block_y = block['geometry']['y1']

                # If this block is on a new line
                if abs(block_y - current_y) > vertical_threshold:
                    if current_line:
                        # Sort current line by x position and add to lines
                        current_line.sort(key=lambda x: x['geometry']['x1'])
                        # Check if this word line contains filter terms
                        line_text = ' '.join([b['text'].strip() for b in current_line]).lower()
                        should_skip = any(term in line_text.split() for term in filter_terms)
                        if not should_skip:
                            lines.append(current_line)
                    current_line = [block]
                    current_y = block_y
                else:
                    current_line.append(block)

            # Add the last line
            if current_line:
                current_line.sort(key=lambda x: x['geometry']['x1'])
                line_text = ' '.join([b['text'].strip() for b in current_line]).lower()
                should_skip = any(term in line_text.split() for term in filter_terms)
                if not should_skip:
                    lines.append(current_line)

        # Sort all lines by vertical position
        lines.sort(key=lambda line: line[0]['geometry']['y1'])

        # Convert grouped lines to structured format
        structured_data = {
            'text': [],
            'left': [],
            'top': [],
            'width': [],
            'height': [],
            'conf': [],
            'line_num': []
        }

        for line_num, line in enumerate(lines):
            # For each line, combine the words if needed
            if len(line) == 1 and line[0]['blockType'] == 'LINE':
                # Single LINE block
                block = line[0]
                text = block['text'].strip()
            else:
                # Multiple WORD blocks - join them
                text_parts = [block['text'].strip() for block in line]
                text = ' '.join(text_parts)

            # Get geometry from first and last block in line
            first_block = line[0]
            last_block = line[-1]

            # Calculate combined line geometry
            left = first_block['geometry']['x1']
            top = min(b['geometry']['y1'] for b in line)
            right = last_block['geometry']['x2']
            bottom = max(b['geometry']['y2'] for b in line)
            width = right - left
            height = bottom - top

            # Calculate average confidence for the line
            conf = sum(float(b['confidence']) for b in line) / len(line)

            structured_data['text'].append(text)
            structured_data['left'].append(left)
            structured_data['top'].append(top)
            structured_data['width'].append(width)
            structured_data['height'].append(height)
            structured_data['conf'].append(conf * 100)
            structured_data['line_num'].append(line_num)

        return structured_data
    except Exception as e:
        logger.error(f"Error recognizing checkboxes: {str(e)}")
        return None

def detect_checkboxes(image, text_regions):
    """
    Detect checkboxes using text height as reference and limiting to only those after the text regions.
    Steps:
    1. Find all potential checkbox contours in the document
    2. Filter checkboxes to only those within vertical range (20% buffer) of text regions
    3. Further filter to only include checkboxes that appear after the end (x2) of text regions
    4. Apply convex hull and solidity checks
    5. Check checkbox status
    """
    try:
        logger.info("Starting checkbox detection process")
        if image is None:
            logger.error("Cannot detect checkboxes: Image is None")
            return []

        if len(image.shape) == 3:
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            logger.info("Converted image to grayscale")
        else:
            gray = image.copy()
            logger.info("Image already in grayscale format")

        logger.info("Applying Gaussian blur and adaptive thresholding")
        blurred = cv2.GaussianBlur(gray, (3, 3), 0)
        thresh = cv2.adaptiveThreshold(
            blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
            cv2.THRESH_BINARY_INV, 11, 2
        )

        kernel = np.ones((2,2), np.uint8)
        thresh = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)
        logger.info("Applied morphological operations to threshold image")

        # Step 1: Find all potential checkbox contours in the document
        contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
        logger.info(f"Found {len(contours)} potential contours in the image")

        # Get text region height and positions for filtering
        heights = [box[3] for text, box, field_name, checkbox_status, checkboxes, line_num in text_regions]
        logger.info(f"Processing {len(text_regions)} text regions with heights: {heights}")
        # print(text_regions)
        reference_height = np.median(heights) if heights else 40
        logger.info(f"Using reference height for checkboxes: {reference_height}")

        min_height = reference_height * 0.7
        max_height = reference_height * 1.3
        logger.info(f"Checkbox height range: {min_height} to {max_height} pixels")

        # Step 2: Calculate vertical and horizontal ranges for each text region
        position_ranges = []
        for i, (_, box, field_name, _, _, _) in enumerate(text_regions):
            x = box[0]
            y = box[1]
            w = box[2]
            h = box[3]

            # Calculate vertical range with 20% buffer
            buffer_v = h * 0.2  # 20% vertical buffer
            y1 = max(0, y - buffer_v)
            y2 = min(thresh.shape[0], y + h + buffer_v)

            # Calculate the end of text region (x2) - checkboxes should appear after this
            text_end_x = x + w

            position_ranges.append((text_end_x, y1, y2))
            logger.info(f"Text region {i} ({field_name}): end_x={text_end_x}, y_range={y1}-{y2}")

        # Combine all the potential checkbox candidates
        checkboxes = []
        filtered_count = {
            "height": 0,
            "aspect_ratio": 0,
            "position": 0,
            "convexity": 0,
            "final": 0
        }

        logger.info("Starting contour filtering for checkbox detection")
        for contour in contours:
            x, y, w, h = cv2.boundingRect(contour)

            # Only process if height is within reasonable range for a checkbox
            if not (min_height <= h <= max_height):
                filtered_count["height"] += 1
                continue

            # Check aspect ratio
            aspect_ratio = w / float(h) if h > 0 else 0
            if not (0.8 <= aspect_ratio <= 1.4):
                filtered_count["aspect_ratio"] += 1
                continue

            # Step 3: Check if the checkbox is after text region and within vertical range
            is_in_range = False
            for text_end_x, text_y1, text_y2 in position_ranges:
                # Check if checkbox is within vertical range
                checkbox_center_y = y + h/2

                # Check vertical alignment (with buffer)
                vertical_match = text_y1 <= checkbox_center_y <= text_y2

                # Checkbox should start after the end of text region
                horizontal_match = x > text_end_x

                if vertical_match and horizontal_match:
                    is_in_range = True
                    logger.info(f"Potential checkbox at ({x},{y}) matches position criteria")
                    break

            if not is_in_range:
                filtered_count["position"] += 1
                continue

            # Step 4: Apply convex hull and solidity checks
            hull = cv2.convexHull(contour)
            hull_area = cv2.contourArea(hull)
            contour_area = cv2.contourArea(contour)
            solidity = contour_area / hull_area if hull_area > 0 else 0

            logger.info(f"Contour at ({x},{y}) has solidity: {solidity:.2f}")

            if cv2.isContourConvex(hull) and solidity > 0.7:
                # Step 5: Check checkbox status
                padding = 2
                y1 = max(0, y - padding)
                y2 = min(thresh.shape[0], y + h + padding)
                x1 = max(0, x - padding)
                x2 = min(thresh.shape[1], x + w + padding)

                if y2 <= y1 or x2 <= x1:
                    filtered_count["final"] += 1
                    continue

                checkbox_region = thresh[y1:y2, x1:x2]
                is_checked = is_checkbox_checked(checkbox_region, x1, y1)
                checkboxes.append((x, y, w, h, is_checked))
                logger.info(f"Found checkbox at ({x},{y}) with size ({w}x{h}), checked: {is_checked}")
            else:
                filtered_count["convexity"] += 1

        result = remove_overlapping_checkboxes(checkboxes)
        logger.info(f"Detected {len(result)} checkboxes after removing overlaps")
        logger.info(f"Filtered out: {filtered_count['height']} by height, {filtered_count['aspect_ratio']} by aspect ratio, {filtered_count['position']} by position, {filtered_count['convexity']} by convexity, {filtered_count['final']} by final checks")
        return result
    except Exception as e:
        logger.error(f"Error detecting checkboxes: {str(e)}")
        return []


def visualize_text_and_checkboxes(image, text_regions):
    """
    Draw bounding boxes around text and checkboxes, with clear indication of checked status
    """
    try:
        if image is None:
            logger.error("Cannot visualize: Image is None")
            return None

        annotated_image = image.copy()

        for text, box, field_name, checkbox_status, checked_checkboxes, all_checkboxes in text_regions:
            x, y, w, h = box

            # Draw blue box around text
            cv2.rectangle(annotated_image, (x, y), (x + w, y + h), (255, 0, 0), 2)

            # Draw all checkboxes in yellow
            for checkbox in all_checkboxes:
                cx, cy, cw, ch = checkbox
                # Draw yellow box for unchecked
                cv2.rectangle(annotated_image, (cx, cy), (cx + cw, cy + ch), (0, 255, 255), 2)
                # Add "X" for unchecked boxes
                cv2.putText(annotated_image, "X",
                            (cx + cw//4, cy + ch*3//4),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 255), 2)

            # Overlay checked checkboxes in green with ✓
            if checked_checkboxes:
                for checkbox in checked_checkboxes:
                    cx, cy, cw, ch = checkbox
                    # Draw green box for checked
                    cv2.rectangle(annotated_image, (cx, cy), (cx + cw, cy + ch), (0, 255, 0), 2)
                    # Add checkmark for checked boxes
                    cv2.putText(annotated_image, "✓",
                                (cx + cw//4, cy + ch*3//4),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

            # Add label with field name and status
            if checkbox_status:
                status_color = (0, 255, 0) if checkbox_status == "Yes" else (0, 0, 255)
                label = f"{field_name}: {checkbox_status}"
                font_scale = 0.5
                font_thickness = 1
                font = cv2.FONT_HERSHEY_SIMPLEX
                (label_w, label_h), _ = cv2.getTextSize(label, font, font_scale, font_thickness)

                # Draw background rectangle for text
                cv2.rectangle(annotated_image,
                              (x, y - label_h - 10),
                              (x + label_w + 10, y),
                              status_color, -1)

                # Draw text
                cv2.putText(annotated_image, label,
                            (x + 5, y - 5),
                            font, font_scale, (255, 255, 255),
                            font_thickness)

        logger.info("Successfully visualized text regions and checkboxes")
        return annotated_image
    except Exception as e:
        logger.error(f"Error visualizing text and checkboxes: {str(e)}")
        return None

def update_json_with_results(json_string, text_regions):
    """
    Final step that updates the JSON data that we get from 'Get data: Document Extraction package.
    Records which checkboxes were checked for each field
    Includes precise position information for each result.
    Formats everything for downstream storage visible on validation page
    """
    try:
        logger.info("Updating JSON with detection results")
        data = json.loads(json_string, strict=False)

        for text, box, field_name, checkbox_status, checked_checkboxes, _ in text_regions:
            if checkbox_status and field_name in data["fields"]:
                data["fields"][field_name] = {
                    "value": checkbox_status,
                    "bounds": f"{box[0]},{box[1]},{box[0]+box[2]},{box[1]+box[3]}"
                }
                logger.debug(f"Updated JSON field '{field_name}' with value '{checkbox_status}'")

        return json.dumps(data, indent=3)
    except json.JSONDecodeError as e:
        logger.error(f"JSON decode error: {str(e)}")
        return json_string
    except Exception as e:
        logger.error(f"Error updating JSON: {str(e)}")
        return json_string

def load_ocr_data(ocr_data_path):
    """
    Load OCR data from a JSON file with explicit encoding handling.
    OCR data is the engine that is returned from the extraction package of
    Document Extraction package.
    """
    try:
        logger.info(f"Loading OCR data from {ocr_data_path}")
        if not os.path.exists(ocr_data_path):
            logger.error(f"OCR data file does not exist: {ocr_data_path}")
            return None

        with open(ocr_data_path, 'r', encoding='utf-8') as f:  # Explicit encoding
            ocr_data = json.load(f)

        logger.info("Successfully loaded OCR data")
        return ocr_data

    except UnicodeDecodeError as e:
        logger.error(f"Encoding issue while reading OCR data: {str(e)}")
        return None
    except json.JSONDecodeError as e:
        logger.error(f"Error decoding OCR data JSON: {str(e)}")
        return None
    except Exception as e:
        logger.error(f"Error loading OCR data: {str(e)}")
        return None

def process_document(input_dic):
    """
    Process document using OCR data loaded from file
    this is the entry point of the process.
    Entry point for the entire process.
    Takes inputs like document path, field names, and OCR data(engine data).
    Coordinates all other functions in the proper sequence.
    Handles both PDF and image files differently
    """
    try:
        global logger
        log_path = input_dic.get('log_path')
        logger = customLogger.aa_custom_logger(log_path, 'INFO')
        logger.info("Starting document processing")
        logger.info("Starting document processing")

        if not input_dic:
            logger.error("Input dictionary is empty or None")
            return None, None

        document_path = input_dic.get('document_path')
        ocr_data_path = input_dic.get('ocr_data')

        if not document_path or not os.path.exists(document_path):
            logger.error(f"Document path is invalid or does not exist: {document_path}")
            return None, None

        # Load OCR data from file
        ocr_data = load_ocr_data(ocr_data_path)
        if not ocr_data:
            logger.error("Failed to load OCR data from file")
            return None, None

        field_names = input_dic.get('field_names', [])
        output_dir = input_dic.get('output_dir', os.getcwd())
        json_string = input_dic.get('json_string', '')

        # Get checkbox values from input dictionary if provided
        checkbox_values = input_dic.get('checkbox_values')

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

                    # Filter OCR data for current page
                    page_ocr = {
                        'blocks': [
                            block for block in ocr_data['engineData']['ocrResult']['blocks']
                            if block.get('pageNum') == page_num + 1
                        ]
                    }

                    text_regions, image = extract_text_regions(temp_image_path, field_names, page_ocr,checkbox_values)
                    all_text_regions.extend(text_regions)

                    if text_regions and image is not None:
                        annotated_image = visualize_text_and_checkboxes(image, text_regions)
                        output_path = os.path.join(output_dir, f"annotated_page_{page_num + 1}.png")
                        cv2.imwrite(output_path, annotated_image)

                doc.close()
        else:
            text_regions, image = extract_text_regions(document_path, field_names, ocr_data,checkbox_values)
            all_text_regions.extend(text_regions)

            if text_regions and image is not None:
                annotated_image = visualize_text_and_checkboxes(image, text_regions)
                output_path = os.path.join(output_dir, "annotated_image.png")
                cv2.imwrite(output_path, annotated_image)

        # Update JSON if provided
        updated_json = None
        if json_string:
            updated_json = update_json_with_results(json_string, all_text_regions)

        logger.info("Document processing completed successfully")
        return updated_json
        # , annotated_image

    except Exception as e:
        logger.error(f"Unhandled exception in process_document: {str(e)}\n{traceback.format_exc()}")
        return None, None

# if __name__ == "__main__":
#     try:
#         field_names = [
#             # "check cashing services",
#             # "issue or cash travelers checks",
#             # "offer prepaid cards",
#             # 'provide money transmission or foreign exchange services',
#             # 'issue or cash travelers checks or money orders',
#             # 'Are you a smoker',
#             # 'Have you traveled abroad',
#             # 'Do you own a car',
#             # 'Do you have pets',
#             'raw material complies with the relevant sustainability',
#             'agricultural biomass was cultivated',
#             'Total default value according to RED',
#             'The agricultural biomass additionally fulfills the measures for low',
#             'The raw material meets the definition of waste or residue',
#             'Were incentives subsidies received for the production',
#             'Is the highest and best use of subject property as improved'
#             'Community lending',
#             # 'Affordable Housing',
#             'Home Buyers Homeownership Education Certificate in file',
#             # 'Are there any physical',
#             # 'Are the utilities and',
#             'Former Address',
#             'Present Address'
#         ]
#         json_string = '''{"pages":[{"width":2550,"height":3299},{"width":2550,"height":3299}],"fields":{"check cashing services":{"value":"","bounds":""},"Do you own a car?":{"value":"","bounds":"0,3299,-1,-1"},"Foreign exchange services":{"value":"","bounds":""},"Do you have pets?":{"value":"","bounds":""},"offer prepaid cards":{"value":"","bounds":""},"issue or cash travelers checks or money orders":{"value":"","bounds":""},"Have you traveled abroad":{"value":"","bounds":"0,3299,-1,-1"},"What is the company's estimated or projected annual revenue/budget (USD)? None and N/A are not allowed. If none, please indicate with $0":{"value":"$0","bounds":"0,0,-1,-1"},"provide money transmission or foreign exchange services":{"value":"","bounds":""}},"tables":{}}'''
#
#
#         custom_checkbox_values = '''{
#                 "yes", "y", "☑yes", "own", "definitely", "correct","no", "n", "No", "☑No", "☑no", "rent", "negative", "incorrect"
#             }'''
#         input_dic = {
#             'field_names': field_names,
#             'document_path': r'C:\Users\syed.hasnain\Downloads\Sample 3.pdf',
#             'output_dir': r'C:\Users\syed.hasnain\Downloads\Output',
#             'json_string': json_string,
#             'ocr_data': r'C:\ProgramData\AutomationAnywhere\My_venv\26EDE37C-F30C-44B6-980C-D46D0779C53F_Sample 3.json'
#             ,'checkbox_values':custom_checkbox_values,
#             'log_path':r'C:\Users\syed.hasnain\Downloads\log.txt'
#         }
#
#         # logger.info("Starting main execution")
#         updated_json = process_document(input_dic)
#
#         if updated_json:
#             # logger.info("Successfully generated updated JSON")
#             print(updated_json)
#
#
#     except Exception as e:
#         # logger.critical(f"Critical error in main execution: {str(e)}\n{traceback.format_exc()}")
#         print(str(e))

    #
