import logging
import os
import pathlib

# Define log file path
scrptLib=str(pathlib.Path(__file__).parent.resolve())
pathLib = str(pathlib.Path().resolve())
LOG_DIR = scrptLib +"/Logs" # Change this to your desired path inside the script working directory
#LOG_DIR = pathLib
LOG_FILE = os.path.join(LOG_DIR, "app.log")

# Ensure log directory exists
os.makedirs(LOG_DIR, exist_ok=True)

def aa_custom_logger1(filePath, logLevel):
    # Configure logging
    if (logLevel == "INFO"):
        levl = logging.INFO
    elif (logLevel == "DEBUG"):
        levl = logging.DEBUG
    else:
        levl = logging.INFO

    logging.basicConfig(level=levl,  # Set logging level (DEBUG, INFO, WARNING, ERROR, CRITICAL)
    format="%(asctime)s - %(levelname)s - %(module)s - %(message)s",
    handlers=[
           #logging.FileHandler(LOG_FILE, mode="a"),  # Log to file
            logging.FileHandler(filePath, mode="a"),  # Log to file
            logging.StreamHandler()  # Log to console
        ]
    )

    # Create a reusable logger instance
    logger = logging.getLogger(__name__)
    return logger

def aa_custom_logger(log_file_path, log_level):
    logger = logging.getLogger(__name__)
    logger.setLevel(getattr(logging, log_level.upper()))
    file_handler = logging.FileHandler(log_file_path, mode='a')
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(name)s (%(message)s', datefmt='(%d-%m-%Y %I.%M.%S %p)')

    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    return logger