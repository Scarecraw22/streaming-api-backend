package pl.agh.iet.db.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pl.agh.iet.db.MetadataEntity;

import java.util.Optional;

@Repository
public interface MetadataRepository extends MongoRepository<MetadataEntity, String> {

    @Query("{ 'streamName': ?0 }")
    Optional<MetadataEntity> findByStreamName(String streamName);
}
