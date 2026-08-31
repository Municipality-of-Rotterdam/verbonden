package nl.rotterdam.verbonden.core.domain;

/**
 * Interface for value types that wrap a single value.
 *
 * @param <T> the type of the wrapped value
 */
public interface ValueHolder<T> {

    T getValue();
}
