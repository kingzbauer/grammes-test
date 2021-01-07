def globals = [:]

def createSchema() {
  mgmt = graph.openManagement()
  mgmt.makePropertyKey("name").dataType(String.class).make()
  mgmt.makePropertyKey("location").dataType(String.class).make()
  mgmt.makeVertexLabel("person").make()
  mgmt.makeVertexLabel("address").make()

  mgmt.makeEdgeLabel("hasAddress").make()

  mgmt.commit()
}

def initGraph() {
  createSchema()

  tx = graph.newTransaction()

  jack = tx.addVertex(T.label, "person", "name", "Jack")
  addr = tx.addVertex(T.label, "address", "location", "Kenya")

  jack.addEdge("hasAddress", addr)

  tx.commit()
}

globals << [hook : [
        onStartUp: { ctx ->
          ctx.logger.info("Executed once at startup of Gremlin Server.")
          initGraph()
        },
        onShutDown: { ctx ->
            ctx.logger.info("Executed once at shutdown of Gremlin Server.")
        }
] as LifeCycleHook]

// define the default TraversalSource to bind queries to - this one will be named "g".
globals << [g : graph.traversal()]
