echo '{
  "query": "{
    Get{
      MovieSearch (
        limit: 5
        nearText: {
          concepts: [\"princess\"],
        }
      ){
        movieName
        director
        plot
      }
    }
  }"
}'  | curl \
    -X POST \
    -H 'Content-Type: application/json' \
    -d @- \
    localhost:8080/v1/graphql
