from collections import OrderedDict
import json
import logging
from os.path import exists

logger = logging.getLogger()

def read_queries(file_path: str, from_line: int = 0):
    queries = list()
    logger.info(f"Opening file: {file_path}")
    with open(file_path, mode="r") as file:  # open file for reading
        logger.info("Reading Queries")
        for idx, query in enumerate(file):  # loop over every line and start storing from the specified line
            if idx >= from_line:
                queries.append(query.strip())
    logger.info(f"Queries Read: {len(queries)}")
    return queries


def save_results(file_path: str, results):
    logger.info(f"Opening file {file_path} in r+ mode")

    if not exists(file_path):
        logger.info(f"Creating {file_path} file")
        with open(file_path, mode="a+") as file:
            pass

    with open(file_path, mode="r+") as file:
        updated_results = dict()
        try:
            # set the file cursor to start
            file.seek(0)

            # read existing data, if any
            logger.info("Loading existing results")
            updated_results = json.load(file)
        except json.JSONDecodeError:
            logger.error("Cannot read results file.")
            pass

        logger.info(f"Existing # of Results: {len(updated_results)}")

        # add new results
        logger.info("Add new results")
        updated_results.update(results)

        logger.info(f"Store the updated results: {len(updated_results)}")
        # set the file cursor to start
        file.seek(0)
        json.dump(updated_results, file, ensure_ascii=False, indent=4)


def read_checkpoint(file_path: str):
    try:
        with open(file_path, mode="r") as file:
            return json.load(file)
    except FileNotFoundError:
        return 0


def save_checkpoint(file_path: str, query_idx: int):
    with open(file_path, mode="w") as file:
        json.dump(query_idx, file, ensure_ascii=False, indent=4)
