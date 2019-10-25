package se.alipsa.ride.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

public class ConsoleRepositoryEventListener extends AbstractRepositoryListener {

   private Logger log = LogManager.getLogger();

   @Override
   public void artifactInstalled(RepositoryEvent event) {
      log.debug("artifact {} installed to file {}", event.getArtifact(), event.getFile());
   }

   @Override
   public void artifactInstalling(RepositoryEvent event) {
      log.debug("installing artifact {} to file {}", event.getArtifact(), event.getFile());
   }

   @Override
   public void artifactResolved(RepositoryEvent event) {
      log.debug("artifact {} resolved from repository {}", event.getArtifact(),
         event.getRepository());
   }

   @Override
   public void artifactDownloading(RepositoryEvent event) {
      log.debug("downloading artifact {} from repository {}", event.getArtifact(),
         event.getRepository());
   }

   @Override
   public void artifactDownloaded(RepositoryEvent event) {
      log.debug("downloaded artifact {} from repository {}", event.getArtifact(),
         event.getRepository());
   }

   @Override
   public void artifactResolving(RepositoryEvent event) {
      log.debug("resolving artifact {}", event.getArtifact());
   }

}
