Python Version: 3.10.9

Note: Dependencies are specified in requirements.txt

To run the scripts the commands are as follows:

> Task 1

python task1.py --queries_set_file=./data/100QueriesSet3.txt --results_output_file=./submission/hw1.json --n_queries_to_search=100 --total_n_of_queries=100 --save_batch_size=2 --use_checkpoint=true --result_limit=10


> Task 2

python task2.py --reference_search_results_file=./data/Google_Result3.json --opponent_search_results_file=./submission/hw1.json --output_file=./submission/hw1.csv