import argparse
import csv
import logging

from utils import load_results, spearmans_rho, url_sanitizer

################################################################################################
### Config
################################################################################################
logging.basicConfig(format="%(asctime)-15s [%(levelname)s] %(funcName)s: %(message)s", level=logging.INFO)
logger = logging.getLogger()


################################################################################################
### Constants
################################################################################################
REFERENCE_SEARCH_RESULT_FILE_PATH = "./../data/Google_Result3.json"
OPPONENT_SEARCH_RESULTS_FILE_PATH = "./../submission/hw1.json"
OUTPUT_FILE_PATH = "./../submission/hw1.csv"


################################################################################################
### Main Program
################################################################################################
def main(reference_search_results_file_path: str, opponent_search_results_file_path: str, output_file_path: str):
    reference_results = load_results(reference_search_results_file_path)  # read reference results
    logger.info(f"# of reference results: {len(reference_results)}")

    opponent_results = load_results(opponent_search_results_file_path)  # read opponent results
    logger.info(f"# of opponent results: {len(opponent_results)}")

    queries = reference_results.keys()  # get all reference search engine queries

    statistics = [["Queries", "Number of Overlapping Results", "Percent Overlap", "Spearman Coefficient"]]

    count = 0
    total_overlap = 0
    total_overlap_percent = 0
    total_rank_correlation = 0

    for idx, query in enumerate(queries):  # loop over all queries
        logger.info(f"Query #{idx + 1}; Query: {query}")

        # sanitize URLs
        sanitized_reference_results = url_sanitizer(reference_results[query])
        sanitized_opponent_results = url_sanitizer(opponent_results[query])

        diff = list()

        # loop over all results for query, and retrieve index of matching corresponding
        # URLs and calculate difference in indices
        for ref_idx, url in enumerate(sanitized_reference_results):
            try:
                opp_idx = sanitized_opponent_results.index(url)
                diff.append(ref_idx - opp_idx)
            except:
                pass

        overlap, rho = spearmans_rho(diff)  # calculate overlap, and spearmans rank coefficient
        overlap_percent = (overlap / len(sanitized_reference_results)) * 100  # calculate overlap percentage

        # WARN: NO NEED to normalize
        # if the overlap is not full, need to multiple rho with overlap percentage
        rank_correlation = rho # (overlap_percent / 100) * rho

        total_overlap += overlap  # maintain sum of overlaps from
        total_overlap_percent += overlap_percent  # maintain sum of overlap percentage
        total_rank_correlation += rank_correlation  # maintain sum of rho values

        result = [f"Query {idx + 1}", overlap, overlap_percent, rank_correlation]

        logger.info(f"Result: {','.join(str(x) for x in result)}")

        statistics.append(result)  # save current results

        count += 1

    final_result = ["Averages", total_overlap / count, total_overlap_percent / count, total_rank_correlation / count]

    # calculate average of overlap, overlap percent and rho values
    statistics.append(final_result)

    logger.info(f"Final ... {','.join(str(x) for x in final_result)}")

    # save the results in CSV format
    with open(output_file_path, mode="w") as file:
        logger.info(f"Writing statistics to file: {output_file_path}")

        csv_writer = csv.writer(file, delimiter=",", quotechar='"', quoting=csv.QUOTE_MINIMAL)
        csv_writer.writerows(statistics)

        logger.info("File written successfully!")

    logger.info("Statistics Calculate Successfully!!!")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="Search Engine Result Comparator",
        description="This programs compare the results from two engines from a given set of queries and corresponding results in a JSON format and calculated Overlap Percentage and Spearman's Coefficient.",
        epilog="Information Retrieval",
    )
    parser.add_argument("--reference_search_results_file", type=str, default=REFERENCE_SEARCH_RESULT_FILE_PATH)
    parser.add_argument("--opponent_search_results_file", type=str, default=OPPONENT_SEARCH_RESULTS_FILE_PATH)
    parser.add_argument("--output_file", type=str, default=OUTPUT_FILE_PATH)

    args = parser.parse_args()
    main(args.reference_search_results_file, args.opponent_search_results_file, args.output_file)
