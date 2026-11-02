package main

import (
	"context"
	"dev.inreach/crawler/greenhouse"
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"time"
)

func main() {
	board := flag.String("board", "elastic", "Greenhouse board token")
	flag.Parse()
	ctx, cancel := context.WithTimeout(context.Background(), 45*time.Second)
	defer cancel()
	result, err := greenhouse.FetchToken(ctx, *board)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
	type item struct {
		ID    string `json:"id"`
		Title string `json:"title"`
		URL   string `json:"url"`
	}
	jobs := make([]item, 0, len(result.Jobs))
	for _, j := range result.Jobs {
		if j.Title == nil {
			fmt.Fprintln(os.Stderr, "job missing title")
			os.Exit(1)
		}
		jobs = append(jobs, item{j.ID, *j.Title, j.URL})
	}
	payload := struct {
		Board    string `json:"board"`
		Complete bool   `json:"complete"`
		RawCount int    `json:"rawCount"`
		Jobs     []item `json:"jobs"`
	}{*board, !result.Truncated && result.Verified, len(jobs), jobs}
	if err := json.NewEncoder(os.Stdout).Encode(payload); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
