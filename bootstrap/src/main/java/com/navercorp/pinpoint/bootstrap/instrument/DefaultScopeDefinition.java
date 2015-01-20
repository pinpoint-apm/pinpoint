package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public class DefaultScopeDefinition implements ScopeDefinition {

    private final String name;
    private final Type type;

    public DefaultScopeDefinition(String name, Type type) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (type == null) {
            throw new NullPointerException("scopeType must not be null");
        }
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultScopeDefinition that = (DefaultScopeDefinition) o;

        if (!name.equals(that.name)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
