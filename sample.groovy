def globals = [:]

def createSchema() {
  mgmt = graph.openManagement()

  name = mgmt.makePropertyKey("name").dataType(String.class).make()
  mgmt.makePropertyKey("location").dataType(String.class).make()
  mgmt.makeVertexLabel("person").make()
  mgmt.makeVertexLabel("address").make()

  mgmt.makeEdgeLabel("hasAddress").make()

  // create name mixed index
  mgmt.buildIndex('name', Vertex.class).addKey(name).buildMixedIndex('search')

  // add a composite index
  mgmt.buildIndex('nameComposite', Vertex.class).addKey(name).buildCompositeIndex()
  mgmt.commit()

  // Wait for the indices to become available
  ManagementSystem.awaitGraphIndexStatus(graph, 'name').status(SchemaStatus.REGISTERED, SchemaStatus.ENABLED).call()
  ManagementSystem.awaitGraphIndexStatus(graph, 'nameComposite').status(SchemaStatus.REGISTERED, SchemaStatus.ENABLED).call()
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
