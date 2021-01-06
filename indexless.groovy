def globals = [:]

globals << [hook : [
        onStartUp: { ctx ->
          ctx.logger.info("Executed once at startup of Gremlin Server.")
          // Load graph of the gods without index
          GraphOfTheGodsFactory.loadWithoutMixedIndex(graph)
        },
        onShutDown: { ctx ->
            ctx.logger.info("Executed once at shutdown of Gremlin Server.")
        }
] as LifeCycleHook]

// define the default TraversalSource to bind queries to - this one will be named "g".
globals << [g : graph.traversal()]
