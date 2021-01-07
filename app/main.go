package main

import (
	"flag"
	"log"
	"time"

	"github.com/northwesternmutual/grammes"
	"github.com/northwesternmutual/grammes/query/predicate"
)

var host = flag.String("h", "ws://localhost:8182", "janusgraph host address")
var delay = flag.Int("d", 60, "Seconds to delay")

func main() {
	flag.Parse()

	// Sleep for about 1 minute
	log.Println("Give the janusgraph server some time to warm up")
	ticker := time.NewTicker(time.Second)
	count := *delay
	for count > 0 {
		select {
		case <-ticker.C:
			log.Printf("%d seconds", count)
			count--
		}
	}
	log.Println("Let's go ahead now")

	cli, err := grammes.DialWithWebSocket(*host)
	handle(err)

	g := grammes.Traversal()
	// Do a full-text search
	query := g.V().Has("name", predicate.TextContains("'Jack'"))

	res, err := cli.VerticesByQuery(query)
	handle(err)

	for _, v := range res {
		log.Printf("Vertex: %v", v.Value)
	}

	// Do an exact search
	query = g.V().Has("name", "Jack")

	res, err = cli.VerticesByQuery(query)
	handle(err)

	for _, v := range res {
		log.Printf("Vertex: %v", v.Value)
	}
}

func handle(err error) {
	if err != nil {
		log.Fatalf("Error: %v", err)
	}
}
