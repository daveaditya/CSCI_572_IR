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
    opponent_results = load_results(opponent_search_results_file_path)  # read opponent results

    queries = reference_results.keys()

    result = [["Queries", "Number of Overlapping Results", "Percent Overlap", "Spearman Coefficient"]]

    count = 0
    total_overlap = 0
    total_overlap_percent = 0
    total_rho = 0

    for idx, query in enumerate(queries):
        sanitized_reference_results = url_sanitizer(reference_results[query])
        sanitized_opponent_results = url_sanitizer(opponent_results[query])

        diff = list()

        for ref_idx, url in enumerate(sanitized_reference_results):
            try:
                opp_idx = sanitized_opponent_results.index(url)
                diff.append(ref_idx - opp_idx)
            except:
                pass

        overlap, rho = spearmans_rho(diff)
        overlap_percent = (overlap / len(sanitized_reference_results)) * 100

        total_overlap += overlap
        total_overlap_percent += overlap_percent
        total_rho += rho

        result.append([f"Query {idx + 1}", overlap, overlap_percent, rho])
        count += 1

    result.append(["Averages", total_overlap / count, total_overlap_percent / count, total_rho / count])

    with open(output_file_path, mode="w") as file:
        csv_writer = csv.writer(file, delimiter=",", quotechar='"', quoting=csv.QUOTE_MINIMAL)
        csv_writer.writerows(result)


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
