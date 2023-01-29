from collections import OrderedDict
import logging
import random
from time import sleep

from bs4 import BeautifulSoup
import requests

logger = logging.getLogger()

random.seed(42)

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36"
}


class SearchEngine:
    @staticmethod
    def search(
        search_engine_url: str,
        query: str,
        result_selector_attrs: str,
        can_search_further_func=None,
        result_limit: int = 10,
        should_sleep: bool = True,
    ):
        results = OrderedDict()  # holds results

        logger.info(f"Starting to get search results until {result_limit} results obtained for query")

        page = 1  # used for paginated search in Ask.com

        while len(results) < result_limit:  # perform search until result_limit is reached

            # prevents loading too many pages too soon
            if should_sleep:
                sleep_for = random.randint(10, 100)
                logger.info(f"Sleeping for {sleep_for} seconds")
                sleep(sleep_for)

            logger.info(f"Search Page: {page}")

            logger.info(f"Now Searching: {query}")
            result_page = requests.get(
                search_engine_url, params={"q": query.replace(" ", "+"), "page": page}, headers=HEADERS
            )
            logger.info(f"Requested: {result_page.url}")
            logger.info(f"Search Engine Response Status Code: {result_page.status_code}")

            if result_page.status_code != 200:
                raise Exception(f"Server returned {result_page.status_code} for query: {result_page.url}")

            soup = BeautifulSoup(result_page.text, "html.parser")

            # avoid duplicate results
            logger.info("Extracting result from search engine response")
            new_results = SearchEngine.__scrape_search_result(soup, result_selector_attrs, result_limit)
            results.update(new_results)

            can_search_further = can_search_further_func(soup, result_limit)

            logger.info(f"No of results collected: {len(results)}")
            if len(results) < result_limit and can_search_further:
                page += 1
            else:
                return list(results.keys())[:result_limit]

        return list(results.keys())[:result_limit]

    @staticmethod
    def __scrape_search_result(soup, result_selector_attrs, result_limit) -> OrderedDict:
        logger.info("Getting all result anchor tags")
        raw_results = soup.find_all("a", attrs=result_selector_attrs, limit=result_limit)

        logger.info("Extracting the links (href) from result anchor tags")
        results = list()
        for result in raw_results:
            results.append(result["href"])

        logger.info(f"Retrieved {len(results)} links")
        return OrderedDict.fromkeys(results)
