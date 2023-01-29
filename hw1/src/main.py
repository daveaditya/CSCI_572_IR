import logging
import re

from search_engine import SearchEngine
from utils import *

logging.basicConfig(format="%(asctime)-15s [%(levelname)s] %(funcName)s: %(message)s", level=logging.INFO)
logger = logging.getLogger()

SEARCH_ENGINE_URL = "https://www.ask.com/web"
SEARCH_RESULT_SELECTOR = "PartialSearchResults-item-title-link result-link"
RESULT_LIMIT = 10

QUERIES_SET_PATH = "./../data/100QueriesSet3.txt"
RESULTS_OUTPUT_FILE_PATH = "./../submission/hw1.json"
CHECKPOINT_FILE_PATH = "./../checkpoint.txt"
TOTAL_QUERIES = 100


def can_search_further_func(soup, result_limit: int):
    prog = re.compile(r"\d+")

    # check if result_limit number of results can be obtained
    logger.info("Looking for result count for current search")
    results_count = (
        soup.find("div", attrs={"class": "PartialResultsHeader-summary"}).contents[0].strip().replace(",", "")
    )
    logger.info(f"Result Count Summary: {results_count}")

    result_counts = prog.findall(results_count)

    should_search_further: bool = True
    if result_counts:
        counts = [int(count) for count in result_counts]
        if counts[1] <= result_limit and counts[2] < result_limit:
            should_search_further = False

    logger.info(f"Should search further?: {should_search_further}")
    return should_search_further


def main():
    # load queries from checkout
    checkpoint = read_checkpoint(CHECKPOINT_FILE_PATH)
    logger.info(f"Checkpoint Query: {checkpoint + 1}")

    logger.info("Retrieving queries")
    queries = read_queries(QUERIES_SET_PATH, from_line=checkpoint)
    logger.info(f"Retrieved query count: {len(queries)}")

    # start search engine crawling
    for query in queries:
        logger.info(f"Query No #{checkpoint + 1}; Query: {query}")

        # perform search
        results = SearchEngine.search(
            SEARCH_ENGINE_URL, query, SEARCH_RESULT_SELECTOR, can_search_further_func, RESULT_LIMIT, should_sleep=True
        )

        # store result
        save_results(RESULTS_OUTPUT_FILE_PATH, {query: results})
        logger.info(f"Results Saved: {len(results)}")

        # update and write checkpoint
        checkpoint += 1
        save_checkpoint(CHECKPOINT_FILE_PATH, checkpoint)
        logger.info("Checkpoint saved")

        break


if __name__ == "__main__":
    main()
