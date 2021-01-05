// Copyright 2019 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// an init script that returns a Map allows explicit setting of global bindings.
def globals = [:]

/**
 * Initialize the graph with the default graph of the gods
 */
def init(ctx) {
  graph.traversal().addV("person").property("name", "Jack").next()
}

// defines a sample LifeCycleHook that prints some output to the Gremlin Server console.
// note that the name of the key in the "global" map is unimportant.
globals << [hook : [
        onStartUp: { ctx ->
          ctx.logger.info("Executed once at startup of Gremlin Server.")
          // Load graph of the gods
          GraphOfTheGodsFactory.load(graph)

          // Add index on name property
          def mgmt = graph.openManagement()
          name = mgmt.getPropertyKey('name')
          mgmt.buildIndex('byNameMixed', Vertex.class).addKey(name).buildMixedIndex("search")
          mgmt.commit()
          ManagementSystem.awaitGraphIndexStatus(graph, "byNameMixed").call()

          mgmt = graph.openManagement()
          mgmt.updateIndex(mgmt.getGraphIndex('byNameMixed'), SchemaAction.REINDEX).get()
          mgmt.commit()

          // Add vertex index
          mgmt = graph.openManagement()
          reason = mgmt.getPropertyKey("reason")
          mgmt.buildIndex("byReasonMixed", Edge.class).addKey(reason).buildMixedIndex("search")
          mgmt.commit()
          ManagementSystem.awaitGraphIndexStatus(graph, "byReasonMixed").call()
          mgmt = graph.openManagement()
          mgmt.updateIndex(mgmt.getGraphIndex('byReasonMixed'), SchemaAction.REINDEX).get()
          mgmt.commit()
        },
        onShutDown: { ctx ->
            ctx.logger.info("Executed once at shutdown of Gremlin Server.")
        }
] as LifeCycleHook]

// define the default TraversalSource to bind queries to - this one will be named "g".
globals << [g : graph.traversal()]
