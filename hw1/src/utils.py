import json
import logging
from math import pow
from os.path import exists
from typing import List, Tuple


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


def load_results(file_path: str):
    # Throw Exception if the file is not present
    if not exists(file_path):
        raise FileNotFoundError(f"Cannot find results file: {file_path}")

    # Else load and return JSON
    logger.info(f"Opening file {file_path}")
    with open(file_path, mode="r") as file:
        return json.load(file)


def save_results(file_path: str, results):
    logger.info(f"Opening file {file_path} in r+ mode")

    is_new_file = False
    if not exists(file_path):
        logger.info(f"Creating {file_path} file")
        with open(file_path, mode="a+") as file:
            pass
        is_new_file = True

    with open(file_path, mode="r+") as file:
        updated_results = dict()
        try:
            if not is_new_file:
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
        updated_results = {**updated_results, **results}

        logger.info(f"Store the updated results: {len(updated_results)}")
        # set the file cursor to start
        file.seek(0)
        json.dump(updated_results, file, ensure_ascii=False, indent=4)


def read_checkpoint(file_path: str):

    if not exists(file_path):
        logger.info(f"Creating {file_path} file")
        with open(file_path, mode="a+") as file:
            json.dump(0, file, ensure_ascii=False, indent=4)

    try:
        with open(file_path, mode="r") as file:
            return json.load(file)
    except FileNotFoundError:
        return 0


def save_checkpoint(file_path: str, query_idx: int):
    with open(file_path, mode="w") as file:
        json.dump(query_idx, file, ensure_ascii=False, indent=4)


def url_sanitizer(urls: List[str]) -> List[str]:
    # takes a list of URLs and returns processed list
    normalized_urls = list()
    for url in urls:
        url = url.replace("www.", "")
        url = url.replace("https://", "http://")
        url = url.strip("/")
        normalized_urls.append(url)

    return normalized_urls


def power(nums: List[int], y: int):
    return list(map(lambda x: pow(x, y), nums))


def spearmans_rho(diff: List[Tuple[int, int]]):
    overlap = len(diff)
    if overlap == 0:
        rho = 0
    elif overlap == 1:
        if sum(diff) == 0:
            rho = 1
        else:
            rho = 0
    else:
        rho = 1 - ((6 * sum(power(diff, 2))) / (overlap * (pow(overlap, 2) - 1)))
    return overlap, rho
