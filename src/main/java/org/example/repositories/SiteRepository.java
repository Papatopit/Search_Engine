package org.example.repositories;


import org.example.model.Site;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    Optional<Site> findByName(String siteName);

    Optional<Site> findByUrl(String url);

    @Override
    @Modifying
    @Query("DELETE FROM Site")
    void deleteAll();
}
