package concert.domain.shared.repositories;

public interface DomainRepository<T> {
    void save(T t);
}
