package pl.agh.iet.db.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.agh.iet.db.MetadataEntity;

@Repository
public interface MetadataRepository extends MongoRepository<MetadataEntity, String> {
}
