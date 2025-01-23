import java.util.Objects;
import java.io.Serializable;
class Edge<T> implements Serializable {
    private final String name;
    private Integer weight;
    private final T destination;

    // Constructor to initialize the Edge object
    Edge(T destination, String name, Integer weight) {
        this.destination = Objects.requireNonNull(destination);
        this.name = name;
        if (Double.isNaN(weight) || weight <= 0) {
            throw new IllegalArgumentException("Vikten måste vara positiv!");
        }
        this.weight = weight;
    }


    public T getDestination() {
        return destination;
    }


    public Integer getWeight() {
        return weight;
    }


    public void setWeight(Integer weight) {
        if (Double.isNaN(weight) || weight <= 0) {
            throw new IllegalArgumentException("Vikten måsten vara positiv!"); // Validate weight
        }
        this.weight = weight;
    }


    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return String.format("till %s med %s tar %d", destination, name, weight);
    }
}

