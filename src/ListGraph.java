

import java.io.Serializable;
import java.util.*;

public class ListGraph<T> implements Graph<T>, Serializable {
    private Map<T, Set<Edge<T>>> edges;

    public ListGraph() {
        this.edges = new HashMap<>();
    }

    @Override
    public void connect(T from, T to, String name, int weight) {
        // Kontrollera om noderna finns i grafen
        if (!edges.containsKey(from) || !edges.containsKey(to)) {
            throw new NoSuchElementException("En eller båda noderna finns inte i grafen!");
        }

        // Kontrollera om vikten är negativ
        if (weight < 0) {
            throw new IllegalArgumentException("Vikten kan inte vara negativ!");
        }

        // Kontrollera om det redan finns en kant mellan noderna
        for (Edge<T> edge : edges.get(from)) {
            if (edge.getDestination().equals(to)) {
                throw new IllegalStateException("Den finns redan en kant mellan noderna");
            }
        }

        // Lägg till kanter mellan noderna
        edges.get(from).add(new Edge<>(to, name, weight)); // Kant från 'from' till 'to'
        edges.get(to).add(new Edge<>(from, name, weight)); // Kant från 'to' till 'from'
    }


    @Override
    public void add(T node) {
        if (!edges.containsKey(node)) {
            edges.put(node, new HashSet<>());
        }
    }




    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Loopa igenom alla noder
        for (T node : edges.keySet()) {
            stringBuilder.append(node.toString()).append(": ");


            Set<Edge<T>> edgesFromNode = edges.get(node);
            for (Edge<T> edge : edgesFromNode) {
                stringBuilder.append(edge.toString()).append(", ");
            }


            if (!edgesFromNode.isEmpty()) {
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public void setConnectionWeight(T node1, T node2, int weight) {
        // Kontrollera om noderna finns i grafen
        if (!edges.containsKey(node1) || !edges.containsKey(node2)) {
            throw new NoSuchElementException("En eller båda noderna hittades inte i grafen");
        }

        // Hitta kanten mellan noderna från node1 till node2
        Edge<T> edge1 = getEdgeBetween(node1, node2);

        // Hitta kanten mellan noderna från node2 till node1
        Edge<T> edge2 = getEdgeBetween(node2, node1);

        // Kontrollera om kanten finns i båda riktningarna
        if (edge1 == null || edge2 == null) {
            throw new NoSuchElementException("Det finns ingen kant mellan noderna");
        }

        // Kontrollera om vikten är negativ
        if (weight < 0) {
            throw new IllegalArgumentException("Vikten måste vara positiv");
        }

        // Uppdatera vikten för kanten från node1 till node2
        edge1.setWeight(weight);

        // Uppdatera vikten för kanten från node2 till node1
        edge2.setWeight(weight);
    }

    @Override
    public Set<T> getNodes() {
        return Set.copyOf(edges.keySet());
    }



    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if (!edges.containsKey(node)) {
            throw new NoSuchElementException("Noden hittades inte i grafen");
        }
        return List.copyOf(edges.get(node));
    }



    @Override
    public Edge<T> getEdgeBetween(T node1, T node2) {
        if (!edges.containsKey(node1) || !edges.containsKey(node2)) {
            throw new NoSuchElementException("En eller båda noderna hittades inte i grafen");
        }

        Set<Edge<T>> edgesFromNode1 = edges.get(node1);
        for (Edge<T> edge : edgesFromNode1) {
            if (edge.getDestination().equals(node2)) {
                return edge;
            }
        }

        return null;
    }

    @Override
    public void disconnect(T node1, T node2) {
        // Kontrollera om noderna finns i grafen
        if (!edges.containsKey(node1) || !edges.containsKey(node2)) {
            throw new NoSuchElementException("En eller båda noderna hittades inte i grafen");
        }

        // Hitta kanten mellan noderna
        Edge<T> edge1 = getEdgeBetween(node1, node2);
        Edge<T> edge2 = getEdgeBetween(node2, node1);

        // Kontrollera om kanten finns
        if (edge1 == null || edge2 == null) {
            throw new IllegalStateException("Det finns ingen kant mellan noderna");
        }

        // Ta bort kanten från båda noderna
        edges.get(node1).remove(edge1);
        edges.get(node2).remove(edge2);
    }

    @Override
    public void remove(T node) {
        if (!edges.containsKey(node)) {
            throw new NoSuchElementException("Noden hittades inte");
        }

        // Ta bort noden från grafen
        edges.remove(node);

        // Ta bort alla kanter kopplade till den borttagna noden
        for (T otherNode : edges.keySet()) {
            Set<Edge<T>> edgesOfOtherNode = edges.get(otherNode);
            edgesOfOtherNode.removeIf(edge -> edge.getDestination().equals(node));
        }
    }

    @Override
    public boolean pathExists(T from, T to) {

        if (!edges.containsKey(from) || !edges.containsKey(to)) {
            return false;
        }


        Set<T> visited = new HashSet<>();


        return dfs(from, to, visited);
    }

    // Hjälp-metod för djupet först sökning
    private boolean dfs(T current, T target, Set<T> visited) {

        if (current.equals(target)) {
            return true;
        }


        visited.add(current);


        Set<Edge<T>> currentEdges = edges.get(current);


        if (currentEdges == null || currentEdges.isEmpty()) {
            return false;
        }


        for (Edge<T> edge : currentEdges) {
            T nextNode = edge.getDestination();
            if (!visited.contains(nextNode) && dfs(nextNode, target, visited)) {
                return true;
            }
        }


        visited.remove(current);
        return false;
    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {
        if (!edges.containsKey(from) || !edges.containsKey(to)) {
            return null;
        }

        Set<T> visited = new HashSet<>();
        Queue<Map.Entry<T, List<Edge<T>>>> queue = new LinkedList<>();

        queue.add(new AbstractMap.SimpleEntry<>(from, new ArrayList<>()));
        visited.add(from);

        Integer totalCost = 0; // Håller reda på den totala kostnaden för vägen

        while (!queue.isEmpty()) {
            Map.Entry<T, List<Edge<T>>> entry = queue.poll();
            T current = entry.getKey();
            List<Edge<T>> path = entry.getValue();

            if (current.equals(to)) {
                // Beräkna totala kostnaden för vägen när vi når målnoden
                totalCost = calculatePathCost(path);
                return path;
            }

            for (Edge<T> edge : edges.get(current)) {
                T neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);

                    List<Edge<T>> newPath = new ArrayList<>(path);
                    newPath.add(edge);

                    queue.add(new AbstractMap.SimpleEntry<>(neighbor, newPath));
                }
            }
        }

        return null;
    }
    private Integer calculatePathCost(List<Edge<T>> path) {
        if (path == null) {
            return 0;
        }

        Integer totalCost = 0;
        for (Edge<T> edge : path) {
            // Här antas det att getWeight-metoden returnerar en double
            totalCost += edge.getWeight().intValue();
        }

        return totalCost;
    }




}










