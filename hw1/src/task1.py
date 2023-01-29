import argparse
import logging
import re

from search_engine import SearchEngine
from utils import *

################################################################################################
### Config
################################################################################################
logging.basicConfig(format="%(asctime)-15s [%(levelname)s] %(funcName)s: %(message)s", level=logging.INFO)
logger = logging.getLogger()


################################################################################################
### Constants
################################################################################################
SEARCH_ENGINE_URL = "https://www.ask.com/web"
SEARCH_RESULT_SELECTOR = "PartialSearchResults-item-title-link result-link"
RESULT_LIMIT = 10

QUERIES_SET_PATH = "./../data/100QueriesSet3.txt"
RESULTS_OUTPUT_FILE_PATH = "./../submission/hw1.json"
CHECKPOINT_FILE_PATH = "./checkpoint.txt"
TOTAL_N_OF_QUERIES = 100


################################################################################################
### Support Functions
################################################################################################
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


################################################################################################
### Main Program
################################################################################################
def main(
    queries_set_file_path: str,
    results_output_file_path: str,
    n_queries_to_search: int,
    total_n_of_queries: int,
    save_batch_size: int,
    use_checkpoint: bool,
    result_limit: int,
):
    checkpoint = 0
    if use_checkpoint:
        # load queries from checkout
        checkpoint = read_checkpoint(CHECKPOINT_FILE_PATH)
        logger.info(f"Checkpoint Query: {checkpoint + 1}")

    logger.info("Retrieving queries")
    queries = read_queries(queries_set_file_path, from_line=checkpoint)
    logger.info(f"Retrieved query count: {len(queries)}")

    queries = queries[:n_queries_to_search]
    logger.info(f"# of queries to search: {n_queries_to_search}")

    count = 1

    logger.info(f"Total # of Queries: {total_n_of_queries}")
    logger.info("Starting Search ...")

    batched_results = dict()

    # start search engine crawling
    for query in queries:
        logger.info(f"Query #{checkpoint + 1}; Query: {query}")

        # perform search
        try:
            results = SearchEngine.search(
                SEARCH_ENGINE_URL,
                query,
                SEARCH_RESULT_SELECTOR,
                can_search_further_func=can_search_further_func,
                result_limit=result_limit,
                should_sleep=True,
            )

            batched_results[query] = results
        except:
            logger.info("An error occurred while searching. Saving the unsaved work and exiting")

            if len(batched_results) > 0:
                logger.info(f"Saving the last batch which might not be full.")

                # store result
                save_results(results_output_file_path, batched_results)
                logger.info(f"Batched Results Saved: {len(batched_results)}")

                if use_checkpoint:
                     # write checkpoint
                    save_checkpoint(CHECKPOINT_FILE_PATH, checkpoint)
                    logger.info("Checkpoint saved")

            exit(1)

        # query is processed, hence checkpoint increases by 1
        checkpoint += 1

        if len(batched_results) % save_batch_size == 0:
            logger.info(f"Batch filled, now saving.")

            # store result
            save_results(results_output_file_path, batched_results)
            logger.info(f"Batched Results Saved: {len(batched_results)}")

            if use_checkpoint:
                # write checkpoint
                save_checkpoint(CHECKPOINT_FILE_PATH, checkpoint)
                logger.info("Checkpoint saved")

            # empty batch to fill again
            batched_results = dict()

        if count == n_queries_to_search:
            if len(batched_results) > 0:
                logger.info(f"Saving the last batch which might not be full.")

                # store result
                save_results(results_output_file_path, batched_results)
                logger.info(f"Batched Results Saved: {len(batched_results)}")

                if use_checkpoint:
                    # write checkpoint
                    save_checkpoint(CHECKPOINT_FILE_PATH, checkpoint)
                    logger.info("Checkpoint saved")

            logger.info(f"Successfully completed: {n_queries_to_search} searches")
            break

        count += 1

    logger.info(f"Search Completed Successfully!! for {count} queries")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="Ask.com Search Crawler",
        description="This program takes in queries, performs search on ask.com using those queries and store the results in JSON format",
        epilog="Information Retrieval",
    )
    parser.add_argument("--queries_set_file", type=str, default=QUERIES_SET_PATH)
    parser.add_argument("--results_output_file", type=str, default=RESULTS_OUTPUT_FILE_PATH)
    parser.add_argument("--n_queries_to_search", type=int, default=10)
    parser.add_argument("--total_n_of_queries", type=int, default=TOTAL_N_OF_QUERIES)
    parser.add_argument("--save_batch_size", type=int, default=5)
    parser.add_argument("--use_checkpoint", type=bool, default=True)
    parser.add_argument("--result_limit", type=int, default=10)

    args = parser.parse_args()
    main(
        queries_set_file_path=args.queries_set_file,
        results_output_file_path=args.results_output_file,
        n_queries_to_search=args.n_queries_to_search,
        total_n_of_queries=args.total_n_of_queries,
        save_batch_size=args.save_batch_size,
        use_checkpoint=args.use_checkpoint,
        result_limit=args.result_limit,
    )
