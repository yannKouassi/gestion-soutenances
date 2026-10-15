package ci.univ.soutenances.repository;

import java.util.List;

import ci.univ.soutenances.model.Memoire;

public interface MemoireRepository {
    List<Memoire> findAll();
}
